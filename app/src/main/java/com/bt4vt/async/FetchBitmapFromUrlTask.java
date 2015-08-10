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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Fetches an image from a given URL and converts it into a Bitmap.
 *
 * @author Ben Sechrist
 */
public class FetchBitmapFromUrlTask extends AsyncTask<String, Void, Bitmap> {

  private final AsyncCallback<Bitmap> callback;

  public FetchBitmapFromUrlTask(AsyncCallback<Bitmap> callback) {
    this.callback = callback;
  }

  @Override
  protected Bitmap doInBackground(String... params) {
    Bitmap bm = null;
    try {
      URL aURL = new URL(params[0]);
      URLConnection conn = aURL.openConnection();
      conn.connect();
      InputStream is = conn.getInputStream();
      BufferedInputStream bis = null;
      try {
        bis = new BufferedInputStream(is);
        bm = BitmapFactory.decodeStream(bis);
      } finally {
        if (bis != null) {
          bis.close();
        }
        is.close();
      }
    } catch (IOException e) {
      callback.onException(e);
    }
    return bm;
  }

  @Override
  protected void onPostExecute(Bitmap bitmap) {
    if (bitmap != null) {
      callback.onSuccess(bitmap);
    }
  }
}
