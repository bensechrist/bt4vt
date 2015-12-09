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

import com.bt4vt.repository.domain.Bus;
import com.bt4vt.repository.exception.FetchException;
import com.google.inject.Singleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Fetches buses from BT4U.
 *
 * @author Ben Sechrist
 */
@Singleton
public class BusFetcher extends Fetcher {

  private static final String BT_BUS_PATH = "http://bt4userver.newcitystaging.com/livemap";

  /**
   * Returns a list of all buses.
   * @return list of buses
   * @throws FetchException Non serious error occurs fetching buses
   */
  public List<Bus> getAll() throws FetchException {
    try {
      return readResponse(fetchResponse(new URL(BT_BUS_PATH)), null);
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Returns a list of all buses for a given route <code>shortName</code>.
   * @param shortName the route short name
   * @return list of buses
   * @throws FetchException Non serious error occurs fetching buses
   */
  public List<Bus> get(String shortName) throws FetchException {
    try {
      return readResponse(fetchResponse(new URL(BT_BUS_PATH)), shortName);
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  private List<Bus> readResponse(String response, String shortName) {
    List<Bus> buses = new ArrayList<>();
    if (response.isEmpty()) return buses;
    try {
      JSONArray array = new JSONArray(response).getJSONObject(0)
          .getJSONArray("buses");
      for (int i=0; i<array.length(); i++) {
        JSONObject obj = array.getJSONObject(i);
        Bus bus = Bus.valueOf(obj);
        if (shortName == null || bus.getRouteShortName().equals(shortName))
          buses.add(bus);
      }
      return buses;
    } catch (JSONException e) {
      return buses;
    }
  }

}
