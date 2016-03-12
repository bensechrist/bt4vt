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

import com.bt4vt.repository.domain.Route;
import com.bt4vt.repository.exception.FetchException;
import com.google.inject.Singleton;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Fetches routes from BT4U.
 *
 * @author Ben Sechrist
 */
@Singleton
public class RouteFetcher extends Fetcher {

  private static final String BT_ROUTE_STOP_PATH = "http://216.252.195.248/webservices/bt4u_webservice.asmx/GetScheduledRoutes?stopCode=";

  /**
   * Returns a list of all routes that are active.
   * @return a list of routes
   * @throws FetchException Non serious error occurs fetching routes
   */
  public List<Route> getAll() throws FetchException {
    try {
      return fetch(new URL(BT_ROUTE_STOP_PATH)).routes;
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Returns a list of all routes that service the given <code>stopCode</code>.
   * @param stopCode the stop code
   * @return list of routes
   * @throws FetchException Non serious error occurs fetching routes
   */
  public List<Route> get(int stopCode) throws FetchException {
    try {
      URL url = new URL(BT_ROUTE_STOP_PATH + String.valueOf(stopCode));
      return fetch(url).routes;
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

}
