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

package com.bt4vt.repository.domain;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Transit bus.
 *
 * @author Ben Sechrist
 */
public class Bus {

  private long id;

  private int direction;

  private boolean isTripper;

  private int lastStopCode;

  private String lastStopName;

  private LatLng latLng;

  private int passengers;

  private String fullRouteName;

  private String routeShortName;

  private Date timestamp;

  private Bus() {}

  public long getId() {
    return id;
  }

  private void setId(long id) {
    this.id = id;
  }

  public int getDirection() {
    return direction;
  }

  private void setDirection(int direction) {
    this.direction = direction;
  }

  public boolean isTripper() {
    return isTripper;
  }

  private void setIsTripper(boolean isTripper) {
    this.isTripper = isTripper;
  }

  public int getLastStopCode() {
    return lastStopCode;
  }

  private void setLastStopCode(int lastStopCode) {
    this.lastStopCode = lastStopCode;
  }

  public String getLastStopName() {
    return lastStopName;
  }

  private void setLastStopName(String lastStopName) {
    this.lastStopName = lastStopName;
  }

  public LatLng getLatLng() {
    return latLng;
  }

  private void setLatLng(LatLng latLng) {
    this.latLng = latLng;
  }

  public int getPassengers() {
    return passengers;
  }

  private void setPassengers(int passengers) {
    this.passengers = passengers;
  }

  public String getFullRouteName() {
    return fullRouteName;
  }

  private void setFullRouteName(String fullRouteName) {
    this.fullRouteName = fullRouteName;
  }

  public String getRouteShortName() {
    return routeShortName;
  }

  private void setRouteShortName(String routeShortName) {
    this.routeShortName = routeShortName;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  private void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  @Override
  public int hashCode() {
    return Long.valueOf(id).hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Bus)) return false;
    Bus that = (Bus) o;
    return Long.valueOf(this.id).equals(that.id);
  }

  public static Bus valueOf(JSONObject obj) throws JSONException {
    Bus bus = new Bus();
    bus.setId(obj.getLong("busId"));
    bus.setDirection(obj.getInt("direction"));
    bus.setIsTripper(obj.getBoolean("isTripper"));
    bus.setLastStopCode(obj.getInt("lastStopCode"));
    bus.setLastStopName(obj.getString("lastStopName"));
    bus.setLatLng(new LatLng(obj.getDouble("latitude"), obj.getDouble("longitude")));
    bus.setPassengers(obj.getInt("passengers"));
    bus.setFullRouteName(obj.getString("pattern"));
    bus.setRouteShortName(obj.getString("route"));
    bus.setTimestamp(new Date(obj.getLong("timestamp")));
    return bus;
  }
}
