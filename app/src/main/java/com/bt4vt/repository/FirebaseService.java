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

import com.bt4vt.BuildConfig;
import com.bt4vt.async.AsyncCallback;
import com.bt4vt.async.FetchGoogleTokenTask;
import com.bt4vt.geofence.BusStopGeofenceService;
import com.bt4vt.repository.model.StopModel;
import com.bt4vt.repository.model.StopModelFactory;
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
    ChildEventListener, AsyncCallback<String>, Firebase.AuthStateListener {

  public static final String USER_EMAIL_KEY = "firebase-user-email";
  public static final String PROFILE_IMAGE_URL = "profileImageURL";
  public static final String DISPLAY_NAME = "displayName";

  private final FirebaseServiceBinder binder = new FirebaseServiceBinder();

  private static final String TAG = "FirebaseService";
  private static final String FIREBASE_BASE_URL = "https://blinding-torch-6262.firebaseio.com/";
  private static final String FAVORITE_STOPS_PATH = "favorite-stops";

  @Inject
  private BusStopGeofenceService busStopGeofenceService;

  @Inject
  private StopModelFactory stopModelFactory;

  @Inject
  private SharedPreferences preferences;

  Firebase firebase;

  private final Set<StopModel> favoritedStops = new HashSet<>();

  private AtomicBoolean authenticated = new AtomicBoolean(false);

  @Override
  public void onCreate() {
    super.onCreate();
    init();
  }

  void init() {
    if (firebase == null) {
      // Remove all Geofences
      busStopGeofenceService.unregisterAllGeofences();
      Firebase.setAndroidContext(FirebaseService.this);
      firebase = new Firebase(FIREBASE_BASE_URL);
      firebase.addAuthStateListener(this);
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    firebase.removeEventListener(this);
    firebase.removeAuthStateListener(this);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    return START_STICKY;
  }

  @Override
  public IBinder onBind(Intent intent) {
    return binder;
  }

  public void registerAuthListener(Firebase.AuthStateListener listener) {
    if (firebase != null) {
      firebase.addAuthStateListener(listener);
    }
  }

  public void unregisterAuthListener(Firebase.AuthStateListener listener) {
    if (firebase != null) {
      firebase.removeAuthStateListener(listener);
    }
  }

  public boolean isFavorited(StopModel stop) {
    return favoritedStops.contains(stop);
  }

  public void addFavorite(StopModel stop) {
    if (authenticated.get()) {
      favoritedStops.add(stop);
      firebase.child(FAVORITE_STOPS_PATH)
          .child(String.valueOf(stop.getCode()))
          .setValue(stop);
    } else {
      throw new IllegalStateException("Firebase not authenticated");
    }
  }

  public void removeFavorite(StopModel stop) {
    if (authenticated.get()) {
      favoritedStops.remove(stop);
      firebase.child(FAVORITE_STOPS_PATH)
          .child(String.valueOf(stop.getCode()))
          .removeValue();
    } else {
      throw new IllegalStateException("Firebase not authenticated");
    }
  }

  @Override
  public void onAuthStateChanged(AuthData authData) {
    if (authData == null) {
      authenticated.set(false);
      String email = preferences.getString(USER_EMAIL_KEY, null);
      if (email != null) {
        new FetchGoogleTokenTask(this, email, this).execute();
      }
    } else {
      authenticated.set(true);
      if (firebase.getParent() == null) { // We are still at the root
        authSetup(authData);
      }
    }
  }

  /**
   * Logs the user with the given Google <code>token</code> and calls the <code>handler</code>
   * when the auth is finished.
   *
   * @param token the auth token
   */
  public void loginGoogle(String token) {
    if (!authenticated.get()) {
      firebase.authWithOAuthToken("google", token, this);
    }
  }

  /**
   * Logs the user out.
   */
  public void logout() {
    preferences.edit().remove(USER_EMAIL_KEY).apply();
    firebase.unauth();
  }

  @Override
  public void onAuthenticated(AuthData authData) {
    Log.i(TAG, "Authenticated");
  }

  @Override
  public void onAuthenticationError(FirebaseError firebaseError) {
    if (BuildConfig.DEBUG) {
      throw new RuntimeException(firebaseError.toException());
    }
    Log.e(TAG, "Firebase Auth Error: " + firebaseError);
  }

  @Override
  public void onSuccess(String token) {
    loginGoogle(token);
  }

  @Override
  public void onException(Exception e) {
    e.printStackTrace();
  }

  @Override
  public void onChildAdded(DataSnapshot dataSnapshot, String s) {
    StopModel stop = stopModelFactory.createModel(dataSnapshot);
    Log.i(TAG, "Adding stop to favorites: " + stop);
    favoritedStops.add(stop);
    // Set Geofence
    busStopGeofenceService.registerGeofence(stop);
  }

  @Override
  public void onChildRemoved(DataSnapshot dataSnapshot) {
    StopModel stop = stopModelFactory.createModel(dataSnapshot);
    Log.i(TAG, "Removing stop from favorites: " + stop);
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
