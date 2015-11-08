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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Transit bus departure.
 *
 * @author Ben Sechrist
 */
public class Departure implements Comparable<Departure> {

  private static final String TAG = "Departure";
  private static final SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a", Locale.US);

  private String shortRouteName;

  private Date departureTime;

  public String getShortRouteName() {
    return shortRouteName;
  }

  public Date getDepartureTime() {
    return departureTime;
  }

  public String getTextDepartureTime() {
    return dateFormat.format(departureTime);
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

  @Override
  public String toString() {
    return String.format("%s: %s", getShortRouteName(), getTextDepartureTime());
  }

  public static Departure valueOf(String s) {
    String[] split = s.split(":", 2);
    if (split.length != 2) {
      throw new IllegalArgumentException("String not properly formatted: " + s);
    }
    Departure departure = new Departure();
    departure.shortRouteName = split[0];
    String timeString = split[1];
    String[] timeSplit = timeString.trim().split("[\\ :]");
    Calendar dTime = Calendar.getInstance();
    dTime.set(Calendar.HOUR, Integer.parseInt(timeSplit[0]) % 12); // This is to set 12 to 0 as there is no 12th hour in Calendar
    dTime.set(Calendar.MINUTE, Integer.parseInt(timeSplit[1]));
    dTime.set(Calendar.AM_PM, (timeSplit[2].contains("AM")) ? Calendar.AM : Calendar.PM);
    if (dTime.before(Calendar.getInstance())) {
      dTime.add(Calendar.DAY_OF_YEAR, 1);
    }
    departure.departureTime = dTime.getTime();
    return departure;
  }

  @Override
  public int compareTo(Departure another) {
    return this.departureTime.compareTo(another.departureTime);
  }
}
