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

import com.bt4vt.repository.bt.DepartureFetcher;
import com.bt4vt.repository.bt.RouteFetcher;
import com.bt4vt.repository.bt.StopFetcher;
import com.bt4vt.repository.domain.Departure;
import com.bt4vt.repository.domain.Route;
import com.bt4vt.repository.domain.ScheduledRoute;
import com.bt4vt.repository.domain.Stop;
import com.bt4vt.repository.model.DepartureModel;
import com.bt4vt.repository.model.DepartureModelFactory;
import com.bt4vt.repository.model.RouteModel;
import com.bt4vt.repository.model.RouteModelFactory;
import com.bt4vt.repository.model.StopModel;
import com.bt4vt.repository.model.StopModelFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;

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
  private Route route;

  @Mock
  private ScheduledRoute scheduledRoute;

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

  @InjectMocks
  private HttpBlacksburgTransitRepository repository;

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
    doReturn(Collections.singletonList(scheduledRoute)).when(routeFetcher).get(1);
    doReturn(routeModel).when(routeModelFactory).createModel(scheduledRoute);

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

//  @Test
//  public void testGetRoutesStopsDepartures() throws Exception {
//    List<Route> routes = repository.getRoutes();
//    assertNotNull(routes);
//    assertThat(routes.size(), greaterThan(0));
//
//    List<Stop> stops = new ArrayList<>();
//    for (Route route : routes) {
//      List<Stop> routeStops = repository.getStops(route);
//      stops.addAll(routeStops);
//      assertNotNull(stops);
//      assertThat(stops.size(), greaterThan(0));
//
//      for (Stop stop : routeStops) {
//        List<NextDeparture> nextDeparturesForRoute = repository.getNextDepartures(stop, route);
//        assertNotNull(nextDeparturesForRoute);
//      }
//    }
//  }
//
//  @Test
//  public void testGetStopsDepartures() throws Exception {
//    List<Stop> stops = repository.getStops();
//    assertNotNull(stops);
//    assertThat(stops.size(), greaterThan(0));
//
//    for (Stop stop : stops) {
//      List<NextDeparture> nextDepartures = repository.getNextDepartures(stop);
//      assertNotNull(nextDepartures);
//      for (NextDeparture nd : nextDepartures) {
//        assertNotNull(nd.getDepartures());
//      }
//    }
//  }
//
//  @Test
//  public void testGetBuses() throws Exception {
//    List<Route> routes = repository.getRoutes();
//    assertNotNull(routes);
//    assertThat(routes.size(), greaterThan(0));
//
//    for (Route route : routes) {
//      List<Bus> buses = repository.getBusLocations(route);
//      assertNotNull(buses);
//    }
//  }
//
//  @Test
//  public void testFetchBusesError() throws Exception {
//    try {
//      repository.fetchBuses("TEST");
//      assertTrue(false);
//    } catch (TransitRepositoryException e) {
//      assertThat(e.getMessage(), containsString("error getting buses for TEST"));
//    }
//  }
}