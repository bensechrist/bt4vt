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

package com.bt4vt.async;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import com.bt4vt.repository.TransitRepository;
import com.bt4vt.repository.domain.Route;
import com.bt4vt.repository.exception.TransitRepositoryException;

import java.util.List;

/**
 * Fetches routes on a separate thread.
 *
 * @author Ben Sechrist
 */
public class RouteAsyncTask extends AsyncTask<Void, Integer, List<Route>> {

  private final TransitRepository transitRepository;
  private final AsyncCallback<List<Route>> callback;

  public RouteAsyncTask(TransitRepository transitRepository, AsyncCallback<List<Route>> callback) {
    this.transitRepository = transitRepository;
    this.callback = callback;
  }

  @Override
  protected List<Route> doInBackground(Void... params) {
    try {
      return transitRepository.getRoutes();
    } catch (final TransitRepositoryException e) {
      new Handler(Looper.getMainLooper()).post(new CallbackExceptionRunnable(callback, e));
      return null;
    }
  }

  @Override
  protected void onPostExecute(List<Route> routes) {
    if (routes != null) {
      callback.onSuccess(routes);
    }
  }
}
