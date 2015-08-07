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
 * Transit bus.
 *
 * @author Ben Sechrist
 */
@Root(name = "LatestInfoTable")
public class Bus {

  @Element(name = "AgencyVehicleName")
  private Integer id;

  @Element(name = "RouteShortName")
  private String routeShortName;

  @Element(name = "RouteName")
  private String routeName;

  @Element(name = "Latitude")
  private Double latitude;

  @Element(name = "Longitude")
  private Double longitude;

  @Element(name = "BlockID")
  private String blockId;

  @Element(name = "TripID")
  private String tripId;

  @Element(name = "IsTripper")
  private String isTripperString;

  @Element(name = "PatternName")
  private String patternName;

  @Element(name = "PatternPoints")
  private String patternPointsString;

  @Element(name = "PatternColor")
  private String patternColor;

  @Element(name = "TripStartTime", required = false)
  private String tripStartTime;

  @Element(name = "LastStopName")
  private String lastStopName;

  @Element(name = "StopCode")
  private Integer lastStopCode;

  @Element(name = "IsBusAtStop")
  private String isAtStopString;

  @Element(name = "IsTimePoint")
  private String isTimePointString;

  @Element(name = "LatestEvent", required = false)
  private String latestEventTime;

  @Element(name = "LatestRSAEvent", required = false)
  private String latestRSAEvent;

  @Element(name = "Direction", required = false)
  private Integer direction;

  @Element(name = "Speed")
  private Integer speed;

  @Element(name = "TotalCount")
  private Integer passengerLoad;

  public Integer getId() {
    return id;
  }

  public String getRouteShortName() {
    return routeShortName;
  }

  public String getRouteName() {
    return routeName;
  }

  public boolean isTripper() {
    return isTripperString.equalsIgnoreCase("Y");
  }

  public Double getLatitude() {
    return latitude;
  }

  public Double getLongitude() {
    return longitude;
  }

  public Integer getPassengerLoad() {
    return passengerLoad;
  }

  public String[] getPatternPoints() {
    return patternPointsString.split(",");
  }

  public String getPatternColor() {
    return patternColor;
  }

  @Override
  public int hashCode() {
    return (id == null) ? 0 : id.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Bus)) return false;
    Bus that = (Bus) o;
    return !(this.id == null || that.id == null) && this.id.equals(that.id);
  }
}
