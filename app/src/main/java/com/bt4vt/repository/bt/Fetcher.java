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

package com.bt4vt.repository.bt;

import com.bt4vt.repository.domain.DocumentElement;
import com.bt4vt.repository.exception.FetchException;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;

/**
 * Fetcher Super Class.
 *
 * @author Ben Sechrist
 */
public class Fetcher {

  protected final Serializer serializer = new Persister(new AnnotationStrategy());

  protected DocumentElement fetch(URL url) throws FetchException {
    return fetch(url, DocumentElement.class);
  }

  protected <T> T fetch(URL url, Class<T> t) throws FetchException {
    try {
      return serializer.read(t, fetchResponse(url));
    } catch (FetchException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected String fetchResponse(URL url) throws FetchException {
    HttpURLConnection conn = null;
    try {
      conn = (HttpURLConnection) url.openConnection();

      int HttpResult = conn.getResponseCode();
      if (HttpResult == HttpURLConnection.HTTP_OK) {
        return getResponse(conn.getInputStream());
      } else {
        // Quietly fail and log exception
        throw new FetchException(
            new Exception(String.format("%d error fetching %s\n%s",
                HttpResult, url.toString(), conn.getResponseMessage())));
      }
    } catch (FetchException e) {
      throw e;
    } catch (IOException e) {
      throw new FetchException(e);
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      if (conn != null) {
        conn.disconnect();
      }
    }
  }

  private String getResponse(InputStream in) throws FetchException {
    StringBuilder sb = new StringBuilder();
    String line;
    try {
      BufferedReader br = new BufferedReader(new InputStreamReader(in, "utf-8"));
      while ((line = br.readLine()) != null) {
        sb.append(line);
      }
      br.close();
      return sb.toString();
    } catch (SocketException e) {
      // Most likely a network error
      throw new FetchException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
