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

import com.bt4vt.repository.domain.Bus;
import com.google.inject.Singleton;

/**
 * Factory for creating {@link BusModel} objects.
 *
 * @author Ben Sechrist
 */
@Singleton
public class BusModelFactory {

  /**
   * Creates a {@link BusModel} from the given <code>bus</code>.
   * @param bus the bus
   * @return the bus model
   */
  public BusModel createModel(Bus bus) {
    BusModelImpl model = new BusModelImpl();
    model.setId(bus.getId());
    model.setLatLng(bus.getLatLng());
    model.setPassengers(bus.getPassengers());
    model.setRouteShortName(bus.getRouteShortName());
    model.setLastUpdated(bus.getTimestamp());
    return model;
  }
}
