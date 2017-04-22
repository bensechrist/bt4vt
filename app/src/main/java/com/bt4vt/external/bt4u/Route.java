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

import android.graphics.Color;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * BT4U route information
 *
 * @author Ben Sechrist
 */
class Route {

  private String shortName;

  private String fullName;

  private String plot;

  private Integer color;

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

  public static Route valueOf(Element element) {
    String attrValue = element.attr("data-routes");
    String[] splits = attrValue.split("|");
    if (splits.length != 3) {
      throw new IllegalArgumentException("Unexpected 'data-routes' attribute value " + attrValue);
    }
    Route route = new Route(splits[0]);
    route.setFullName(splits[1]);
    route.setColor(Color.parseColor(String.format("#%s", splits[2])));
    return route;
  }

  public static Route valueOf(JSONObject json) throws JSONException {
    Document document = Jsoup.parse(json.getString("routeDetailsHtml"));
    Element header = document.getElementsByClass("headerBordered").first();
    Element fullNameEl = header.child(0);
    Element shortNameEl = header.child(1);
    Route route = new Route(shortNameEl.text());
    route.setFullName(fullNameEl.text());
    route.setPlot(json.getString("routePlot"));
    route.setColor(Color.parseColor(String.format("#%s", json.getString("routePlotColor"))));
    return route;
  }
}
