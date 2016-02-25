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

import android.support.annotation.NonNull;

/**
 * Implementation of {@link RouteModel}.
 *
 * @author Ben Sechrist
 */
public class RouteModelImpl implements RouteModel {

  private String name;
  private String shortName;

  @Override
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getShortName() {
    return shortName;
  }

  public void setShortName(String shortName) {
    this.shortName = shortName;
  }

  @Override
  public int hashCode() {
    if (getName() != null)
      return getName().hashCode();
    return 0;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) return true;
    if (!(o instanceof RouteModel)) return false;
    RouteModel that = (RouteModel) o;
    return !(this.getName() == null || that.getName() == null) && this.getName().equals(that.getName());
  }

  @Override
  public String toString() {
//    return String.format("%s:%s", getName(), getShortName());
    return getShortName();
  }

  @Override
  public int compareTo(@NonNull RouteModel another) {
    return this.getShortName().compareTo(another.getShortName());
  }
}
