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
import org.simpleframework.xml.convert.Convert;

import java.util.Date;

/**
 * Transit bus departure.
 *
 * @author Ben Sechrist
 */
@Root(name = "NextDepartures")
public class Departure {

  private static final String TAG = "Departure";

  @Element(name = "RouteShortName")
  private String routeShortName;

  @Element(name = "PatternName")
  private String routeDisplayName;

  @Element(name = "StopName")
  private String stopName;

  @Element(name = "AdjustedDepartureTime")
  @Convert(DepartureTimeConverter.class)
  private Date departureTime;

  @Element(name = "StopNotes", required = false)
  private String notes;

  @Element(name = "TripPointID", required = false)
  private String tripPointID;

  @Element(name = "TripNotes", required = false)
  private String tripNotes;

  public String getRouteShortName() {
    return routeShortName;
  }

  public String getRouteName() {
    return routeDisplayName;
  }

  public String getStopName() {
    return stopName;
  }

  public Date getDepartureTime() {
    return departureTime;
  }

  public String getNotes() {
    return notes;
  }

  @Override
  public int hashCode() {
    if (departureTime == null) {
      return 0;
    }
    return departureTime.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) return true;
    if (!(o instanceof Departure)) return false;
    Departure that = (Departure) o;
    return this.departureTime.equals(that.departureTime);
  }
}
