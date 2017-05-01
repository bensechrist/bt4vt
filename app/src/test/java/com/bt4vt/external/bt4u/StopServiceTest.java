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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Tests the {@link StopService}.
 *
 * @author Ben Sechrist
 */
@RunWith(MockitoJUnitRunner.class)
public class StopServiceTest {

  private static final String BASE_URL = "http://base-url";

  @Mock
  private RequestService requestService;

  @Mock
  private StopFactory stopFactory;

  @Mock
  private RequestFactory requestFactory;

  @Mock
  private JsonArrayRequest jsonArrayRequest;

  @Mock
  private com.bt4vt.external.bt4u.Response.Listener listener;

  @Mock
  private com.bt4vt.external.bt4u.Response.ExceptionListener exceptionListener;

  @InjectMocks
  private StopService stopService;

  @Before
  public void injectBaseUrl() {
    requestFactory.BT4U_BASE_URL = BASE_URL;
  }

  @Test
  public void testGetAll() throws Exception {
    doReturn(jsonArrayRequest).when(requestFactory).stops(any(Response.Listener.class),
        any(Response.ErrorListener.class));

    stopService.getAll(listener, exceptionListener);

    verify(requestFactory, times(1)).stops(any(Response.Listener.class),
        any(Response.ErrorListener.class));
    verify(requestService, times(1)).addToRequestQueue(jsonArrayRequest);
  }
}
