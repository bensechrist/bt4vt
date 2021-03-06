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

import com.google.android.gms.maps.model.LatLng;
import com.google.inject.Singleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Creates {@link Bus} objects.
 *
 * @author Ben Sechrist
 */
@Singleton
public class BusFactory {

  public List<Bus> buses(JSONArray jsonBuses) throws JSONException {
    List<Bus> buses = new ArrayList<>();
    for (int i = 0; i < jsonBuses.length(); i++) {
      JSONObject jsonBus = jsonBuses.getJSONObject(i);
      Bus bus = new Bus(jsonBus.getString("id"));
      JSONObject jsonRoute = jsonBus.getJSONObject("route");
      Route route = new Route(jsonRoute.getString("shortName"));
      route.setFullName(jsonRoute.getString("fullName"));
      bus.setRoute(route);
      bus.setLatLng(new LatLng(jsonBus.getDouble("latitude"), jsonBus.getDouble("longitude")));
      bus.setDirection(jsonBus.getInt("direction"));
      bus.setTripper(jsonBus.getBoolean("isTripper"));
      bus.setPassengers(jsonBus.getInt("passengers"));
      if (jsonBus.has("lastStopCode")) {
        bus.setLastStopCode(jsonBus.getString("lastStopCode"));
      }
      if (jsonBus.has("lastStopName")) {
        bus.setLastStopName(jsonBus.getString("lastStopName"));
      }
      bus.setTimestamp(new Date(jsonBus.getLong("timestamp")));
      buses.add(bus);
    }
    return buses;
  }
}
