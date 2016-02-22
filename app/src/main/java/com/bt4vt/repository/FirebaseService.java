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
import android.os.Binder;
import android.os.IBinder;

import com.bt4vt.geofence.BusStopGeofenceService;
import com.bt4vt.repository.model.StopModel;
import com.bt4vt.repository.model.StopModelFactory;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.inject.Inject;

import roboguice.service.RoboService;

/**
 * Firebase-specific interactions.
 *
 * @author Ben Sechrist
 */
public class FirebaseService extends RoboService implements ChildEventListener {

  public static final String USER_EMAIL_KEY = "firebase-user-email";

  private final FirebaseServiceBinder binder = new FirebaseServiceBinder();

  private static final String TAG = "FirebaseService";
  public static final String FIREBASE_BASE_URL = "https://blinding-torch-6262.firebaseio.com/";
  protected static final String FAVORITE_STOPS_PATH = "favorite-stops";

  @Inject
  private BusStopGeofenceService busStopGeofenceService;

  @Inject
  private StopModelFactory stopModelFactory;

  Firebase firebase;

  @Override
  public void onCreate() {
    super.onCreate();
    init();
  }

  void init() {
    if (firebase == null) {
      // Remove all Geofences
      busStopGeofenceService.unregisterAllGeofences();
      Firebase.setAndroidContext(this);
      firebase = new Firebase(FIREBASE_BASE_URL);
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    firebase.removeEventListener(this);
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

  public void registerStopListener(StopModel stop, ChildEventListener listener) {
    firebase.child(FAVORITE_STOPS_PATH)
        .child(String.valueOf(stop.getCode()))
        .addChildEventListener(listener);
  }

  public void unregisterStopListener(ChildEventListener listener) {
    firebase.removeEventListener(listener);
  }

  public void addFavorite(StopModel stop) {
    firebase.child(FAVORITE_STOPS_PATH)
        .child(String.valueOf(stop.getCode()))
        .setValue(stop);
  }

  public void removeFavorite(StopModel stop) {
    firebase.child(FAVORITE_STOPS_PATH)
        .child(String.valueOf(stop.getCode()))
        .removeValue();
  }

  @Override
  public void onChildAdded(DataSnapshot dataSnapshot, String s) {
    StopModel stop = stopModelFactory.createModel(dataSnapshot);
    // Set Geofence
    busStopGeofenceService.registerGeofence(stop);
  }

  @Override
  public void onChildRemoved(DataSnapshot dataSnapshot) {
    StopModel stop = stopModelFactory.createModel(dataSnapshot);
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

  public class FirebaseServiceBinder extends Binder {

    public FirebaseService getService() {
      return FirebaseService.this;
    }

  }
}
