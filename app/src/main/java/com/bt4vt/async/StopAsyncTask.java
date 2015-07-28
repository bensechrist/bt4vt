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

import com.bt4vt.repository.TransitRepository;
import com.bt4vt.repository.domain.Route;
import com.bt4vt.repository.domain.Stop;
import com.bt4vt.repository.exception.TransitRepositoryException;

import java.util.List;

/**
 * Fetches bus stops on a separate thread.
 *
 * @author Ben Sechrist
 */
public class StopAsyncTask extends AsyncTask<Route, Integer, List<Stop>> {

  private final TransitRepository transitRepository;
  private final AsyncCallback<List<Stop>> callback;

  public StopAsyncTask(TransitRepository transitRepository, AsyncCallback<List<Stop>> callback) {
    this.transitRepository = transitRepository;
    this.callback = callback;
  }

  @Override
  protected List<Stop> doInBackground(Route... params) {
    try {
      Route route = params[0];
      if (route == null) {
        return transitRepository.getStops();
      } else {
        return transitRepository.getStops(route);
      }
    } catch (TransitRepositoryException e) {
      callback.onException(e);
      return null;
    }
  }

  @Override
  protected void onPostExecute(List<Stop> stops) {
    if (stops != null) {
      callback.onSuccess(stops);
    }
  }
}
