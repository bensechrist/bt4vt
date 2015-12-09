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

import com.bt4vt.repository.domain.Departure;
import com.bt4vt.repository.exception.FetchException;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertNotNull;

/**
 * Tests the {@link DepartureFetcher}.
 *
 * @author Ben Sechrist
 */
public class DepartureFetcherTest {

  private final DepartureFetcher fetcher = new DepartureFetcher();

  @Test
  public void testGet() throws Exception {
    List<Departure> departures = fetcher.get("MSS", 1609);
    assertNotNull(departures);
  }

  @Test(expected = FetchException.class)
  public void testGetNotFound() throws Exception {
    fetcher.get("NOT ROUTE", -1);
  }
}