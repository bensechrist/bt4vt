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

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

/**
 * BT4U bus information.
 *
 * @author Ben Sechrist
 */
public class Bus {

  private String id;

  private Route route;

  private LatLng latLng;

  private int direction;

  private boolean isTripper;

  private int passengers;

  private String lastStopCode;

  private String lastStopName;

  private Date timestamp;

  public Bus(String busId) {
    this.id = busId;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Route getRoute() {
    return route;
  }

  public void setRoute(Route route) {
    this.route = route;
  }

  public LatLng getLatLng() {
    return latLng;
  }

  public void setLatLng(LatLng latLng) {
    this.latLng = latLng;
  }

  public int getDirection() {
    return direction;
  }

  public void setDirection(int direction) {
    this.direction = direction;
  }

  public boolean isTripper() {
    return isTripper;
  }

  public void setTripper(boolean tripper) {
    isTripper = tripper;
  }

  public int getPassengers() {
    return passengers;
  }

  public void setPassengers(int passengers) {
    this.passengers = passengers;
  }

  public String getLastStopCode() {
    return lastStopCode;
  }

  public void setLastStopCode(String lastStopCode) {
    this.lastStopCode = lastStopCode;
  }

  public String getLastStopName() {
    return lastStopName;
  }

  public void setLastStopName(String lastStopName) {
    this.lastStopName = lastStopName;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Bus bus = (Bus) o;

    return id.equals(bus.id);

  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
