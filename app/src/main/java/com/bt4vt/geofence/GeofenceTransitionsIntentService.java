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
import com.bt4vt.repository.exception.TransitRepositoryException;
import com.bt4vt.repository.model.DepartureModel;
import com.bt4vt.repository.model.StopModel;
import com.bt4vt.repository.model.StopModelFactory;
import com.bt4vt.util.NoficationUtils;
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
  private StopModelFactory stopModelFactory;

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
          try {
            StopModel stop = stopModelFactory.createModel(geofence);
            List<DepartureModel> departures = transitRepository.getDepartures(stop);
            final int MAX_DEPARTURES = getResources().getInteger(R.integer.max_departures_shown);
            if (departures.size() > MAX_DEPARTURES) {
              departures = departures.subList(0, MAX_DEPARTURES);
            }
            sendNotfication(stop, departures);
          } catch (TransitRepositoryException e) {
            Log.e(getClass().getSimpleName(), e.toString());
          }
        }
        break;

      case Geofence.GEOFENCE_TRANSITION_EXIT:
        for (Geofence geofence : triggeringGeofences) {
          removeNotification(stopModelFactory.createModel(geofence));
        }
        break;

      default:
        // Log the error.
        Log.e(getClass().getSimpleName(), getString(R.string.geofence_transition_invalid_type,
            geofenceTransition));
        break;
    }
  }

  private void sendNotfication(StopModel stop, List<DepartureModel> departures) {
    List<String> departureStrings = new ArrayList<>();
    Collections.sort(departures);
    for (DepartureModel departure : departures) {
      departureStrings.add(getString(R.string.notification_departures_format,
          departure.getTextDepartureTime(), departure.getRouteShortName()));
    }
    Intent intent = new Intent(this, MainActivity.class);
    intent.putExtra(MainActivity.EXTRA_STOP, stop.toString());
    NotificationCompat.Builder builder = NoficationUtils.generateInboxBuilder(this, stop.getName(),
        departureStrings, intent, stop.getCode());
    notificationManager.notify(stop.getCode(), builder.build());
  }

  private void removeNotification(StopModel stop) {
    notificationManager.cancel(stop.getCode());
  }
}
