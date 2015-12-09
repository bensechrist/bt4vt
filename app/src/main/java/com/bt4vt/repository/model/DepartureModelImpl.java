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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Implementation of {@link DepartureModel}.
 *
 * @author Ben Sechrist
 */
public class DepartureModelImpl implements DepartureModel {

  private static final SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a", Locale.US);

  private String routeShortName;

  private String routeName;

  private String stopName;

  private Date departureTime;

  @Override
  public String getRouteShortName() {
    return routeShortName;
  }

  public void setRouteShortName(String routeShortName) {
    this.routeShortName = routeShortName;
  }

  @Override
  public String getRouteName() {
    return routeName;
  }

  public void setRouteName(String routeName) {
    this.routeName = routeName;
  }

  @Override
  public String getStopName() {
    return stopName;
  }

  public void setStopName(String stopName) {
    this.stopName = stopName;
  }

  @Override
  public String getTextDepartureTime() {
    return dateFormat.format(departureTime);
  }

  @Override
  public Date getDepartureTime() {
    return departureTime;
  }

  public void setDepartureTime(Date departureTime) {
    this.departureTime = departureTime;
  }

  @Override
  public int compareTo(DepartureModel another) {
    return departureTime.compareTo(((DepartureModelImpl) another).departureTime);
  }

  @Override
  public String toString() {
    return String.format("%s: %s", getRouteName(), getTextDepartureTime());
  }
}
