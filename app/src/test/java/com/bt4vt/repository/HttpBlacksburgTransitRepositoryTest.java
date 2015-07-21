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

import com.bt4vt.repository.domain.Route;
import com.bt4vt.repository.domain.RouteFactory;
import com.bt4vt.repository.domain.Stop;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
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
  private RouteFactory routeFactory;

  @InjectMocks
  private HttpBlacksburgTransitRepository repository;

  @Test
  public void testGetRoutes() throws Exception {
    final String name = "name";
    doReturn(new Route(name))
        .when(routeFactory).createRoute(anyString());

    List<Route> result = repository.getRoutes();
    assertNotNull(result);
    assertThat(result.size(), greaterThan(0));
    assertEquals(result.get(0).getName(), name);
  }

  @Test
  public void testGetStops() throws Exception {
    List<Stop> result = repository.getStops(new Route("Main Street - South"));
    assertNotNull(result);
    assertThat(result.size(), greaterThan(0));
  }
}