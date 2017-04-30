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

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.bt4vt.R;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.json.JSONArray;
import org.json.JSONException;

import java.net.MalformedURLException;
import java.net.URL;

import roboguice.inject.InjectResource;

/**
 * Service to get current stop information.
 *
 * @author Ben Sechrist
 */
@Singleton
public class StopService {

  @InjectResource(R.string.bt4u_base_url)
  String BT4U_BASE_URL;

  private final String BT4U_STOP_URI = "/stops/%s";

  @Inject
  private RequestService requestService;

  @Inject
  private StopFactory stopFactory;

  public void get(String stopCode, final Response.Listener<Stop> listener,
                  final Response.ExceptionListener exceptionListener) {
    try {
      URL baseUrl = new URL(BT4U_BASE_URL);
      URL url = new URL(baseUrl, String.format(BT4U_STOP_URI, stopCode));
      JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url.toExternalForm(),
          null, new com.android.volley.Response.Listener<JSONArray>() {
        @Override
        public void onResponse(JSONArray response) {
          try {
            listener.onResult(stopFactory.stop(response));
          } catch (JSONException e) {
            exceptionListener.onException(e);
          }
        }
      }, new com.android.volley.Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
          exceptionListener.onException(error);
        }
      });
      requestService.addToRequestQueue(request);
    } catch (MalformedURLException e) {
      exceptionListener.onException(e);
    }
  }
}
