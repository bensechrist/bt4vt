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

package com.bt4vt.repository.model;

import com.bt4vt.repository.domain.Stop;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.model.Marker;
import com.google.inject.Singleton;

/**
 * Factory for creating {@link StopModel} objects.
 *
 * @author Ben Sechrist
 */
@Singleton
public class StopModelFactory {

  /**
   * Creates a {@link StopModel} from the given <code>stop</code>.
   * @param stop the stop
   * @return the stop model
   */
  public StopModel createModel(Stop stop) {
    StopModelImpl model = new StopModelImpl(stop.getName(), stop.getCode());
    model.setLatitude(stop.getLatitude());
    model.setLongitude(stop.getLongitude());
    model.setRoutePattern(stop.getRoutePattern());
    model.setRoutePatternColor(stop.getRoutePatternColor());
    model.setFavorited(stop.isFavorited());
    return model;
  }

  /**
   * Creates a {@link StopModel} from the given <code>marker</code>.
   * @param marker the marker
   * @return the stop model
   */
  public StopModel createModel(Marker marker) {
    StopModelImpl model = StopModelImpl.valueOf(marker.getTitle());
    model.setLatLng(marker.getPosition());
    return model;
  }

  /**
   * Creates a {@link StopModel} from the given <code>geofence</code>.
   * @param geofence the geofence
   * @return the stop model
   */
  public StopModel createModel(Geofence geofence) {
    return StopModelImpl.valueOf(geofence.getRequestId());
  }

  /**
   * Creates a {@link StopModel} from the given <code>str</code>.
   * @param str the string value of stop
   * @return the stop model
   */
  public StopModel createModel(String str) {
    return StopModelImpl.valueOf(str);
  }
}
