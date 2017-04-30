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

import com.google.android.gms.maps.model.LatLng;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Creates {@link Stop} objects.
 *
 * @author Ben Sechrist
 */
@Singleton
class StopFactory {

  private static final Pattern HEADER_PATTERN =
      Pattern.compile(".*\\\\(#(.{4})\\\\)", Pattern.CASE_INSENSITIVE);

  public Stop stop(JSONArray response) throws JSONException {
    JSONObject json = response.getJSONObject(0);
    String stopDetailsString = json.getString("busStopDetailsHtml");
    Document stopDetailsDocument = Jsoup.parse(stopDetailsString);
    Element header = stopDetailsDocument.getElementsByTag("h2").first();
    Matcher matcher = HEADER_PATTERN.matcher(header.text());
    Stop stop = new Stop(matcher.group());
    stop.setName(json.getString("stopName"));
    stop.setLatLng(new LatLng(json.getDouble("stopLatitude"), json.getDouble("stopLongitude")));
    return stop;
  }
}
