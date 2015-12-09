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

import com.bt4vt.repository.domain.Departure;
import com.google.inject.Singleton;

/**
 * Factory for creating {@link DepartureModel} objects.
 *
 * @author Ben Sechrist
 */
@Singleton
public class DepartureModelFactory {

  /**
   * Creates a {@link DepartureModel} from the given <code>departure</code>.
   * @param departure the departure
   * @return the newly created DepartureModel
   */
  public DepartureModel createModel(Departure departure) {
    DepartureModelImpl model = new DepartureModelImpl();
    model.setRouteShortName(departure.getRouteShortName());
    model.setRouteName(departure.getRouteName());
    model.setStopName(departure.getStopName());
    model.setDepartureTime(departure.getDepartureTime());
    return model;
  }

}
