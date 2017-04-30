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

import com.bt4vt.repository.model.StopModelImpl;
import com.google.android.gms.maps.model.LatLng;

/**
 * BT4U stop information.
 *
 * @author Ben Sechrist
 */
public class Stop {

  private String code;

  private String name;

  private LatLng latLng;

  public Stop(String code) {
    this.code = code;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public LatLng getLatLng() {
    return latLng;
  }

  public void setLatLng(LatLng latLng) {
    this.latLng = latLng;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Stop stop = (Stop) o;

    return code.equals(stop.code);

  }

  @Override
  public int hashCode() {
    return code.hashCode();
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
