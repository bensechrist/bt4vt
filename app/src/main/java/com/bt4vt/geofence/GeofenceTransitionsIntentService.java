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

import android.app.NotificationManager;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.bt4vt.MainActivity;
import com.bt4vt.R;
import com.bt4vt.external.bt4u.Departure;
import com.bt4vt.external.bt4u.DepartureService;
import com.bt4vt.external.bt4u.Response;
import com.bt4vt.external.bt4u.Stop;
import com.bt4vt.external.bt4u.StopService;
import com.bt4vt.util.NoficationUtils;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;
import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.List;

import roboguice.service.RoboIntentService;

/**
 * Handles geofence transitions.
 *
 * @author Ben Sechrist
 */
public class GeofenceTransitionsIntentService extends RoboIntentService implements
    Response.Listener<Stop>, Response.ExceptionListener {

  private static final String TAG = "GeofenceTransitions";

  @Inject
  private StopService stopService;

  @Inject
  private DepartureService departureService;

  @Inject
  private NotificationManager notificationManager;

  public GeofenceTransitionsIntentService() {
    super("GeofenceTransitionsIntentService");
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    Log.i(TAG, "Got geofence transition intent: " + intent.toString());

    GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
    if (geofencingEvent.hasError()) {
      Log.e(TAG, String.format("Geofence transition error: %d", geofencingEvent.getErrorCode()));
      return;
    }

    int geofenceTransition = geofencingEvent.getGeofenceTransition();

    List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

    switch (geofenceTransition) {
      case Geofence.GEOFENCE_TRANSITION_DWELL:
        Log.i(TAG, "Dwelling transition");
        for (Geofence geofence : triggeringGeofences) {
          String stopCode = geofence.getRequestId();
          stopService.get(stopCode, this, this);
        }
        break;

      case Geofence.GEOFENCE_TRANSITION_EXIT:
        Log.i(TAG, "Exit transition");
        for (Geofence geofence : triggeringGeofences) {
          String stopCode = geofence.getRequestId();
          stopService.get(stopCode, new Response.Listener<Stop>() {
            @Override
            public void onResult(Stop result) {
              removeNotification(result);
            }
          }, this);
        }
        break;

      default:
        // Log the error.
        Log.e(TAG, getString(R.string.geofence_transition_invalid_type,
            geofenceTransition));
        break;
    }
  }

  @Override
  public void onResult(final Stop stop) {
    departureService.getAll(null, stop.getCode(), new Response.Listener<List<Departure>>() {
      @Override
      public void onResult(List<Departure> departures) {
        final int MAX_DEPARTURES = getResources().getInteger(R.integer.max_departures_shown);
        for (Departure departure : departures) {
          if (departure.getDepartures().size() > MAX_DEPARTURES) {
            departure.setDepartures(departure.getDepartures().subList(0, MAX_DEPARTURES));
          }
        }
        sendNotfication(stop, departures);
      }
    }, this);
  }

  @Override
  public void onException(Exception e) {
    e.printStackTrace();
  }

  private void sendNotfication(Stop stop, List<Departure> departures) {
    List<String> departureStrings = new ArrayList<>();
    for (Departure departure : departures) {
      String departuresString = "";
      for (String departureText : departure.getDepartures()) {
        departuresString += departureText + '\n';
      }
      departureStrings.add(getString(R.string.notification_departures_format,
          departure.getRouteName(), departuresString));
    }
    Intent intent = new Intent(this, MainActivity.class);
    intent.putExtra(MainActivity.EXTRA_STOP_CODE, stop.getCode());
    NotificationCompat.Builder builder = NoficationUtils.generateInboxBuilder(this, stop.getName(),
        departureStrings, intent, stop.getCode().hashCode());
    notificationManager.notify(stop.getCode().hashCode(), builder.build());
  }

  private void removeNotification(Stop stop) {
    notificationManager.cancel(stop.getCode().hashCode());
  }
}
