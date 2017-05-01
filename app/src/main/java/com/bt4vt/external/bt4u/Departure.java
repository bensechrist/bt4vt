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

import java.util.List;

/**
 * BT4U bus information.
 *
 * @author Ben Sechrist
 */
public class Departure {

  private String routeName;

  private List<String> departures;

  public Departure(String routeName) {
    this.routeName = routeName;
  }

  public String getRouteName() {
    return routeName;
  }

  public void setRouteName(String routeName) {
    this.routeName = routeName;
  }

  public List<String> getDepartures() {
    return departures;
  }

  public void setDepartures(List<String> departures) {
    this.departures = departures;
  }

  @Override
  public String toString() {
    return "Departure{" +
        "routeName='" + routeName + '\'' +
        ", departures=" + departures +
        '}';
  }
}
