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

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.ImageButton;

import com.bt4vt.fragment.NavigationDrawerFragment;
import com.bt4vt.fragment.RetainedMapFragment;
import com.bt4vt.repository.FirebaseService;
import com.bt4vt.repository.domain.Route;
import com.bt4vt.repository.domain.Stop;
import com.google.inject.Inject;

import java.util.List;

import roboguice.activity.RoboFragmentActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

/**
 * Handles the layout {@link com.bt4vt.R.layout#activity_main}.
 *
 * @author Ben Sechrist
 */
@ContentView(R.layout.activity_main)
public class MainActivity extends RoboFragmentActivity implements
    RetainedMapFragment.TalkToActivity, NavigationDrawerFragment.TalkToActivity, View.OnClickListener {

  public static final String EXTRA_STOP = "com.bt4vt.extra.stop";
  private static final String FIRST_TIME_OPEN_KEY = "first_time_open_app";
  private static final String SHOWCASE_ID = "com.bt4vt.nav_showcase";

  @Inject
  private SharedPreferences preferences;

  @Inject
  private ConnectivityManager connectivityManager;

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

    navButton.setOnClickListener(this);

    if (navFragment == null) {
      navFragment = (NavigationDrawerFragment) getSupportFragmentManager()
          .findFragmentById(R.id.left_drawer);
    }

    if (mapFragment == null) {
      mapFragment = (RetainedMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
    }

    initData();
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

    new MaterialShowcaseView.Builder(this)
        .setTarget(navButton)
        .setDismissText(R.string.showcase_confirm)
        .setContentText(R.string.nav_button_showcase_text)
        .setDelay(500)
        .singleUse(SHOWCASE_ID)
        .show();
    if (preferences.contains(FIRST_TIME_OPEN_KEY)) {
      preferences.edit().remove(FIRST_TIME_OPEN_KEY).apply();
    }

    checkNetwork();
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    switch (requestCode) {
      case RetainedMapFragment.REQUEST_LOCATION_PERMISSION: {
        if (mapFragment != null && grantResults.length > 0
            && grantResults[0] == PackageManager.PERMISSION_DENIED) {
          if (ActivityCompat.shouldShowRequestPermissionRationale(this,
              Manifest.permission.ACCESS_FINE_LOCATION)) {
            mapFragment.showLocationPermissionRationale();
          }
        }
      }
    }
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
  public void showAllStops() {
    mainLoadingView.setVisibility(View.VISIBLE);
    mapFragment.setCurrentRoute(null);
    mapFragment.clearMap();
    mapFragment.fetchStops(null);
  }

  @Override
  public void onClick(View v) {
    if (v.getId() == navButton.getId()) {
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
    } else {
      initData();
      checkNetwork();
    }
  }

  private void checkNetwork() {
    if (!isNetworkAvailable()) {
      View view = mapFragment.getView();
      if (view != null) {
        Snackbar.make(view, R.string.no_network_message, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.retry, this)
            .show();
      }
    }
  }

  private void initData() {
    if (isNetworkAvailable()) {
      Intent serviceIntent = new Intent(this, FirebaseService.class);
      startService(serviceIntent);

      Intent intent = getIntent();
      String stopString = intent.getStringExtra(EXTRA_STOP);
      if (stopString != null) {
        mainLoadingView.setVisibility(View.VISIBLE);
        mapFragment.fetchStop(stopString);
      }
      navFragment.fetchRoutes();
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

  @Override
  public void showLoadingIcon() {
    mainLoadingView.setVisibility(View.VISIBLE);
  }

  private boolean isNetworkAvailable() {
    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
  }
}
