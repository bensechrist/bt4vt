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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Transit bus departure.
 *
 * @author Ben Sechrist
 */
public class Departure {

  private String shortRouteName;

  private Date departureTime;

  public String getShortRouteName() {
    return shortRouteName;
  }

  public String getDepartureTime() {
    return SimpleDateFormat.getTimeInstance(DateFormat.SHORT).format(departureTime);
  }

  public static Departure valueOf(String s) {
    String[] split = s.split(":", 2);
    if (split.length != 2) {
      throw new IllegalArgumentException("String not properly formatted: " + s);
    }
    Departure departure = new Departure();
    departure.shortRouteName = split[0];
    String timeString = split[1];
    try {
      departure.departureTime = SimpleDateFormat.getTimeInstance(DateFormat.SHORT).parse(timeString);
    } catch (ParseException e) {
      throw new IllegalArgumentException("Error reading time: " + timeString
          + " from string: " + s);
    }
    return departure;
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
