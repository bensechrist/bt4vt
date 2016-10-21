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

package com.bt4vt.repository;

import com.bt4vt.repository.bt.BusFetcher;
import com.bt4vt.repository.bt.DepartureFetcher;
import com.bt4vt.repository.bt.RouteFetcher;
import com.bt4vt.repository.bt.StopFetcher;
import com.bt4vt.repository.domain.Bus;
import com.bt4vt.repository.domain.Departure;
import com.bt4vt.repository.domain.Route;
import com.bt4vt.repository.domain.Stop;
import com.bt4vt.repository.listener.BusListener;
import com.bt4vt.repository.listener.BusTimerTask;
import com.bt4vt.repository.model.BusModel;
import com.bt4vt.repository.model.BusModelFactory;
import com.bt4vt.repository.model.DepartureModel;
import com.bt4vt.repository.model.DepartureModelFactory;
import com.bt4vt.repository.model.RouteModel;
import com.bt4vt.repository.model.RouteModelFactory;
import com.bt4vt.repository.model.StopModel;
import com.bt4vt.repository.model.StopModelFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertSame;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Tests the {@link HttpBlacksburgTransitRepository}.
 *
 * @author Ben Sechrist
 */
@RunWith(MockitoJUnitRunner.class)
public class HttpBlacksburgTransitRepositoryTest {

  @Mock
  private RouteFetcher routeFetcher;

  @Mock
  private RouteModelFactory routeModelFactory;

  @Mock
  private StopFetcher stopFetcher;

  @Mock
  private StopModelFactory stopModelFactory;

  @Mock
  private DepartureFetcher departureFetcher;

  @Mock
  private DepartureModelFactory departureModelFactory;

  @Mock
  private BusFetcher busFetcher;

  @Mock
  private BusModelFactory busModelFactory;

  @Mock
  private Route route;

  @Mock
  private RouteModel routeModel;

  @Mock
  private Stop stop;

  @Mock
  private StopModel stopModel;

  @Mock
  private Departure departure;

  @Mock
  private DepartureModel departureModel;

  @Mock
  private Bus bus;

  @Mock
  private BusModel busModel;

  @Mock
  private BusTimerTask busTimerTask;

  @InjectMocks
  private HttpBlacksburgTransitRepository repository;

  @Before
  public void setup() throws Exception {
    repository.busTimerTask = busTimerTask;
  }

  @Test
  public void testGetRoutesSuccessfully() throws Exception {
    doReturn(Collections.singletonList(route)).when(routeFetcher).getAll();
    doReturn(routeModel).when(routeModelFactory).createModel(route);

    List<RouteModel> results = repository.getRoutes();
    assertNotNull(results);
    assertEquals(results.get(0), routeModel);
  }

  @Test
  public void testGetRoutesByStopSuccessfully() throws Exception {
    doReturn(1).when(stopModel).getCode();
    doReturn(Collections.singletonList(route)).when(routeFetcher).get(1);
    doReturn(routeModel).when(routeModelFactory).createModel(route);

    List<RouteModel> results = repository.getRoutes(stopModel);
    assertNotNull(results);
    assertEquals(results.get(0), routeModel);
  }

  @Test
  public void testGetAllStopsSuccessfully() throws Exception {
    doReturn(Collections.singletonList(route)).when(routeFetcher).getAll();
    doReturn("ROUTE").when(routeModel).getShortName();
    doReturn(Collections.singletonList(stop)).when(stopFetcher).get(anyString());
    doReturn(stopModel).when(stopModelFactory).createModel(stop);

    List<StopModel> results = repository.getStops();
    assertNotNull(results);
    assertEquals(results.get(0), stopModel);
  }

  @Test
  public void testGetStopsForRouteSuccessfully() throws Exception {
    doReturn("ROUTE").when(routeModel).getShortName();
    doReturn(Collections.singletonList(stop)).when(stopFetcher).get(anyString());
    doReturn(stopModel).when(stopModelFactory).createModel(stop);

    List<StopModel> results = repository.getStops(routeModel);
    assertNotNull(results);
    assertEquals(results.get(0), stopModel);
  }

