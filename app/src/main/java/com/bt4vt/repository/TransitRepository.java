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

import com.bt4vt.repository.exception.TransitRepositoryException;
import com.bt4vt.repository.listener.BusListener;
import com.bt4vt.repository.model.BusModel;
import com.bt4vt.repository.model.DepartureModel;
import com.bt4vt.repository.model.RouteModel;
import com.bt4vt.repository.model.StopModel;

import java.util.List;

/**
 * Repository for getting transit information.
 *
 * @author Ben Sechrist
 */
public interface TransitRepository {

  /**
   * Returns a list of all transit routes.
   * @return a list of routes
   */
  List<RouteModel> getRoutes() throws TransitRepositoryException;

  /**
   * Returns a list of transit routes that service the given <code>stop</code>.
   * @param stop the stop
   * @return list of routes
   * @throws TransitRepositoryException
   */
  List<RouteModel> getRoutes(StopModel stop) throws TransitRepositoryException;

  /**
   * Returns a list of all stops.
   * @return a list of stops
   */
  List<StopModel> getStops() throws TransitRepositoryException;

  /**
   * Returns a list of all stops for a route.
   * @param route the route to get stops for
   * @return a list of stops
   */
  List<StopModel> getStops(RouteModel route) throws TransitRepositoryException;

  /**
   * Returns a stop for the given stop code.
   * @param stopCode the stop code
   * @return a stop
   * @throws TransitRepositoryException
   */
  StopModel getStop(int stopCode) throws TransitRepositoryException;

  /**
   * Updates the information kept about the stop in the local DB.
   * @param stop the stop to update
   */
  void updateStop(StopModel stop);

  /**
   * Returns a list of the next departures for all routes at a stop.
   * @param stop the stop to get departures for
   * @return a list of departures
   */
  List<DepartureModel> getDepartures(StopModel stop) throws TransitRepositoryException;

  /**
   * Returns a list of the next departures for the given route at a stop.
   * @param route the route to get departures for
   * @param stop the stop to get departures for
   * @return a list of departures
   */
  List<DepartureModel> getDepartures(RouteModel route, StopModel stop) throws TransitRepositoryException;

  /**
   * Returns a list of the current bus locations for the <code>route</code>.
   * @param route the route
   * @return the bus locations
   */
  List<BusModel> getBusLocations(RouteModel route) throws TransitRepositoryException;

  /**
   * Registers the <code>busListener</code> to be called every time new
   * information is available for buses on the <code>route</code>.
   * @param route the route to watch
   * @param busListener the callback listener
   */
  void registerBusListener(RouteModel route, BusListener busListener);

  /**
   * Clears the <code>busListener</code>.
   * @param busListener the listener
   */
  void clearBusListener(BusListener busListener);

  /**
   * Clears all bus listeners.
   */
  void clearBusListeners();
}
