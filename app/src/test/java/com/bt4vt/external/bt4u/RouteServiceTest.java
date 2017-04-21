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
}
