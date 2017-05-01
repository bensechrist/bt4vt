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

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bt4vt.R;
import com.google.inject.Singleton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import roboguice.inject.InjectResource;

/**
 * Creates {@link com.android.volley.Request} objects.
 *
 * @author Ben Sechrist
 */
@Singleton
class RequestFactory {

  @InjectResource(R.string.bt4u_base_url)
  String BT4U_BASE_URL;

  @InjectResource(R.string.bt4u_api_header_key)
  private String API_HEADER_KEY;

  @InjectResource(R.string.bt4u_api_key)
  private String API_KEY;

  private final String BT4U_BUS_URI = "buses?route=%s";

  private final String BT4U_DEPARTURE_URI = "departures?route=%s&stopCode=%s";

  private final String BT4U_ROUTE_URI = "routes/%s";

  private final String BT4U_STOP_URI = "stops/%s";

  private URI getUrl(String uri) throws URISyntaxException {
    return new URI(BT4U_BASE_URL).resolve(uri);
  }

  private Map<String, String> getBT4VTHeaders() {
    Map<String, String> headers = new HashMap<>();
    headers.put(API_HEADER_KEY, API_KEY);
    return headers;
  }

  private class BT4VTArrayRequest extends JsonArrayRequest {
    BT4VTArrayRequest(String url, Response.Listener<JSONArray> listener,
                      Response.ErrorListener errorListener) {
      super(url, listener, errorListener);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
      return getBT4VTHeaders();
    }
  }

  private class BT4VTRequest extends JsonObjectRequest {
    BT4VTRequest(String url, JSONObject jsonRequest, Response.Listener<JSONObject> listener,
                 Response.ErrorListener errorListener) {
      super(url, jsonRequest, listener, errorListener);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
      return getBT4VTHeaders();
    }
  }

  public JsonArrayRequest buses(String route, Response.Listener<JSONArray> listener,
                                Response.ErrorListener errorListener) throws URISyntaxException {
    try {
      URI url = getUrl(String.format(BT4U_BUS_URI, URLEncoder.encode(route, "UTF-8")));
      return new BT4VTArrayRequest(url.toString(), listener, errorListener);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  public JsonArrayRequest departures(String route, String stopCode,
                                     Response.Listener<JSONArray> listener,
                                     Response.ErrorListener errorListener)
      throws URISyntaxException {
    try {
      URI url = getUrl(String.format(BT4U_DEPARTURE_URI, URLEncoder.encode(route, "UTF-8"),
          URLEncoder.encode(stopCode, "UTF-8")));
      return new BT4VTArrayRequest(url.toString(), listener, errorListener);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  public JsonArrayRequest routes(Response.Listener<JSONArray> listener,
                                 Response.ErrorListener errorListener) throws URISyntaxException {
    URI url = getUrl(String.format(BT4U_ROUTE_URI, ""));
    return new BT4VTArrayRequest(url.toString(), listener, errorListener);
  }

  public JsonObjectRequest route(String shortName, Response.Listener<JSONObject> listener,
                                 Response.ErrorListener errorListener) throws URISyntaxException {
    URI url = getUrl(String.format(BT4U_ROUTE_URI, shortName));
    return new BT4VTRequest(url.toString(), null, listener, errorListener);
  }

  public JsonArrayRequest stops(Response.Listener<JSONArray> listener,
                                Response.ErrorListener errorListener) throws URISyntaxException {
    URI url = getUrl(String.format(BT4U_STOP_URI, ""));
    return new BT4VTArrayRequest(url.toString(), listener, errorListener);
  }

  public JsonObjectRequest stop(String stopCode, Response.Listener<JSONObject> listener,
                                Response.ErrorListener errorListener) throws URISyntaxException {
    URI url = getUrl(String.format(BT4U_STOP_URI, stopCode));
    return new BT4VTRequest(url.toString(), null, listener, errorListener);
  }
}
