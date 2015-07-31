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

/**
 * Transit stop departure
 *
 * @author Ben Sechrist
 */
@Root(name = "NextDepartures")
public class NextDeparture {

  @Element(name = "RouteName")
  private String routeName;

  @Element(name = "AdjustedDepartureTime_TripNotes")
  private String notes;

  public String getRouteName() {
    return routeName;
  }



  @Override
  public int hashCode() {
    String[] array = new String[]{routeName, notes};
    int hashCode = 1;
    for (Object element : array) {
      int elementHashCode;

      if (element == null) {
        elementHashCode = 0;
      } else {
        elementHashCode = (element).hashCode();
      }
      hashCode = 31 * hashCode + elementHashCode;
    }
    return hashCode;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) return true;
    if (!(o instanceof NextDeparture)) return false;
    NextDeparture that = (NextDeparture) o;
    return this.routeName.equals(that.routeName) &&
        this.notes.equals(that.notes);
  }

  @Override
  public String toString() {
    return String.format("%s - %s", routeName, notes);
  }
}
