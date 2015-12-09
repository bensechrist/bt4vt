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

package com.bt4vt.repository.model;

import java.util.Date;

/**
 * Transit departure model.
 *
 * @author Ben Sechrist
 */
public interface DepartureModel extends Comparable<DepartureModel> {

  /**
   * Returns the short route name.
   * @return the short route name
   */
  String getRouteShortName();

  /**
   * Returns the full route name.
   * @return the route name
   */
  String getRouteName();

  /**
   * Returns the stop name.
   * @return the stop name
   */
  String getStopName();

  /**
   * Returns the textual representation of the departure time.
   * @return the departure time
   */
  String getTextDepartureTime();

  /**
   * Returns the departure time.
   * @return the departure time
   */
  Date getDepartureTime();
}
