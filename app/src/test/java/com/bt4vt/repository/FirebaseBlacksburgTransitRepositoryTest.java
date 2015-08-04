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

import android.content.SharedPreferences;

import com.bt4vt.repository.domain.Stop;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;

/**
 * Tests the {@link FirebaseBlacksburgTransitRepository}.
 *
 * @author Ben Sechrist
 */
@RunWith(MockitoJUnitRunner.class)
public class FirebaseBlacksburgTransitRepositoryTest {

  @Mock
  private SharedPreferences preferences;

  @Mock
  private Firebase firebase;

  @Mock
  private AuthData authData;

  @InjectMocks
  private FirebaseBlacksburgTransitRepository repository;

  @Before
  public void setup() throws Exception {
    doReturn(authData).when(firebase).getAuth();
    doReturn(firebase).when(firebase).child(anyString());

    repository.firebase = firebase;
    repository.init();
  }

  @Test
  public void testFavoriteStops() throws Exception {
    List<Stop> stops = repository.getStops();

    for (Stop stop : stops) {
      assertFalse(repository.isFavorited(stop));
      repository.favoriteStop(stop);
      assertTrue(repository.isFavorited(stop));
      repository.unfavoriteStop(stop);
      assertFalse(repository.isFavorited(stop));
    }
  }
}
