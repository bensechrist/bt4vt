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

import com.google.inject.Singleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates {@link Departure} objects.
 *
 * @author Ben Sechrist
 */
@Singleton
public class DepartureFactory {

  public List<Departure> departures(JSONArray jsonDepartures) throws JSONException {
    List<Departure> departures = new ArrayList<>();
    for (int i = 0; i < jsonDepartures.length(); i++) {
      JSONObject jsonDeparture = jsonDepartures.getJSONObject(i);
      Departure departure = new Departure(jsonDeparture.getString("routeName"));
      List<String> departureStrings = new ArrayList<>();
      JSONArray jsonDepartureStrings = jsonDeparture.getJSONArray("departures");
      for (int j = 0; j < jsonDepartureStrings.length(); j++) {
        departureStrings.add(jsonDepartureStrings.getString(j));
      }
      departure.setDepartures(departureStrings);
      departures.add(departure);
    }
    return departures;
  }
}
