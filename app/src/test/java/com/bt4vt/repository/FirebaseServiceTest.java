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

import com.bt4vt.repository.model.StopModel;
import com.firebase.client.AuthData;
import com.firebase.client.ChildEventListener;
import com.firebase.client.Firebase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
  private SharedPreferences.Editor editor;

  @Mock
  private Firebase firebase;

  @Mock
  private AuthData authData;

  @Mock
  private StopModel stopModel;

  @InjectMocks
  private FirebaseService service;

  @Before
  public void setup() throws Exception {
    doReturn(firebase).when(firebase).child(anyString());
    doReturn(firebase).when(firebase).getParent();
    doReturn(editor).when(preferences).edit();
    doReturn(editor).when(editor).remove(anyString());

    service.firebase = firebase;
    service.onAuthStateChanged(authData);
    verify(firebase).getParent();
  }

  @Test
  public void testRegisterAuthListener() throws Exception {
    Firebase.AuthStateListener listenerMock = mock(Firebase.AuthStateListener.class);
    service.registerAuthListener(listenerMock);
    verify(firebase).addAuthStateListener(listenerMock);
  }

  @Test
  public void testUnregisterAuthListener() throws Exception {
    Firebase.AuthStateListener listenerMock = mock(Firebase.AuthStateListener.class);
    service.unregisterAuthListener(listenerMock);
    verify(firebase).removeAuthStateListener(listenerMock);
  }

  @Test
  public void testRegisterStopListener() throws Exception {
    ChildEventListener listenerMock = mock(ChildEventListener.class);
    doReturn(1).when(stopModel).getCode();

    service.registerStopListener(stopModel, listenerMock);
    verify(firebase).child(FirebaseService.FAVORITE_STOPS_PATH);
    verify(firebase).child("1");
    verify(firebase).addChildEventListener(listenerMock);
  }

  @Test
  public void testUnregisterStopListener() throws Exception {
    ChildEventListener listenerMock = mock(ChildEventListener.class);

    service.unregisterStopListener(listenerMock);
    verify(firebase).removeEventListener(listenerMock);
  }

  @Test
  public void testAddFavorite() throws Exception {
    doReturn(1).when(stopModel).getCode();

    service.addFavorite(stopModel);
    verify(firebase).child(FirebaseService.FAVORITE_STOPS_PATH);
    verify(firebase).child("1");
    verify(firebase).setValue(stopModel);
  }

  @Test
  public void testRemoveFavorite() throws Exception {
    doReturn(1).when(stopModel).getCode();

    service.removeFavorite(stopModel);
    verify(firebase).child(FirebaseService.FAVORITE_STOPS_PATH);
    verify(firebase).child("1");
    verify(firebase).removeValue();
  }

  @Test
  public void testLoginGoogle() throws Exception {
    String token = "token";

    service.onAuthStateChanged(null);
    service.loginGoogle(token);
    verify(firebase).authWithOAuthToken("google", token, service);
  }

  @Test
  public void testLogout() throws Exception {
    service.logout();
    verify(preferences).edit();
    verify(editor).remove(FirebaseService.USER_EMAIL_KEY);
    verify(editor).apply();
    verify(firebase).unauth();
  }
}
