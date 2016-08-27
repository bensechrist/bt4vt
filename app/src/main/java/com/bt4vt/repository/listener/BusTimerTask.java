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
import com.bt4vt.repository.exception.TransitRepositoryException;
import com.bt4vt.repository.model.BusModel;
import com.bt4vt.repository.model.RouteModel;

import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Bus-based implementation of {@link TimerTask}.
 *
 * @author Ben Sechrist
 */
public class BusTimerTask extends TimerTask {

  private final Lock lock = new ReentrantLock();
  private final Condition condition = lock.newCondition();
  private AtomicBoolean running = new AtomicBoolean(false);

  private final RouteModel route;
  private final List<BusListener> busListeners;
  private final TransitRepository transitRepository;

  public BusTimerTask(RouteModel route, List<BusListener> busListeners, TransitRepository transitRepository) {
    this.route = route;
    this.busListeners = busListeners;
    this.transitRepository = transitRepository;
  }

  public void addListener(BusListener busListener) {
    busListeners.add(busListener);
  }

  public void removeListener(BusListener busListener) {
    busListeners.remove(busListener);
  }

  @Override
  public void run() {
    running.set(true);
    try {
      final List<BusModel> buses = transitRepository.getBusLocations(route);
      for (BusListener busListener : busListeners) {
        busListener.onUpdateBuses(buses);
      }
    } catch (TransitRepositoryException e) {
      e.printStackTrace();
    } finally {
      lock.lock();
      try {
        running.set(false);
        condition.signalAll();
      } finally {
        lock.unlock();
      }
    }
  }

  public void awaitTermination() {
    if (running.get()) {
      lock.lock();
      if (running.get()) {
        try {
          condition.await();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        } finally {
          lock.unlock();
        }
      }
    }
  }
}
