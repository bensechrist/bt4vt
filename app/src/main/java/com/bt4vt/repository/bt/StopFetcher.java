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

package com.bt4vt.repository.bt;

import android.graphics.Color;
import android.util.Log;

import com.bt4vt.repository.domain.Stop;
import com.bt4vt.repository.exception.FetchException;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.inject.Singleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Fetches stops from BT4U.
 *
 * @author Ben Sechrist
 */
@Singleton
public class StopFetcher extends Fetcher {

  private static final String BT_STOP_PATH = "http://bt4userver.newcitystaging.com/routes/";
  private static final LatLngBounds VALID_BOUNDS = new LatLngBounds(new LatLng(37, -81), new LatLng(37.5, -79.5));

  /**
   * Returns a list of all stops for the given route <code>shortName</code>.
   * Every stop in the list will contain route pattern information.
   * @param shortName the route short name
   * @return list of stops
   * @throws FetchException Non serious error occurs fetching stops
   */
  public List<Stop> get(String shortName) throws FetchException {
    try {
      return readResponse(fetchResponse(new URL(BT_STOP_PATH + shortName)));
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  private List<Stop> readResponse(String response) {
    List<Stop> stops = new ArrayList<>();
    if (response.isEmpty()) return stops;
    try {
      JSONObject jsonResponse = new JSONArray(response).getJSONObject(0);
      String routePlot = jsonResponse.getString("routePlot");
      String routePlotColor = jsonResponse.getString("routePlotColor");
      JSONArray array = jsonResponse.getJSONObject("mapDecorations").getJSONArray("decorations");
      for (int i=0; i<array.length(); i++) {
        JSONObject obj = array.getJSONObject(i);
        Stop stop = Stop.valueOf(obj.getString("popupTitle"));
        stop.setLatitude(obj.getDouble("latitude"));
        stop.setLongitude(obj.getDouble("longitude"));
        stop.setRoutePattern(routePlot);
        try {
          stop.setRoutePatternColor(Color.parseColor(String.format("#%s", routePlotColor)));
        } catch (IllegalArgumentException e) {
          stop.setRoutePatternColor(null);
        }
        if (isValid(stop))
          stops.add(stop);
        else
          Log.i("StopFetcher", String.format("Excluding stop %s", stop.toString()));
      }
      return stops;
    } catch (JSONException e) {
      return stops;
    }
  }

  private boolean isValid(Stop stop) {
    return VALID_BOUNDS.contains(new LatLng(stop.getLatitude(), stop.getLongitude()));
  }

}
