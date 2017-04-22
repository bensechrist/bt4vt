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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import okhttp3.Call;
import okhttp3.Request;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Tests the {@link RouteService}
 *
 * @author Ben Sechrist
 */
@RunWith(MockitoJUnitRunner.class)
public class RouteServiceTest {

  private static final String ROUTE_URL = "route-url";

  @Mock
  private HttpClient httpClient;

  @Mock
  private Request.Builder requestBuilder;

  @Mock
  private Call call;

  @InjectMocks
  private RouteService routeService;

  private Request request;

  @Before
  public void injectBaseUrl() {
    routeService.BT4U_ROUTE_URL = ROUTE_URL;
  }

  @Test
  public void testGetAll() throws Exception {
    doReturn(requestBuilder).when(requestBuilder).url(ROUTE_URL);
    doReturn(request).when(requestBuilder).build();
    doReturn(call).when(httpClient).newCall(request);

    routeService.getAll(null);

    verify(requestBuilder, times(1)).url(ROUTE_URL);
    verify(requestBuilder, times(1)).build();
    verify(httpClient, times(1)).newCall(request);
    verify(call, times(1)).enqueue(any(okhttp3.Callback.class));
  }

  @Test
  public void testGet() throws Exception {
    doReturn(requestBuilder).when(requestBuilder).url(any(String.class));
    doReturn(request).when(requestBuilder).build();
    doReturn(call).when(httpClient).newCall(request);

    String shortName = "test";

    routeService.get(shortName, null);

    verify(requestBuilder, times(1)).url(ROUTE_URL + '/' + shortName);
    verify(requestBuilder, times(1)).build();
    verify(httpClient, times(1)).newCall(request);
    verify(call, times(1)).enqueue(any(okhttp3.Callback.class));
  }
}
