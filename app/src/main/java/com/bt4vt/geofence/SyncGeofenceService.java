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

package com.bt4vt.geofence;

import android.content.Intent;
import android.util.Log;

import com.bt4vt.MainActivity;
import com.bt4vt.repository.model.StopModel;
import com.bt4vt.repository.model.StopModelImpl;
import com.bt4vt.repository.model.StopModels;
import com.firebase.client.AuthData;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.GenericTypeIndicator;
import com.firebase.client.ValueEventListener;
import com.google.inject.Inject;

import java.util.List;

import roboguice.service.RoboIntentService;

/**
 * Handles adding geofences for favorited stops.
 *
 * @author Ben Sechrist
 */
public class SyncGeofenceService extends RoboIntentService implements Firebase.AuthStateListener,
    ValueEventListener {

  private static final String TAG = "SyncGeofenceService";

  @Inject
  private BusStopGeofenceService busStopGeofenceService;

  private Firebase firebaseRef;

  public SyncGeofenceService() {
    super("SyncGeofenceService");
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    busStopGeofenceService.unregisterAllGeofences();
    Firebase.setAndroidContext(this);
    firebaseRef = new Firebase(MainActivity.FIREBASE_BASE_URL);
    firebaseRef.addAuthStateListener(this);
  }

  @Override
  public void onAuthStateChanged(AuthData authData) {
    firebaseRef.removeAuthStateListener(this);
    if (authData != null) {
      firebaseRef.child(authData.getUid())
          .child("favorite-stops")
          .addListenerForSingleValueEvent(this);
    } else {
      stopSelf();
    }
  }

  @Override
  public void onDataChange(DataSnapshot dataSnapshot) {
    Log.i(TAG, dataSnapshot.toString());
    for (DataSnapshot child : dataSnapshot.getChildren()) {
      StopModel stopModel = StopModels.fromDataSnapshot(child);
      Log.i(TAG, "Registering geofence " + stopModel.toString());
      busStopGeofenceService.registerGeofence(stopModel);
    }
    stopSelf();
  }

  @Override
  public void onCancelled(FirebaseError firebaseError) {
    Log.e(TAG, firebaseError.toString());
    stopSelf();
  }
}
