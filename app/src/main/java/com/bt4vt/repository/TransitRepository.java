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

import com.bt4vt.repository.domain.Route;
import com.bt4vt.repository.domain.Stop;

import java.util.List;

/**
 * Repository for getting transit information.
 *
 * @author Ben Sechrist
 */
public interface TransitRepository {

  /**
   * Returns a list of all transit routes.
   * @return a list of routes
   */
  List<Route> getRoutes();

  /**
   * Returns a list of all stops for a route.
   * @param route the route to get stops for
   * @return a list of stops
   */
  List<Stop> getStops(Route route);
}
