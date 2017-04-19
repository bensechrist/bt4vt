package com.bt4vt.external.bt4u;

/**
 * Callback for asynchronous requests to BT4U server.
 *
 * @author Ben Sechrist
 */
public interface Callback<T> {

  /**
   * Called when any response is returned from the originating request.
   * @param t the result
   */
  void onResult(T t);

  /**
   * Called when the status code returned from a BT4U request is unexpected.
   * @param statusCode the HTTP status code
   * @param body the body of the HTTP response
   */
  void onFail(int statusCode, String body);

  /**
   * Called when an HTTP request throws an exception (i.e. Timeout).
   * @param exception the exception that occurred
   */
  void onException(Exception exception);
}
