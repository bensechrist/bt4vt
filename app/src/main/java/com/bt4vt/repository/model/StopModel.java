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

/**
 * Transit stop model.
 *
 * @author Ben Sechrist
 */
public interface StopModel {

  /**
   * Returns the stop name.
   * @return the stop name
   */
  String getName();

  /**
   * Returns the stop code.
   * @return the stop code
   */
  int getCode();

  /**
   * Returns the stop latitude.
   * @return the latitude
   */
  double getLatitude();

  /**
   * Returns the stop longitude.
   * @return the longitude
   */
  double getLongitude();

  /**
   * Returns the route pattern or null.
   * @return the route pattern
   */
  String getRoutePattern();

  /**
   * Returns the route pattern color in hex or null.
   * @return the route color
   */
  Integer getRoutePatternColor();
}
