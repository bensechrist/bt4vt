/*
 * Copyright 2015 Ben Sechrist
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bt4vt.repository;

import android.content.Context;

import com.bt4vt.R;
import com.bt4vt.repository.domain.Bus;
import com.bt4vt.repository.domain.DocumentElement;
import com.bt4vt.repository.domain.NextDeparture;
import com.bt4vt.repository.domain.Route;
import com.bt4vt.repository.domain.RouteFactory;
import com.bt4vt.repository.domain.Stop;
import com.bt4vt.repository.exception.TransitRepositoryException;
import com.bt4vt.repository.listener.BusListener;
import com.bt4vt.repository.listener.BusTimerTask;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;

/**
 * Http-based Blacksburg Transit implementation of {@link TransitRepository}.
 *
 * @author Ben Sechrist
 */
@Singleton
public class HttpBlacksburgTransitRepository implements TransitRepository {

  private static final String BT_MOBILE_PATH = "http://bt4uclassic.org/Mobile.aspx";
  private static final String ROUTE_LIST_BOX_ID = "routeListBox";
  private static final String BT_STOPS_PATH = "http://bt4uclassic.org/LiveMap.aspx/GetBusStops";
  private static final String BT_DEPARTURES_PATH = "http://bt4uclassic.org/LiveMap.aspx/GetNextDepartures";
  private static final String BT_UPDATE_PATH = "http://bt4uclassic.org/LiveMap.aspx/UpdateLatestInfo";

  @Inject
  RouteFactory routeFactory;

  @Inject
  Context context;

  private final Serializer serializer = new Persister();

  private final List<BusListener> busListeners = new ArrayList<>();

  private BusTimerTask busTimerTask;

  private final Timer timer = new Timer();

  @Override
  public List<Route> getRoutes() throws TransitRepositoryException {
    List<Route> routes = new ArrayList<>();
    try {
      Document doc = Jsoup.connect(BT_MOBILE_PATH).get();
      Element elem = doc.getElementById(ROUTE_LIST_BOX_ID);
      Elements children = elem.children();
      for (Element e : children) {
        if (!e.val().isEmpty()) {
          routes.add(routeFactory.createRoute(e.val()));
        }
      }
    } catch (IOException e) {
      throw new TransitRepositoryException(e);
    }
    return routes;
  }

  @Override
  public List<Stop> getStops() throws TransitRepositoryException {
    return getStops(null);
  }

  @Override
  public List<Stop> getStops(Route route) throws TransitRepositoryException {
    List<Stop> stops = new ArrayList<>();

    final String routeName = (route != null) ? route.getName() : "";
    String stopsString = fetchStops(routeName);

    try {
      DocumentElement doc = getDocumentElement(stopsString);
      stops.addAll(doc.stops);

      return stops;
    } catch (Exception e) { // Exception from reading xml by serializer
      throw new RuntimeException(context.getString(R.string.xml_parse_error_format, stopsString, e));
    }
  }

  @Override
  public List<NextDeparture> getNextDepartures(Stop stop) throws TransitRepositoryException {
    return getNextDepartures(stop, null);
  }

  @Override
  public List<NextDeparture> getNextDepartures(Stop stop, Route route) throws TransitRepositoryException {
    List<NextDeparture> departures = new ArrayList<>();

    final String stopId;
    if (route == null) {
      stopId = String.valueOf(stop.getCode());
    } else {
      stopId = String.format("%s,%s", stop.getCode(), route.getName());
    }
    String departuresString = fetchDepartures(stopId);

    try {
      DocumentElement doc = getDocumentElement(departuresString);
      if (!doc.nextDepartures.isEmpty()) {
        departures.addAll(doc.nextDepartures);
      }

      return departures;
    } catch (Exception e) { // Exception from reading xml by serializer
      throw new RuntimeException(context.getString(R.string.xml_parse_error_format, departuresString, e));
    }
  }

  @Override
  public List<Bus> getBusLocations(Route route) throws TransitRepositoryException {
    List<Bus> buses = new ArrayList<>();

    String busesString = fetchBuses(route.getName());

    try {
      if (busesString.isEmpty()) {
        return buses;
      }
      DocumentElement doc = getDocumentElement(busesString);
      Date now = new Date();
      for (Bus bus : doc.buses) {
        if (!bus.isTripper()) {
          bus.setLastUpdated(now);
          buses.add(bus);
        }
      }

      return buses;
    } catch (Exception e) { // Exception from reading xml by serializer
      throw new RuntimeException(context.getString(R.string.xml_parse_error_format, busesString, e));
    }
  }

  @Override
  public void registerBusListener(Route route, BusListener busListener) {
    busListeners.add(busListener);
    if (busTimerTask == null) {
      busTimerTask = new BusTimerTask(route, busListeners, this);
      timer.scheduleAtFixedRate(busTimerTask, 0, 1000);
    } else {
      busTimerTask.addListener(busListener);
    }
  }

