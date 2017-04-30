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

import com.google.inject.Singleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates {@link Route} objects.
 *
 * @author Ben Sechrist
 */
@Singleton
public class RouteFactory {

  public List<Route> routes(JSONArray jsonArray) throws JSONException {
    String routeListString = jsonArray.getJSONObject(0).getString("routeListHtml");
    Document routeListDocument = Jsoup.parse(routeListString);
    Elements routeElements = routeListDocument.getElementsByClass("list-group-item");
    List<Route> routes = new ArrayList<>();
    for (Element el : routeElements) {
      String attrValue = el.attr("data-routes");
      String[] splits = attrValue.split("\\|");
      if (splits.length != 3) {
        System.err.println("Unexpected 'data-routes' attribute value " + attrValue);
        continue;
      }
      Route route = new Route(splits[0]);
      route.setFullName(splits[1]);
      route.setColor(Color.parseColor(String.format("#%s", splits[2])));
      routes.add(route);
    }
    return routes;
  }

  public Route route(JSONArray jsonArray) throws JSONException {
    JSONObject json = jsonArray.getJSONObject(0);
    String routeDetailsString = json.getString("routeDetailsHtml");
    Document routeDetailsDocument = Jsoup.parse(routeDetailsString);
    Element header = routeDetailsDocument.getElementsByClass("headerBordered").first();
    Element fullNameEl = header.child(0);
    Element shortNameEl = header.child(1);
    Route route = new Route(shortNameEl.text());
    route.setFullName(fullNameEl.text());
    route.setPlot(json.getString("routePlot"));
    route.setColor(Color.parseColor(String.format("#%s", json.getString("routePlotColor"))));
    return route;
  }
}
