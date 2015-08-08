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

package com.bt4vt;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.ImageButton;

import com.bt4vt.fragment.NavigationDrawerFragment;
import com.bt4vt.fragment.RetainedMapFragment;
import com.bt4vt.repository.FirebaseService;
import com.bt4vt.repository.domain.Route;
import com.bt4vt.repository.domain.Stop;

import java.util.List;

import roboguice.activity.RoboFragmentActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

/**
 * Handles the layout {@link com.bt4vt.R.layout#activity_main}.
 *
 * @author Ben Sechrist
 */
@ContentView(R.layout.activity_main)
public class MainActivity extends RoboFragmentActivity implements
    RetainedMapFragment.TalkToActivity, NavigationDrawerFragment.TalkToActivity, View.OnClickListener {

  public static final String EXTRA_STOP = "com.bt4vt.extra.stop";

  @InjectView(R.id.drawer_layout)
  private DrawerLayout mDrawerLayout;

  @InjectView(R.id.button_nav_drawer)
  private ImageButton navButton;

  @InjectView(R.id.main_loading_view)
  private View mainLoadingView;

  private RetainedMapFragment mapFragment;

  private NavigationDrawerFragment navFragment;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Intent serviceIntent = new Intent(this, FirebaseService.class);
    startService(serviceIntent);

    navButton.setOnClickListener(this);

    if (navFragment == null) {
      navFragment = (NavigationDrawerFragment) getSupportFragmentManager()
          .findFragmentById(R.id.left_drawer);
    }

    if (mapFragment == null) {
      mapFragment = (RetainedMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
    }

    Intent intent = getIntent();
    String stopString = intent.getStringExtra(EXTRA_STOP);
    if (stopString != null) {
      mainLoadingView.setVisibility(View.VISIBLE);
      mapFragment.fetchStop(stopString);
    }
    navFragment.fetchRoutes();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    Intent serviceIntent = new Intent(this, FirebaseService.class);
    stopService(serviceIntent);
  }

  @Override
  protected void onResume() {
    super.onResume();
    mapFragment.setUpMapIfNeeded();
  }

  @Override
  public void onStopsReady(List<Stop> stops) {
    mapFragment.showStops(stops);
  }

  @Override
  public void onRouteSelected(String routeName) {
    Route currentRoute = new Route(routeName);
    mainLoadingView.setVisibility(View.VISIBLE);
    mapFragment.setCurrentRoute(currentRoute);
    mapFragment.clearMap();
    mapFragment.fetchStops(currentRoute);
    mapFragment.showBuses(currentRoute);
  }

  @Override
  public void onClick(View v) {
    if (navFragment != null) {
      View view = navFragment.getView();
      if (view != null) {
        if (mDrawerLayout.isDrawerOpen(view)) {
          closeDrawer();
        } else {
          mDrawerLayout.openDrawer(view);
        }
      }
    }
  }

  @Override
  public void closeDrawer() {
    if (navFragment != null) {
      View view = navFragment.getView();
      if (view != null) {
        mDrawerLayout.closeDrawer(navFragment.getView());
      }
    }
  }

  @Override
  public boolean isLoadingContent() {
    return (mainLoadingView.getVisibility() == View.VISIBLE);
  }

  @Override
  public void hideLoadingIcon() {
    mainLoadingView.setVisibility(View.INVISIBLE);
  }
}
