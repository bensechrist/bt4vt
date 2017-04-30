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

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.inject.Singleton;

import org.json.JSONArray;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import roboguice.inject.InjectResource;

/**
 * Creates {@link com.android.volley.Request} objects.
 *
 * @author Ben Sechrist
 */
@Singleton
class RequestFactory {

  @InjectResource(com.bt4vt.R.string.bt4u_base_url)
  String BT4U_BASE_URL;

  private final String BT4U_ROUTE_URI = "/routes/%s";

  private final String BT4U_BUS_URI = "/livemap";

  private URL getUrl(String uri) throws MalformedURLException {
    return new URL(new URL(BT4U_BASE_URL), uri);
  }

  public Request<List<Route>> routes(Response.Listener<List<Route>> listener,
                                     Response.ErrorListener errorListener)
      throws MalformedURLException {
    URL url = getUrl(String.format(BT4U_ROUTE_URI, ""));
    return (new Request<List<Route>>(Request.Method.GET, url.toExternalForm(), null, listener,
        errorListener) {
      @Override
      protected Response<List<Route>> parseNetworkResponse(NetworkResponse response) {
        return null;
      }

      @Override
      protected void deliverResponse(List<Route> response) {

      }
    });
  }

  public JsonArrayRequest route(String shortName, Response.Listener<JSONArray> listener,
                                Response.ErrorListener errorListener) throws MalformedURLException {
    URL url = getUrl(String.format(BT4U_ROUTE_URI, shortName));
    return (new JsonArrayRequest(Request.Method.GET, url.toExternalForm(), null, listener,
        errorListener));
  }

  public JsonArrayRequest buses(Response.Listener<JSONArray> listener,
                                Response.ErrorListener errorListener) throws MalformedURLException {
    URL url = getUrl(BT4U_BUS_URI);
    return (new JsonArrayRequest(Request.Method.GET, url.toExternalForm(), null, listener,
        errorListener));
  }
}
