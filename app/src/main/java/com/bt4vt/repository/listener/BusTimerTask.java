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

package com.bt4vt.repository.listener;

import com.bt4vt.repository.TransitRepository;
import com.bt4vt.repository.domain.Bus;
import com.bt4vt.repository.domain.Route;

import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Bus-based implementation of {@link TimerTask}.
 *
 * @author Ben Sechrist
 */
public class BusTimerTask extends TimerTask {

  private final Route route;
  private final List<BusListener> busListeners;
  private final TransitRepository transitRepository;

  private final AtomicBoolean isCanceled = new AtomicBoolean(false);

  public BusTimerTask(Route route, List<BusListener> busListeners, TransitRepository transitRepository) {
    this.route = route;
    this.busListeners = busListeners;
    this.transitRepository = transitRepository;
  }

  @Override
  public void run() {
    final List<Bus> buses = transitRepository.getBusLocations(route);
    for (BusListener busListener : busListeners) {
      busListener.onUpdateBuses(buses);
    }
  }
}
