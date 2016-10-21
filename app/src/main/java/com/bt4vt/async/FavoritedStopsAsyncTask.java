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
import com.bt4vt.repository.exception.TransitRepositoryException;
import com.bt4vt.repository.model.StopModel;

import java.util.List;

/**
 * Fetches favorited bus stops on a separate thread.
 *
 * @author Ben Sechrist
 */
public class FavoritedStopsAsyncTask extends AsyncTask<Void, Integer, List<StopModel>> {

  private final TransitRepository transitRepository;
  private final AsyncCallback<List<StopModel>> callback;

  public FavoritedStopsAsyncTask(TransitRepository transitRepository, AsyncCallback<List<StopModel>> callback) {
    this.transitRepository = transitRepository;
    this.callback = callback;
  }

  @Override
  protected List<StopModel> doInBackground(Void... voids) {
    try {
      return transitRepository.getFavoritedStops();
    } catch (TransitRepositoryException e) {
      new Handler(Looper.getMainLooper()).post(new CallbackExceptionRunnable(callback, e));
      return null;
    }
  }

  @Override
  protected void onPostExecute(List<StopModel> stopModels) {
    if (stopModels != null) {
      callback.onSuccess(stopModels);
    }
  }
}
