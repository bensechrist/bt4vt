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

import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Tests the {@link BusService}.
 *
 * @author Ben Sechrist
 */
@RunWith(MockitoJUnitRunner.class)
public class BusServiceTest {

  private static final String BASE_URL = "http://base-url";

  @Mock
  private RequestService requestService;

  @Mock
  private BusFactory busFactory;

  @Mock
  private RequestFactory requestFactory;

  @Mock
  private JsonArrayRequest jsonArrayRequest;

  @Mock
  private com.bt4vt.external.bt4u.Response.Listener listener;

  @Mock
  private com.bt4vt.external.bt4u.Response.ExceptionListener exceptionListener;

  @InjectMocks
  private BusService busService;

  @Before
  public void injectBaseUrl() {
    requestFactory.BT4U_BASE_URL = BASE_URL;
  }

  @Test
  public void testGetAll() throws Exception {
    doReturn(jsonArrayRequest).when(requestFactory).buses((String) eq(null),
        any(Response.Listener.class), any(Response.ErrorListener.class));

    busService.getAll(listener, exceptionListener);

    verify(requestFactory, times(1)).buses((String) eq(null), any(Response.Listener.class),
        any(Response.ErrorListener.class));
    verify(requestService, times(1)).addToRequestQueue(jsonArrayRequest);
  }
}
