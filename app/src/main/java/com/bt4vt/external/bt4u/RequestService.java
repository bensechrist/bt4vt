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

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Handles all requests sent to BT4U.
 *
 * @author Ben Sechrist
 */
@Singleton
public class RequestService {

  private RequestQueue requestQueue;

  private Context context;

  @Inject
  public RequestService(Context context) {
    this.context = context;
  }

  public RequestQueue getRequestQueue() {
    if (requestQueue == null) {
      requestQueue = Volley.newRequestQueue(context.getApplicationContext());
    }
    return requestQueue;
  }

  public <T> void addToRequestQueue(Request<T> request) {
    getRequestQueue().add(request);
  }
}
