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

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Implementation of {@link BusModel}.
 *
 * @author Ben Sechrist
 */
public class BusModelImpl implements BusModel {

  private long id;
  private LatLng latLng;
  private int passengers;
  private int direction;
  private String routeShortName;
  private Date lastUpdated;

  @Override
  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  @Override
  public LatLng getLatLng() {
    return latLng;
  }

  public void setLatLng(LatLng latLng) {
    this.latLng = latLng;
  }

  @Override
  public int getPassengers() {
    return passengers;
  }

  public void setPassengers(int passengers) {
    this.passengers = passengers;
  }

  @Override
  public int getDirection() {
    return direction;
  }

  public void setDirection(int direction) {
    this.direction = direction;
  }

  @Override
  public String getRouteShortName() {
    return routeShortName;
  }

  public void setRouteShortName(String routeShortName) {
    this.routeShortName = routeShortName;
  }

  @Override
  public String getLastUpdated() {
    return SimpleDateFormat
        .getTimeInstance()
        .format(lastUpdated);
  }

  public void setLastUpdated(Date lastUpdated) {
    this.lastUpdated = lastUpdated;
  }
}
