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

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * BT4U route information.
 *
 * @author Ben Sechrist
 */
public class Route implements Comparable<Route> {

  private String shortName;

  private String fullName;

  private String plot;

  private Integer color;

  private List<Stop> stops = new ArrayList<>();

  public Route(String shortName) {
    this.shortName = shortName;
  }

  public String getShortName() {
    return shortName;
  }

  public void setShortName(String shortName) {
    this.shortName = shortName;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public String getPlot() {
    return plot;
  }

  public void setPlot(String plot) {
    this.plot = plot;
  }

  public Integer getColor() {
    return color;
  }

  public void setColor(Integer color) {
    this.color = color;
  }

  public List<Stop> getStops() {
    return stops;
  }

  public void setStops(List<Stop> stops) {
    this.stops = stops;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Route route = (Route) o;

    return shortName.equals(route.shortName);

  }

  @Override
  public int hashCode() {
    return shortName.hashCode();
  }

  @Override
  public int compareTo(@NonNull Route another) {
    return this.getFullName().compareTo(another.getFullName());
  }

  @Override
  public String toString() {
    return String.format("%s - %s", shortName, fullName);
  }
}
