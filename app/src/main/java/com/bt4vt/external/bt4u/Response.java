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

/**
 * Callback for asynchronous requests to BT4U server.
 *
 * @author Ben Sechrist
 */
public class Response<T> {

  private T result;
  private Exception exception;

  public interface Listener<T> {
    /**
     * Called when any response is returned from the originating request.
     *
     * @param result the result
     */
    void onResult(T result);
  }

  public interface ExceptionListener {
    /**
     * Called when an HTTP request throws an exception (i.e. Timeout)
     * or it returns an unexpected status code.
     *
     * @param e the exception that occurred
     */
    void onException(Exception e);
  }
}
