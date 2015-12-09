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

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.io.Serializable;

/**
 * Transit route.
 *
 * @author Ben Sechrist
 */
@Root(name = "CurrentRoutes")
public class Route implements Serializable {

  @Element(name = "RouteName")
  private String name;

  @Element(name = "RouteShortName")
  private String shortName;

  public String getName() {
    return name;
  }

  public String getShortName() {
    return shortName;
  }

  @Override
  public int hashCode() {
    if (getName() != null) {
      return getName().hashCode();
    }
    return 0;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) return true;
    if (!(o instanceof Route)) return false;
    Route that = (Route) o;
    return !(this.getName() == null || that.getName() == null) && this.getName().equals(that.getName());
  }
}
