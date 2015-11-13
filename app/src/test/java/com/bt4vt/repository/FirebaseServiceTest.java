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

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;

/**
 * Tests the {@link FirebaseService}.
 *
 * @author Ben Sechrist
 */
@RunWith(MockitoJUnitRunner.class)
public class FirebaseServiceTest {

  @Mock
  private SharedPreferences preferences;

  @Mock
  private Firebase firebase;

  @Mock
  private AuthData authData;

  @InjectMocks
  private FirebaseService service;

  @Before
  public void setup() throws Exception {
    doReturn(firebase).when(firebase).child(anyString());
    doReturn(firebase).when(firebase).getParent();

    service.firebase = firebase;
    service.init();
    service.onAuthStateChanged(authData);
  }

  @Test
  public void testFavoriteStops() throws Exception {
    Stop stop = new Stop();
    assertFalse(service.isFavorited(stop));
    service.addFavorite(stop);
    assertTrue(service.isFavorited(stop));
    service.removeFavorite(stop);
    assertFalse(service.isFavorited(stop));
  }
}
