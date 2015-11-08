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

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.bt4vt.R;
import com.bt4vt.repository.TransitRepository;
import com.bt4vt.repository.domain.Departure;
import com.bt4vt.repository.domain.NextDeparture;
import com.bt4vt.repository.domain.Route;
import com.bt4vt.repository.domain.Stop;
import com.bt4vt.repository.exception.TransitRepositoryException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Fetches departures on a separate thread.
 *
 * @author Ben Sechrist
 */
public class DepartureAsyncTask extends AsyncTask<Void, Void, List<Departure>> {

  private static final String TAG = "DepartureAsyncTask";

  private final TransitRepository transitRepository;
  private final Stop stop;
  private final Route route;
  private final AsyncCallback<List<Departure>> callback;
  private final Context context;

  public DepartureAsyncTask(TransitRepository transitRepository, Stop stop, Route route,
                            AsyncCallback<List<Departure>> callback, Context context) {
    this.transitRepository = transitRepository;
    this.stop = stop;
    this.route = route;
    this.callback = callback;
    this.context = context;
  }

  @Override
  protected List<Departure> doInBackground(Void... params) {
    try {
      List<Departure> departures = new ArrayList<>();
      int MAX_DEPARTURE_LOOKAHEAD_MINS = context.getResources().getInteger(R.integer.max_departure_lookahead_mins);
      for (NextDeparture nd : transitRepository.getNextDepartures(stop, route)) {
        for (Departure d : nd.getDepartures()) {
          if (isBefore(MAX_DEPARTURE_LOOKAHEAD_MINS, d)) {
            departures.add(d);
          }
        }
      }
      Collections.sort(departures);
      Log.d(TAG, departures.toString());
      return departures;
    } catch (final TransitRepositoryException e) {
      new Handler(Looper.getMainLooper()).post(new CallbackExceptionRunnable(callback, e));
      return null;
    }
  }

  private boolean isBefore(int MAX_DEPARTURE_LOOKAHEAD_MINS, Departure d) {
    return d.getDepartureTime().before(
        new Date(new Date().getTime() +
            TimeUnit.MILLISECONDS.convert(MAX_DEPARTURE_LOOKAHEAD_MINS, TimeUnit.MINUTES)));
  }

  @Override
  protected void onPostExecute(List<Departure> departures) {
    if (departures != null) {
      callback.onSuccess(departures);
    }
  }
}
