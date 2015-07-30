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

/**
 * Handles calling the callback when an exception occurs.
 *
 * @author Ben Sechrist
 */
public class CallbackExceptionRunnable implements Runnable {

  private final AsyncCallback callback;
  private final Exception exception;

  public CallbackExceptionRunnable(AsyncCallback callback, Exception exception) {
    this.callback = callback;
    this.exception = exception;
  }

  @Override
  public void run() {
    callback.onException(exception);
  }
}
