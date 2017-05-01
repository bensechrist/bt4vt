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

import com.google.inject.Inject;

import roboguice.receiver.RoboBroadcastReceiver;

public class BootReceiver extends RoboBroadcastReceiver {

  private static final String TAG = "BootReceiver";

  @Inject
  private BusStopGeofenceService busStopGeofenceService;

//  @Override
//  public void handleReceive(Context context, Intent intent) {
//    Log.d(TAG, "Received boot");
//    if (GoogleApiAvailability.getInstance()
//        .isGooglePlayServicesAvailable(context) != ConnectionResult.SUCCESS) {
//      Log.e(TAG, context.getString(R.string.no_play_services));
//    } else {
//      List<StopFavorite> stopFavorites = new Select().from(StopFavorite.class)
//          .where("isFavorited = ?", true).execute();
//      Log.d(TAG, "Adding geofences for favorited stops");
//      Log.d(TAG, stopFavorites.toString());
//      for (StopFavorite stopFavorite : stopFavorites) {
//        // TODO: fetch stop info
//      }
//    }
//  }
//
//  @Override
//  public void onSuccess(StopModel stopModel) {
//    if (stopModel != null) {
//      busStopGeofenceService.registerGeofence(stopModel);
//    }
//  }
//
//  @Override
//  public void onException(Exception e) {
//    Log.e(TAG, e.getLocalizedMessage());
//  }
}
