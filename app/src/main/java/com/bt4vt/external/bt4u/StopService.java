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

import com.android.volley.VolleyError;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.List;

/**
 * Service to get current stop information.
 *
 * @author Ben Sechrist
 */
@Singleton
public class StopService {

  @Inject
  private RequestService requestService;

  @Inject
  private RequestFactory requestFactory;

  @Inject
  private StopFactory stopFactory;

  public void getAll(final Response.Listener<List<Stop>> listener,
                     final Response.ExceptionListener exceptionListener) {
    try {
      requestService.addToRequestQueue(requestFactory.stops(
          new com.android.volley.Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
              try {
                listener.onResult(stopFactory.stops(response));
              } catch (JSONException e) {
                exceptionListener.onException(e);
              }
            }
          }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
              exceptionListener.onException(error);
            }
          }));
    } catch (URISyntaxException e) {
      exceptionListener.onException(e);
    }
  }

  public void get(String stopCode, final Response.Listener<Stop> listener,
                  final Response.ExceptionListener exceptionListener) {
    try {
      requestService.addToRequestQueue(requestFactory.stop(stopCode,
          new com.android.volley.Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
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
          }));
    } catch (URISyntaxException e) {
      exceptionListener.onException(e);
    }
  }
}
