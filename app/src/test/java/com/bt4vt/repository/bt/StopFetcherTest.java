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

package com.bt4vt.repository.bt;

import com.bt4vt.repository.domain.Stop;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests the {@link StopFetcher}.
 *
 * @author Ben Sechrist
 */
public class StopFetcherTest {

  private StopFetcher fetcher = new StopFetcher();

  @Test
  public void testGet() throws Exception {
    List<Stop> results = fetcher.get("MSS");
    assertNotNull(results);
    assertNotNull(results.get(0).getRoutePattern());
  }

  @Test
  public void testGetNotFound() throws Exception {
    assertEquals(fetcher.get("NOT ROUTE").size(), 0);
  }

  @Test
  public void testGetSingle() throws Exception {
    Stop result = fetcher.get(1609);
    assertNotNull(result);
  }

  @Test
  public void testGetSingleNotFound() throws Exception {
    assertNull(fetcher.get(0));
  }
}