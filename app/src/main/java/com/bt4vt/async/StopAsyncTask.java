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
