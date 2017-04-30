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

/**
 * Fetches a stop on a separate thread.
 *
 * @author Ben Sechrist
 */
public class StopAsyncTask extends AsyncTask<Void, Integer, StopModel> {

  private final TransitRepository transitRepository;
  private final int stopCode;
  private final AsyncCallback<StopModel> callback;

  public StopAsyncTask(TransitRepository transitRepository, int stopCode, AsyncCallback<StopModel> callback) {
    this.transitRepository = transitRepository;
    this.stopCode = stopCode;
    this.callback = callback;
  }

  @Override
  protected StopModel doInBackground(Void... params) {
    try {
      return transitRepository.getStop(stopCode);
    } catch (TransitRepositoryException e) {
      new Handler(Looper.getMainLooper()).post(new CallbackExceptionRunnable(callback, e));
      return null;
    }
  }

  @Override
  protected void onPostExecute(StopModel stopModel) {
    if (stopModel != null) {
      callback.onSuccess(stopModel);
    }
  }
}
