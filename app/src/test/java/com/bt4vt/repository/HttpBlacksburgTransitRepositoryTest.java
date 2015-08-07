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

import com.bt4vt.repository.domain.Bus;
import com.bt4vt.repository.domain.NextDeparture;
import com.bt4vt.repository.domain.Route;
import com.bt4vt.repository.domain.RouteFactory;
import com.bt4vt.repository.domain.Stop;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

/**
 * Tests the {@link HttpBlacksburgTransitRepository}.
 *
 * @author Ben Sechrist
 */
public class HttpBlacksburgTransitRepositoryTest {

  private final HttpBlacksburgTransitRepository repository = new HttpBlacksburgTransitRepository();

  @Before
  public void setUp() throws Exception {
    repository.routeFactory = new RouteFactory();
  }

  @Test
  public void testGetRoutesStopsDepartures() throws Exception {
    List<Route> routes = repository.getRoutes();
    assertNotNull(routes);
    assertThat(routes.size(), greaterThan(0));

    List<Stop> stops = new ArrayList<>();
    for (Route route : routes) {
      List<Stop> routeStops = repository.getStops(route);
      stops.addAll(routeStops);
      assertNotNull(stops);
      assertThat(stops.size(), greaterThan(0));

      for (Stop stop : routeStops) {
        List<NextDeparture> nextDeparturesForRoute = repository.getNextDepartures(stop, route);
        assertNotNull(nextDeparturesForRoute);
      }
    }
  }

  @Test
  public void testGetStopsDepartures() throws Exception {
    List<Stop> stops = repository.getStops();
    assertNotNull(stops);
    assertThat(stops.size(), greaterThan(0));

    for (Stop stop : stops) {
      List<NextDeparture> nextDepartures = repository.getNextDepartures(stop);
      assertNotNull(nextDepartures);
      for (NextDeparture nd : nextDepartures) {
        assertNotNull(nd.getDepartures());
      }
    }
  }

  @Test
  public void testGetBuses() throws Exception {
    List<Route> routes = repository.getRoutes();
    assertNotNull(routes);
    assertThat(routes.size(), greaterThan(0));

    for (Route route : routes) {
      List<Bus> buses = repository.getBusLocations(route);
      assertNotNull(buses);
    }
  }
}