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

import com.bt4vt.repository.domain.DocumentElement;
import com.bt4vt.repository.domain.Route;
import com.bt4vt.repository.domain.RouteFactory;
import com.bt4vt.repository.domain.Stop;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.commons.lang.StringEscapeUtils;
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
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Http-based Blacksburg Transit implementation of {@link TransitRepository}.
 *
 * @author Ben Sechrist
 */
@Singleton
public class HttpBlacksburgTransitRepository implements TransitRepository {

  private static final String BT_MOBILE_PATH = "http://bt4u.org/Mobile.aspx";
  private static final String ROUTE_LIST_BOX_ID = "routeListBox";
  private static final String BT_STOPS_PATH = "http://bt4u.org/LiveMap.aspx/GetBusStops";

  @Inject
  protected RouteFactory routeFactory;

  private Serializer serializer = new Persister();

  @Override
  public List<Route> getRoutes() {
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
      throw new RuntimeException(e);
    }
    return routes;
  }

  @Override
  public List<Stop> getStops(Route route) {
    List<Stop> stops = new ArrayList<>();

    try {
      String stopsString = fetchStops(route.getName());
      DocumentElement doc = serializer.read(DocumentElement.class, stopsString);
      stops.addAll(doc.stops);

      return stops;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private String fetchStops(String routeName) {
    HttpURLConnection conn = null;
    try {
      URL url = new URL(BT_STOPS_PATH);

      conn = getHttpURLConnection(url);

      OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
      wr.write("{\"eventArgument\":\"" + routeName + "\"}");
      wr.flush();

      int HttpResult = conn.getResponseCode();
      if (HttpResult == HttpURLConnection.HTTP_OK) {
        String result = getResponse(conn.getInputStream());

        final String[] split = result.split(":");
        assert split.length == 2;
        final String encodedStopsString = split[1].replaceAll("\"", "");

        return StringEscapeUtils.unescapeJava(encodedStopsString);
      } else {
        throw new RuntimeException(HttpResult + " error getting stops for " + routeName
            + "\n" + conn.getResponseMessage());
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      if (conn != null) {
        conn.disconnect();
      }
    }
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
}
