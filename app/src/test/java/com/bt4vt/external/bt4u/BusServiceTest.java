package com.bt4vt.external.bt4u;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Request;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;
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
