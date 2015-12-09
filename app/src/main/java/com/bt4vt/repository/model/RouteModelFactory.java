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

import android.view.MenuItem;

import com.bt4vt.repository.domain.Route;
import com.google.inject.Singleton;

/**
 * Factory for creating {@link RouteModel} objects.
 *
 * @author Ben Sechrist
 */
@Singleton
public class RouteModelFactory {

  /**
   * Creates a {@link RouteModel} from the given <code>route</code>.
   * @param route the route
   * @return the route model
   */
  public RouteModel createModel(Route route) {
    RouteModelImpl model = new RouteModelImpl();
    model.setName(route.getName());
    model.setShortName(route.getShortName());
    return model;
  }

  /**
   * Creates a {@link RouteModel} from the given <code>menuItem</code>.
   * @param menuItem the menu item
   * @return the route model
   */
  public RouteModel createModel(MenuItem menuItem) {
    RouteModelImpl model = new RouteModelImpl();
    model.setName(String.valueOf(menuItem.getTitle()));
    model.setShortName(String.valueOf(menuItem.getTitleCondensed()));
    return model;
  }
}
