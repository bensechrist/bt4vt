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

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.Scopes;

import java.io.IOException;

/**
 * @author Ben Sechrist
 */
public class FetchGoogleTokenTask extends AsyncTask<Void, Void, String> {

  private final Context context;
  private final String email;
  private final AsyncCallback<String> callback;

  public FetchGoogleTokenTask(Context context, String email, AsyncCallback<String> callback) {
    this.context = context;
    this.email = email;
    this.callback = callback;
  }

  @Override
  protected String doInBackground(Void... voids) {
    try {
      String scope = String.format("oauth2:%s", Scopes.PLUS_LOGIN);
      return GoogleAuthUtil.getToken(context, email, scope);
    } catch (IOException | GoogleAuthException e) {
      new Handler(Looper.getMainLooper()).post(new CallbackExceptionRunnable(callback, e));
    }
    return null;
  }

  @Override
  protected void onPostExecute(String token) {
    if (token != null) {
      callback.onSuccess(token);
    }
  }
}
