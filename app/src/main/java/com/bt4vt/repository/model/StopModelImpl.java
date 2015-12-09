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

/**
 * Implementation of {@link StopModel}.
 *
 * @author Ben Sechrist
 */
public class StopModelImpl implements StopModel {

  private String name;
  private int code;
  private double latitude;
  private double longitude;
  private String routePattern;
  private Integer routePatternColor;

  public StopModelImpl(String name, int code) {
    this.name = name;
    this.code = code;
  }

  public StopModelImpl() {
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public int getCode() {
    return code;
  }

  @Override
  public double getLatitude() {
    return latitude;
  }

  public void setLatitude(double latitude) {
    this.latitude = latitude;
  }

  @Override
  public double getLongitude() {
    return longitude;
  }

  public void setLongitude(double longitude) {
    this.longitude = longitude;
  }

  public void setLatLng(LatLng latLng) {
    this.latitude = latLng.latitude;
    this.longitude = latLng.longitude;
  }

  @Override
  public String getRoutePattern() {
    return routePattern;
  }

  public void setRoutePattern(String routePattern) {
    this.routePattern = routePattern;
  }

  @Override
  public Integer getRoutePatternColor() {
    return routePatternColor;
  }

  public void setRoutePatternColor(Integer routePatternColor) {
    this.routePatternColor = routePatternColor;
  }

  @Override
  public int hashCode() {
    return Integer.valueOf(getCode()).hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) return true;
    if (!(o instanceof StopModel)) return false;
    StopModel that = (StopModel) o;
    return Integer.valueOf(this.getCode()).equals(that.getCode());
  }

  @Override
  public String toString() {
    return String.format("%s (#%d)", getName(), getCode());
  }

  public static StopModelImpl valueOf(String string) {
    String[] split = string.split(" \\(#");
    if (split.length != 2) {
      throw new IllegalArgumentException("String improperly formatted: " + string);
    }
    return new StopModelImpl(split[0], Integer.valueOf(split[1].substring(0, split[1].length()-1)));
  }
}