  @Override
  public void clearBusListener(BusListener busListener) {
    if (busTimerTask != null) {
      busTimerTask.awaitTermination();
    }
    busListeners.remove(busListener);
    if (busListeners.isEmpty()) {
      clearBusListeners();
    } else if (busTimerTask != null) {
      busTimerTask.removeListener(busListener);
    }
  }

  @Override
  public void clearBusListeners() {
    if (busTimerTask != null) {
      busTimerTask.awaitTermination();
      busTimerTask.cancel();
    }
    busListeners.clear();
    busTimerTask = null;
  }

  private DocumentElement getDocumentElement(String documentString) throws Exception {
    return serializer.read(DocumentElement.class, documentString);
  }

  protected String fetchBuses(String routeName) throws TransitRepositoryException {
    HttpURLConnection conn = null;
    try {
      URL url = new URL(BT_UPDATE_PATH);

      conn = getHttpURLConnection(url);

      writeEventArgument(routeName, conn.getOutputStream());

      int HttpResult = conn.getResponseCode();
      if (HttpResult == HttpURLConnection.HTTP_OK) {
        return readResponse(conn.getInputStream());
      } else {
        // Quietly fail and log exception
        throw new TransitRepositoryException(
            new Exception(String.format("%d error getting buses for %s\n%s",
                HttpResult, routeName, conn.getResponseMessage())));
      }
    } catch (IOException e) {
      throw new TransitRepositoryException(e);
    } finally {
      if (conn != null) {
        conn.disconnect();
      }
    }
  }

  private String fetchDepartures(String stopId) throws TransitRepositoryException {
    HttpURLConnection conn = null;
    try {
      URL url = new URL(BT_DEPARTURES_PATH);

      conn = getHttpURLConnection(url);

      writeEventArgument(stopId, conn.getOutputStream());

      int HttpResult = conn.getResponseCode();
      if (HttpResult == HttpURLConnection.HTTP_OK) {
        return readResponse(conn.getInputStream());
      } else {
        throw new RuntimeException(HttpResult + " error getting departures for " + stopId
            + "\n" + conn.getResponseMessage());
      }
    } catch (IOException e) {
      throw new TransitRepositoryException(e);
    } finally {
      if (conn != null) {
        conn.disconnect();
      }
    }
  }

  private String fetchStops(String routeName) throws TransitRepositoryException {
    HttpURLConnection conn = null;
    try {
      URL url = new URL(BT_STOPS_PATH);

      conn = getHttpURLConnection(url);

      writeEventArgument(routeName, conn.getOutputStream());

      int HttpResult = conn.getResponseCode();
      if (HttpResult == HttpURLConnection.HTTP_OK) {
        return readResponse(conn.getInputStream());
      } else {
        throw new RuntimeException(HttpResult + " error getting stops for " + routeName
            + "\n" + conn.getResponseMessage());
      }
    } catch (IOException e) {
      throw new TransitRepositoryException(e);
    } finally {
      if (conn != null) {
        conn.disconnect();
      }
    }
  }

  private void writeEventArgument(String argument, OutputStream out) {
    try {
      OutputStreamWriter wr = new OutputStreamWriter(out);
      wr.write("{\"eventArgument\":\"" + argument + "\"}");
      wr.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String readResponse(InputStream in) {
    String result = getResponse(in);

    final String[] split = result.split(":", 2);
    final String encodedString = split[1].replaceAll("\"", "");

    final String unescapedString = unescapeJava(encodedString);
    return unescapedString.substring(0, unescapedString.length() - 1);
  }

  private HttpURLConnection getHttpURLConnection(URL url) throws IOException {
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setDoOutput(true);
    conn.setDoInput(true);
    conn.setRequestProperty("Content-Type", "application/json");
    conn.setRequestProperty("Accept", "application/json");
    conn.setRequestMethod("POST");
    return conn;
  }

  private String getResponse(InputStream in) {
    StringBuilder sb = new StringBuilder();
    String line;
    try {
      BufferedReader br = new BufferedReader(new InputStreamReader(in, "utf-8"));
      while ((line = br.readLine()) != null) {
        sb.append(line);
      }
      br.close();
      return sb.toString();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static String unescapeJava(String escaped) {
    if (!escaped.contains("\\u"))
      return escaped;

    String processed = "";

    int position = escaped.indexOf("\\u");
    while (position != -1) {
      if (position != 0)
        processed += escaped.substring(0, position);
      String token = escaped.substring(position + 2, position + 6);
      escaped = escaped.substring(position + 6);
      processed += (char) Integer.parseInt(token, 16);
      position = escaped.indexOf("\\u");
    }
    processed += escaped;

    return processed;
  }
}
