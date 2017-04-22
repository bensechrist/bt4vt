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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Tests the {@link BusService}
 *
 * @author Ben Sechrist
 */
@RunWith(MockitoJUnitRunner.class)
public class BusServiceTest {

  private static final String BUS_URL = "bus-url";

  @Mock
  private HttpClient httpClient;

  @Mock
  private Request.Builder requestBuilder;

  @Mock
  private Call call;

  @InjectMocks
  private BusService busService;

  private Request request;

  @Before
  public void injectBaseUrl() {
    busService.BT4U_BUS_URL = BUS_URL;
  }

  @Test
  public void testGetAll() throws Exception {
    doReturn(requestBuilder).when(requestBuilder).url(BUS_URL);
    doReturn(request).when(requestBuilder).build();
    doReturn(call).when(httpClient).newCall(request);

    busService.getAll(null);

    verify(requestBuilder, times(1)).url(BUS_URL);
    verify(requestBuilder, times(1)).build();
    verify(httpClient, times(1)).newCall(request);
    verify(call, times(1)).enqueue(any(okhttp3.Callback.class));
  }
}
