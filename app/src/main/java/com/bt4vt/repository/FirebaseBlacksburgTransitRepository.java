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
import android.util.Log;

import com.bt4vt.repository.domain.Stop;
import com.firebase.client.AuthData;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.inject.Inject;

import java.util.HashSet;
import java.util.Set;

import roboguice.inject.ContextSingleton;

/**
 * Adds more information to {@link HttpBlacksburgTransitRepository} from Firebase.
 *
 * @author Ben Sechrist
 */
@ContextSingleton
public class FirebaseBlacksburgTransitRepository extends HttpBlacksburgTransitRepository
    implements FirebaseService, Firebase.AuthResultHandler, ChildEventListener {

  private static final String FAVORITE_STOPS_PATH = "favorite-stops";
  private static final String GOOGLE_OAUTH_TOKEN_KEY = "google_oauth_token";

  @Inject
  private SharedPreferences preferences;

  Firebase firebase;

  private final Set<Stop> favoritedStops = new HashSet<>();

  @Override
  public void init() {
    if (firebase == null) {
      firebase = new Firebase("https://blinding-torch-6262.firebaseio.com/");
      if (isAuthenticated()) {
        authSetup(firebase.getAuth());
      } else {
        String token = preferences.getString(GOOGLE_OAUTH_TOKEN_KEY, null);
        if (token != null) {
          loginGoogle(token);
        }
      }
    }
  }

  @Override
  public boolean isFavorited(Stop stop) {
    return favoritedStops.contains(stop);
  }

  @Override
  public void favoriteStop(Stop stop) {
    if (isAuthenticated()) {
      favoritedStops.add(stop);
      firebase.child(FAVORITE_STOPS_PATH)
          .child(String.valueOf(stop.getCode()))
          .setValue(stop);
    }
  }

  @Override
  public void unfavoriteStop(Stop stop) {
    if (isAuthenticated()) {
      favoritedStops.remove(stop);
      firebase.child(FAVORITE_STOPS_PATH)
          .child(String.valueOf(stop.getCode()))
          .removeValue();
    }
  }

  @Override
  public boolean isAuthenticated() {
    return ((firebase != null) && (firebase.getAuth() != null));
  }

  @Override
  public void loginGoogle(String token) {
    preferences.edit().putString(GOOGLE_OAUTH_TOKEN_KEY, token).apply();
    if (firebase != null) {
      firebase.authWithOAuthToken("google", token, this);
    } else {
      init();
    }
  }

  @Override
  public void onAuthenticated(AuthData authData) {
    Log.i(getClass().getSimpleName(), "Authenticated");
    authSetup(authData);
  }

  @Override
  public void onAuthenticationError(FirebaseError firebaseError) {
    Log.e(getClass().getSimpleName(), "Firebase Auth Error: " + firebaseError);
    if (firebaseError.getCode() == FirebaseError.EXPIRED_TOKEN ||
        firebaseError.getCode() == FirebaseError.INVALID_TOKEN) {
      preferences.edit().remove(GOOGLE_OAUTH_TOKEN_KEY).apply();
    }
  }

  @Override
  public void onChildAdded(DataSnapshot dataSnapshot, String s) {
    favoritedStops.add(dataSnapshot.getValue(Stop.class));
  }

  @Override
  public void onChildRemoved(DataSnapshot dataSnapshot) {
    favoritedStops.remove(dataSnapshot.getValue(Stop.class));
  }

  @Override
  public void onChildChanged(DataSnapshot dataSnapshot, String s) {
    // We don't care about this for now
  }

  @Override
  public void onChildMoved(DataSnapshot dataSnapshot, String s) {
    // We don't care about this for now
  }

  @Override
  public void onCancelled(FirebaseError firebaseError) {
    // We don't care about this for now
  }

  private void authSetup(AuthData authData) {
    firebase = firebase.child(authData.getUid());
    firebase.child(FAVORITE_STOPS_PATH)
        .addChildEventListener(this);
  }
}
