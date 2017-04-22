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

import com.bt4vt.R;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;
import roboguice.inject.InjectResource;

/**
 * Service to get current route information
 *
 * @author Ben Sechrist
 */
@Singleton
public class RouteService {

  @InjectResource(R.string.bt4u_base_url)
  private String BT4U_BASE_URL;

  String BT4U_ROUTE_URL = BT4U_BASE_URL + "routes";

  @Inject
  private HttpClient client;

  @Inject
  private Request.Builder requestBuilder;

  public void getAll(final Callback<List<Route>> callback) {
    Request request = requestBuilder
        .url(BT4U_ROUTE_URL)
        .build();

    client.newCall(request).enqueue(new okhttp3.Callback() {
      @Override
      public void onFailure(Call call, IOException e) {
        callback.onException(e);
      }

      @Override
      public void onResponse(Call call, Response response) throws IOException {
        try {
          int statusCode = response.code();
          String stringBody = response.body().string();
          if (statusCode == 200) {
            String routeListString = new JSONArray(stringBody).getJSONObject(0).getString("routeListHtml");
            Document document = Jsoup.parse(routeListString);
            Elements elements = document.getElementsByClass("list-group-item");
            List<Route> routes = new ArrayList<>();
            for (Element el : elements) {
              routes.add(Route.valueOf(el));
            }
            callback.onResult(routes);
          } else {
            callback.onFail(statusCode, stringBody);
          }
        } catch (JSONException e) {
          callback.onException(e);
        }
      }
    });
  }

  public void get(String shortName, final Callback<Route> callback) {
    Request request = requestBuilder
        .url(BT4U_ROUTE_URL + '/' + shortName)
        .build();

    client.newCall(request).enqueue(new okhttp3.Callback() {
      @Override
      public void onFailure(Call call, IOException e) {
        callback.onException(e);
      }

      @Override
      public void onResponse(Call call, Response response) throws IOException {
        try {
          int statusCode = response.code();
          String stringBody = response.body().string();
          if (statusCode == 200) {
            JSONObject jsonRoute = new JSONArray(stringBody).getJSONObject(0);
            callback.onResult(Route.valueOf(jsonRoute));
          } else {
            callback.onFail(statusCode, stringBody);
          }
        } catch (JSONException e) {
          callback.onException(e);
        }
      }
    });
  }
}
