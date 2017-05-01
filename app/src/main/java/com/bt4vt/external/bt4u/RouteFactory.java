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

package com.bt4vt.external.bt4u;

import android.graphics.Color;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates {@link Route} objects.
 *
 * @author Ben Sechrist
 */
@Singleton
public class RouteFactory {

  @Inject
  private StopFactory stopFactory;

  public List<Route> routes(JSONArray jsonArray) throws JSONException {
    List<Route> routes = new ArrayList<>();
    for (int i = 0; i < jsonArray.length(); i++) {
      JSONObject jsonRoute = jsonArray.getJSONObject(i);
      Route route = route(jsonRoute);
      routes.add(route);
    }
    return routes;
  }

  public Route route(JSONObject jsonRoute) throws JSONException {
    Route route = new Route(jsonRoute.getString("shortName"));
    if (jsonRoute.has("fullName")) {
      route.setFullName(jsonRoute.getString("fullName"));
    }
    if (jsonRoute.has("plot")) {
      route.setPlot(jsonRoute.getString("plot"));
    }
    if (jsonRoute.has("plotColor")) {
      route.setColor(Color.parseColor(String.format("#%s", jsonRoute.getString("plotColor"))));
    }
    if (jsonRoute.has("stops")) {
      List<Stop> stops = new ArrayList<>();
      JSONArray jsonStops = jsonRoute.getJSONArray("stops");
      for (int j = 0; j < jsonStops.length(); j++) {
        JSONObject jsonStop = jsonStops.getJSONObject(j);
        Stop stop = stopFactory.stop(jsonStop);
        stops.add(stop);
      }
      route.setStops(stops);
    }
    return route;
  }
}