  @Test
  public void testGetFavoritedStopsSuccessfully() throws Exception {
    doReturn(Collections.singletonList(route)).when(routeFetcher).getAll();
    doReturn("ROUTE").when(routeModel).getShortName();
    doReturn(Collections.singletonList(stop)).when(stopFetcher).get(anyString());
    doReturn(stopModel).when(stopModelFactory).createModel(stop);

    List<StopModel> results = repository.getFavoritedStops();
    assertNotNull(results);
    assertEquals(results.get(0), stopModel);
  }

  @Test
  public void testGetAllDeparturesForStopSuccessfully() throws Exception {
    doReturn(Collections.singletonList(route)).when(routeFetcher).getAll();
    doReturn("ROUTE").when(route).getShortName();
    doReturn(1).when(stopModel).getCode();
    doReturn(Collections.singletonList(departure)).when(departureFetcher).get(anyString(), anyInt());
    doReturn(departureModel).when(departureModelFactory).createModel(departure);

    List<DepartureModel> results = repository.getDepartures(routeModel, stopModel);
    assertNotNull(results);
    assertEquals(results.get(0), departureModel);
  }

  @Test
  public void testGetDeparturesForRouteAndStopSuccessfully() throws Exception {
    doReturn("ROUTE").when(routeModel).getShortName();
    doReturn(1).when(stopModel).getCode();
    doReturn(Collections.singletonList(departure)).when(departureFetcher).get(anyString(), anyInt());
    doReturn(departureModel).when(departureModelFactory).createModel(departure);

    List<DepartureModel> results = repository.getDepartures(routeModel, stopModel);
    assertNotNull(results);
    assertEquals(results.get(0), departureModel);
  }

  @Test
  public void testGetBusLocationsSuccessfully() throws Exception {
    doReturn("ROUTE").when(routeModel).getShortName();
    doReturn(Collections.singletonList(bus)).when(busFetcher).get(anyString());
    doReturn(busModel).when(busModelFactory).createModel(bus);

    List<BusModel> results = repository.getBusLocations(routeModel);
    assertNotNull(results);
    assertEquals(results.get(0), busModel);
  }

  @Test
  public void testRegisterBusListenerAndClearSuccessfully() throws Exception {
    BusListener busListener = new BusListener() {
      @Override
      public void onUpdateBuses(List<BusModel> buses) {
      }
    };
    repository.registerBusListener(routeModel, busListener);

    verify(busTimerTask).addListener(busListener);
    assertEquals(repository.busListeners.size(), 1);
    assertSame(repository.busListeners.get(0), busListener);

    repository.clearBusListener(busListener);

    verify(busTimerTask, times(2)).awaitTermination();
    verify(busTimerTask).cancel();
    assertEquals(repository.busListeners.size(), 0);
    assertNull(repository.busTimerTask);
  }

  @Test
  public void testRegisterBusListenerAndClearAllSuccessfully() throws Exception {
    BusListener busListener = new BusListener() {
      @Override
      public void onUpdateBuses(List<BusModel> buses) {
      }
    };
    BusListener busListener2 = new BusListener() {
      @Override
      public void onUpdateBuses(List<BusModel> buses) {
      }
    };
    repository.registerBusListener(routeModel, busListener);

    verify(busTimerTask).addListener(busListener);
    assertEquals(repository.busListeners.size(), 1);
    assertSame(repository.busListeners.get(0), busListener);

    repository.registerBusListener(routeModel, busListener2);

    verify(busTimerTask).addListener(busListener2);
    assertEquals(repository.busListeners.size(), 2);
    assertSame(repository.busListeners.get(1), busListener2);

    repository.clearBusListeners();

    verify(busTimerTask).awaitTermination();
    verify(busTimerTask).cancel();
    assertEquals(repository.busListeners.size(), 0);
    assertNull(repository.busTimerTask);
  }
}