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

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.bt4vt.geofence.BusStopGeofenceService;
import com.bt4vt.repository.domain.Stop;
import com.firebase.client.AuthData;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.inject.Inject;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import roboguice.service.RoboService;

/**
 * Firebase-specific interactions.
 *
 * @author Ben Sechrist
 */
public class FirebaseService extends RoboService implements Firebase.AuthResultHandler,
    ChildEventListener {

  private final FirebaseServiceBinder binder = new FirebaseServiceBinder();

  public static boolean isRunning = false;

  private static final String FIREBASE_BASE_URL = "https://blinding-torch-6262.firebaseio.com/";
  private static final String FAVORITE_STOPS_PATH = "favorite-stops";
  private static final String GOOGLE_OAUTH_TOKEN_KEY = "google_oauth_token";

  @Inject
  private BusStopGeofenceService busStopGeofenceService;

  @Inject
  private SharedPreferences preferences;

  Firebase firebase;

  private final Set<Stop> favoritedStops = new HashSet<>();

  private AtomicBoolean authenticating = new AtomicBoolean(false);

  @Override
  public void onCreate() {
    super.onCreate();
    init();
  }

  void init() {
    isRunning = true;

    if (firebase == null) {
      // Remove all Geofences
      busStopGeofenceService.unregisterAllGeofences();
      Firebase.setAndroidContext(FirebaseService.this);
      firebase = new Firebase(FIREBASE_BASE_URL);
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
  public void onDestroy() {
    super.onDestroy();
    isRunning = false;

    firebase.removeEventListener(this);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Log.i(getClass().getSimpleName(), "Started service");
    return START_STICKY;
  }

  @Override
  public IBinder onBind(Intent intent) {
    return binder;
  }

  public boolean isFavorited(Stop stop) {
    return favoritedStops.contains(stop);
  }

  public void addFavorite(Stop stop) {
    if (isAuthenticated()) {
      favoritedStops.add(stop);
      firebase.child(FAVORITE_STOPS_PATH)
          .child(String.valueOf(stop.getCode()))
          .setValue(stop);
    } else {
      throw new IllegalStateException("Firebase not authenticated");
    }
  }

  public void removeFavorite(Stop stop) {
    if (isAuthenticated()) {
      favoritedStops.remove(stop);
      firebase.child(FAVORITE_STOPS_PATH)
          .child(String.valueOf(stop.getCode()))
          .removeValue();
    } else {
      throw new IllegalStateException("Firebase not authenticated");
    }
  }

  /**
   * Returns whether the user has authenticated.
   *
   * @return true if the user is logged in, false otherwise
   */
  public boolean isAuthenticated() {
    return ((firebase != null) && (firebase.getAuth() != null));
  }

  /**
   * Logs the user with the given Google <code>token</code>.
   *
   * @param token the auth token
   */
  public void loginGoogle(String token) {
    if (!isAuthenticated() && !authenticating.get()) {
      authenticating.set(true);
      preferences.edit().putString(GOOGLE_OAUTH_TOKEN_KEY, token).apply();
      firebase.authWithOAuthToken("google", token, this);
    }
  }

  @Override
  public void onAuthenticated(AuthData authData) {
    authenticating.set(false);
    Log.i(getClass().getSimpleName(), "Authenticated");
    authSetup(authData);
  }

  @Override
  public void onAuthenticationError(FirebaseError firebaseError) {
    authenticating.set(false);
    Log.e(getClass().getSimpleName(), "Firebase Auth Error: " + firebaseError);
    int errorCode = firebaseError.getCode();
    if (errorCode == FirebaseError.EXPIRED_TOKEN ||
        errorCode == FirebaseError.INVALID_TOKEN ||
        errorCode == FirebaseError.INVALID_CREDENTIALS) {
      preferences.edit().remove(GOOGLE_OAUTH_TOKEN_KEY).apply();
    }
  }

  @Override
  public void onChildAdded(DataSnapshot dataSnapshot, String s) {
    Stop stop = dataSnapshot.getValue(Stop.class);
    Log.i(getClass().getSimpleName(), "Adding stop to favorites: " + stop);
    favoritedStops.add(stop);
    // Set Geofence
    busStopGeofenceService.registerGeofence(stop);
  }

  @Override
  public void onChildRemoved(DataSnapshot dataSnapshot) {
    Stop stop = dataSnapshot.getValue(Stop.class);
    Log.i(getClass().getSimpleName(), "Removing stop from favorites: " + stop);
    favoritedStops.remove(stop);
    // Remove Geofence
    busStopGeofenceService.unregisterGeofence(stop);
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

  public class FirebaseServiceBinder extends Binder {
    public FirebaseService getService() {
      return FirebaseService.this;
    }
  }
}
