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

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.bt4vt.R;
import com.bt4vt.external.bt4u.Response;
import com.bt4vt.external.bt4u.Stop;
import com.bt4vt.external.bt4u.StopService;
import com.bt4vt.model.FavoriteStop;
import com.bt4vt.service.FavoriteStopService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.inject.Inject;

import java.util.List;

import roboguice.receiver.RoboBroadcastReceiver;

public class BootReceiver extends RoboBroadcastReceiver implements Response.Listener<Stop>,
    Response.ExceptionListener {

  private static final String TAG = "BootReceiver";

  @Inject
  private FavoriteStopService favoriteStopService;

  @Inject
  private StopService stopService;

  @Inject
  private BusStopGeofenceService busStopGeofenceService;

  @Override
  public void handleReceive(Context context, Intent intent) {
    Log.d(TAG, "Received boot");
    if (GoogleApiAvailability.getInstance()
        .isGooglePlayServicesAvailable(context) != ConnectionResult.SUCCESS) {
      Log.e(TAG, context.getString(R.string.no_play_services));
    } else {
      List<FavoriteStop> favoriteStops = favoriteStopService.getFavoriteStops();
      Log.d(TAG, "Adding geofences for favorited stops");
      Log.d(TAG, favoriteStops.toString());
      for (FavoriteStop favoriteStop : favoriteStops) {
        stopService.get(favoriteStop.getCode(), this, this);
      }
    }
  }

  @Override
  public void onResult(Stop result) {
    busStopGeofenceService.registerGeofence(result);
  }

  @Override
  public void onException(Exception e) {
    e.printStackTrace();
  }
}
