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

import com.activeandroid.query.Select;
import com.bt4vt.model.FavoriteStop;
import com.google.android.gms.maps.model.LatLng;
import com.google.inject.Singleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates {@link Stop} objects.
 *
 * @author Ben Sechrist
 */
@Singleton
class StopFactory {

  public List<Stop> stops(JSONArray jsonStops) throws JSONException {
    List<Stop> stops = new ArrayList<>();
    for (int i = 0; i < jsonStops.length(); i++) {
      JSONObject jsonStop = jsonStops.getJSONObject(i);
      Stop stop = stop(jsonStop);
      stops.add(stop);
    }
    return stops;
  }

  public Stop stop(JSONObject jsonStop) throws JSONException {
    Stop stop = new Stop(jsonStop.getString("code"));
    stop.setName(jsonStop.getString("name"));
    stop.setLatLng(new LatLng(jsonStop.getDouble("latitude"), jsonStop.getDouble("longitude")));
    FavoriteStop favoriteStop = new Select()
        .from(FavoriteStop.class)
        .where("code = ?", stop.getCode())
        .executeSingle();
    if (favoriteStop != null) {
      stop.setFavorited(favoriteStop.isFavorited());
    }
    return stop;
  }
}
