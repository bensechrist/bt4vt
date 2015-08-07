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
import com.bt4vt.repository.TransitRepository;
import com.bt4vt.repository.domain.Departure;
import com.bt4vt.repository.domain.NextDeparture;
import com.bt4vt.repository.domain.Stop;
import com.bt4vt.repository.exception.TransitRepositoryException;
import com.bt4vt.util.NoficationUtil;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import roboguice.service.RoboIntentService;

/**
 * Handles geofence transitions.
 *
 * @author Ben Sechrist
 */
public class GeofenceTransitionsIntentService extends RoboIntentService {

  @Inject
  private TransitRepository transitRepository;

  @Inject
  private NotificationManager notificationManager;

  public GeofenceTransitionsIntentService() {
    super("GeofenceTransitionsIntentService");
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
    if (geofencingEvent.hasError()) {
      Log.e(getClass().getSimpleName(), String.format("Geofence transition error: %d",
          geofencingEvent.getErrorCode()));
      return;
    }

    int geofenceTransition = geofencingEvent.getGeofenceTransition();

    List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

    switch (geofenceTransition) {
      case Geofence.GEOFENCE_TRANSITION_DWELL:
        for (Geofence geofence : triggeringGeofences) {
          Stop stop = Stop.valueOf(geofence.getRequestId());
          try {
            List<NextDeparture> nextDepartures = transitRepository.getNextDepartures(stop);
            sendNotfication(stop, nextDepartures);
          } catch (TransitRepositoryException e) {
            Log.e(getClass().getSimpleName(), e.toString());
          }
        }
        break;

      case Geofence.GEOFENCE_TRANSITION_EXIT:
        for (Geofence geofence : triggeringGeofences) {
          Stop stop = Stop.valueOf(geofence.getRequestId());
          removeNotification(stop);
        }
        break;

      default:
        // Log the error.
        Log.e(getClass().getSimpleName(), getString(R.string.geofence_transition_invalid_type,
            geofenceTransition));
        break;
    }
  }

  private void sendNotfication(Stop stop, List<NextDeparture> nextDepartures) {
    List<String> departureStrings = new ArrayList<>();
    List<Departure> departures = new ArrayList<>();
    for (NextDeparture nextDeparture : nextDepartures) {
      departures.addAll(nextDeparture.getDepartures());
    }
    Collections.sort(departures);
    for (Departure departure : departures) {
      departureStrings.add(getString(R.string.notification_departures_format,
          departure.getDepartureTime(), departure.getShortRouteName()));
    }
    Intent intent = new Intent(this, MainActivity.class);
    intent.putExtra(MainActivity.EXTRA_STOP, stop.toString());
    NotificationCompat.Builder builder = NoficationUtil.generateInboxBuilder(this, stop.getName(),
        departureStrings, intent, stop.getCode());
    notificationManager.notify(stop.getCode(), builder.build());
  }

  private void removeNotification(Stop stop) {
    notificationManager.cancel(stop.getCode());
  }
}
