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

package com.bt4vt.repository;

import com.bt4vt.repository.bt.BusFetcher;
import com.bt4vt.repository.bt.DepartureFetcher;
import com.bt4vt.repository.bt.RouteFetcher;
import com.bt4vt.repository.bt.StopFetcher;
import com.bt4vt.repository.domain.Bus;
import com.bt4vt.repository.domain.Departure;
import com.bt4vt.repository.domain.Route;
import com.bt4vt.repository.domain.ScheduledRoute;
import com.bt4vt.repository.domain.Stop;
import com.bt4vt.repository.exception.FetchException;
import com.bt4vt.repository.exception.TransitRepositoryException;
import com.bt4vt.repository.listener.BusListener;
import com.bt4vt.repository.listener.BusTimerTask;
import com.bt4vt.repository.model.BusModel;
import com.bt4vt.repository.model.BusModelFactory;
import com.bt4vt.repository.model.DepartureModel;
import com.bt4vt.repository.model.DepartureModelFactory;
import com.bt4vt.repository.model.RouteModel;
import com.bt4vt.repository.model.RouteModelFactory;
import com.bt4vt.repository.model.StopModel;
import com.bt4vt.repository.model.StopModelFactory;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

/**
 * Injectable implementation of {@link TransitRepository}.
 *
 * @author Ben Sechrist
 */
@Singleton
public class HttpBlacksburgTransitRepository implements TransitRepository {

  @Inject
  RouteFetcher routeFetcher;

  @Inject
  RouteModelFactory routeModelFactory;

  @Inject
  StopFetcher stopFetcher;

  @Inject
  StopModelFactory stopModelFactory;

  @Inject
  DepartureFetcher departureFetcher;

  @Inject
  DepartureModelFactory departureModelFactory;

  @Inject
  BusFetcher busFetcher;

  @Inject
  BusModelFactory busModelFactory;

  private final List<BusListener> busListeners = new ArrayList<>();

  private BusTimerTask busTimerTask;

  private final Timer timer = new Timer();

  @Override
  public List<RouteModel> getRoutes() throws TransitRepositoryException {
    try {
      List<Route> routes = routeFetcher.getAll();
      List<RouteModel> models = new ArrayList<>();
      for (Route route : routes) {
        models.add(routeModelFactory.createModel(route));
      }
      return models;
    } catch (FetchException e) {
      throw new TransitRepositoryException(e);
    }
  }

  @Override
  public List<RouteModel> getRoutes(StopModel stop) throws TransitRepositoryException {
    try {
      List<ScheduledRoute> routes = routeFetcher.get(stop.getCode());
      List<RouteModel> models = new ArrayList<>();
      for (Route route : routes) {
        models.add(routeModelFactory.createModel(route));
      }
      return models;
    } catch (FetchException e) {
      throw new TransitRepositoryException(e);
    }
  }

  @Override
  public List<StopModel> getStops() throws TransitRepositoryException {
    try {
      List<StopModel> models = new ArrayList<>();
      for (Route route : routeFetcher.getAll()) {
        List<Stop> stops = stopFetcher.get(route.getShortName());
        for (Stop stop : stops) {
          models.add(stopModelFactory.createModel(stop));
        }
      }
      return models;
    } catch (FetchException e) {
      throw new TransitRepositoryException(e);
    }
  }

  @Override
  public List<StopModel> getStops(RouteModel route) throws TransitRepositoryException {
    try {
      List<Stop> stops = stopFetcher.get(route.getShortName());
      List<StopModel> models = new ArrayList<>();
      for (Stop stop : stops) {
        models.add(stopModelFactory.createModel(stop));
      }
      return models;
    } catch (FetchException e) {
      throw new TransitRepositoryException(e);
    }
  }

  @Override
  public List<DepartureModel> getDepartures(StopModel stop) throws TransitRepositoryException {
    try {
      List<DepartureModel> models = new ArrayList<>();
      for (Route route : routeFetcher.getAll()) {
        List<Departure> departures = departureFetcher.get(route.getShortName(), stop.getCode());
        for (Departure departure : departures) {
          models.add(departureModelFactory.createModel(departure));
        }
      }
      return models;
    } catch (FetchException e) {
      throw new TransitRepositoryException(e);
    }
  }

  @Override
  public List<DepartureModel> getDepartures(RouteModel route, StopModel stop) throws TransitRepositoryException {
    try {
      List<Departure> departures = departureFetcher.get(route.getShortName(), stop.getCode());
      List<DepartureModel> models = new ArrayList<>();
      for (Departure departure : departures) {
        models.add(departureModelFactory.createModel(departure));
      }
      System.out.println(models);
      return models;
    } catch (FetchException e) {
      throw new TransitRepositoryException(e);
    }
  }

  @Override
  public List<BusModel> getBusLocations(RouteModel route) throws TransitRepositoryException {
    try {
      List<Bus> buses = busFetcher.get(route.getShortName());
      List<BusModel> models = new ArrayList<>();
      for (Bus bus : buses) {
        models.add(busModelFactory.createModel(bus));
      }
      return models;
    } catch (FetchException e) {
      throw new TransitRepositoryException(e);
    }
  }

  @Override
  public void registerBusListener(RouteModel route, BusListener busListener) {
    busListeners.add(busListener);
    if (busTimerTask == null) {
      busTimerTask = new BusTimerTask(route, busListeners, this);
      timer.scheduleAtFixedRate(busTimerTask, 0, 1000);
    } else {
      busTimerTask.addListener(busListener);
    }
  }

  @Override
  public void clearBusListener(BusListener busListener) {
    if (busTimerTask != null) {
      busTimerTask.awaitTermination();
    }
    busListeners.remove(busListener);
    if (busListeners.isEmpty()) {
      clearBusListeners();
    } else if (busTimerTask != null) {
      busTimerTask.removeListener(busListener);
    }
  }

  @Override
  public void clearBusListeners() {
    if (busTimerTask != null) {
      busTimerTask.awaitTermination();
      busTimerTask.cancel();
    }
    busListeners.clear();
    busTimerTask = null;
  }
}
