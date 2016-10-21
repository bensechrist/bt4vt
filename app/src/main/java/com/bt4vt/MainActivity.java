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
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.android.vending.billing.IInAppBillingService;
import com.bt4vt.async.AsyncCallback;
import com.bt4vt.async.DonationOptionAsyncTask;
import com.bt4vt.async.FavoritedStopsAsyncTask;
import com.bt4vt.fragment.NavigationDrawerFragment;
import com.bt4vt.fragment.RetainedMapFragment;
import com.bt4vt.repository.TransitRepository;
import com.bt4vt.repository.model.RouteModel;
import com.bt4vt.repository.model.StopModel;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.Calendar;
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
    RetainedMapFragment.TalkToActivity, NavigationDrawerFragment.TalkToActivity,
    View.OnClickListener, ServiceConnection {

  private static final String TAG = "MainActivity";

  public static final String EXTRA_STOP = "com.bt4vt.extra.stop";
  private static final String FIRST_TIME_OPEN_KEY = "first_time_open_app";
  private static final String TIMES_OPENED_KEY = "number_times_opened_app";
  private static final String LAST_PROMPT_KEY = "last_prompt_date_millis";
  public static final String LAST_DONATION_KEY = "last_donation_date_millis";
  private static final String SHOWCASE_ID = "com.bt4vt.nav_showcase";

  @Inject
  private SharedPreferences preferences;

  @Inject
  private ConnectivityManager connectivityManager;

  @Inject
  private TransitRepository transitRepository;

  @InjectView(R.id.drawer_layout)
  private DrawerLayout mDrawerLayout;

  @InjectView(R.id.button_nav_drawer)
  private ImageButton navButton;

  @InjectView(R.id.main_loading_view)
  private View mainLoadingView;

  private RetainedMapFragment mapFragment;

  private NavigationDrawerFragment navFragment;

  private IInAppBillingService inAppBillingService;

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

    Intent serviceIntent =
        new Intent("com.android.vending.billing.InAppBillingService.BIND");
    serviceIntent.setPackage("com.android.vending");
    bindService(serviceIntent, this, Context.BIND_AUTO_CREATE);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (inAppBillingService != null) {
      unbindService(this);
    }
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

    setShortcuts();

    if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) != ConnectionResult.SUCCESS) {
      View view = mapFragment.getView();
      if (view != null) {
        Snackbar.make(view, R.string.no_play_services, Snackbar.LENGTH_LONG).show();
      }
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    switch (requestCode) {
      case RetainedMapFragment.REQUEST_LOCATION_PERMISSION: {
        if (mapFragment != null) {
          if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mapFragment.setUpMap();
          } else if (ActivityCompat.shouldShowRequestPermissionRationale(this,
              Manifest.permission.ACCESS_FINE_LOCATION)) {
            mapFragment.showLocationPermissionRationale();
          }
        }
      }
    }
  }

  @Override
  public void onStopsReady(List<StopModel> stops) {
    mapFragment.showStops(stops);
  }

  @Override
  public void onRouteSelected(RouteModel route) {
    mainLoadingView.setVisibility(View.VISIBLE);
    mapFragment.setCurrentRoute(route);
    mapFragment.clearMap();
    mapFragment.fetchStops(route);
    mapFragment.showBuses(route);
  }

  @Override
  public void showAllStops() {
    mainLoadingView.setVisibility(View.VISIBLE);
    mapFragment.setCurrentRoute(null);
    mapFragment.clearMap();
    mapFragment.fetchStops(null);
  }

  @Override
  public IInAppBillingService getBillingService() {
    return inAppBillingService;
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

  private void setShortcuts() {
    if (Build.VERSION.SDK_INT >= 25) {
      new FavoritedStopsAsyncTask(transitRepository, new AsyncCallback<List<StopModel>>() {
        @TargetApi(25)
        @Override
        public void onSuccess(List<StopModel> stopModels) {
          Log.d(TAG, "models " + stopModels.toString());
          ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);

          List<ShortcutInfo> shortcuts = new ArrayList<>();

          for (StopModel stopModel : stopModels) {
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            intent.setAction(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra(MainActivity.EXTRA_STOP, stopModel.toString());
            ShortcutInfo shortcut = new ShortcutInfo.Builder(MainActivity.this, String.valueOf(stopModel.getCode()))
                .setShortLabel(stopModel.getName())
                .setLongLabel(stopModel.getName())
                .setIcon(Icon.createWithResource(MainActivity.this, R.drawable.bus_stop_icon))
                .setIntent(intent)
                .build();
            shortcuts.add(shortcut);
          }

          shortcutManager.setDynamicShortcuts(shortcuts);

          Log.d(TAG, "shortcuts " + shortcutManager.getDynamicShortcuts());
        }

        @Override
        public void onException(Exception e) {

        }
      }).execute();
    }
  }

  private void initData() {
    if (isNetworkAvailable()) {
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

  @Override
  public void onServiceConnected(ComponentName name, IBinder service) {
    inAppBillingService = IInAppBillingService.Stub.asInterface(service);

    int timesOpened = preferences.getInt(TIMES_OPENED_KEY, 0) + 1;
    preferences.edit()
        .putInt(TIMES_OPENED_KEY, timesOpened)
        .apply();
    Calendar now = Calendar.getInstance();
    Calendar threshold = Calendar.getInstance();
    Calendar lastPrompt = Calendar.getInstance();
    Calendar lastDonation = Calendar.getInstance();
    threshold.add(Calendar.DAY_OF_YEAR, -60);
    lastPrompt.setTimeInMillis(preferences.getLong(LAST_PROMPT_KEY, 0));
    lastDonation.setTimeInMillis(preferences.getLong(LAST_DONATION_KEY, 0));
    boolean sameDay = now.get(Calendar.YEAR) == lastPrompt.get(Calendar.YEAR) &&
        now.get(Calendar.DAY_OF_YEAR) == lastPrompt.get(Calendar.DAY_OF_YEAR);
    Log.d(TAG, String.format("Opened %d times", timesOpened));
    Log.d(TAG, String.format("Last prompted %s", lastPrompt.getTime().toString()));
    Log.d(TAG, String.format("Last donation %s", lastDonation.getTime().toString()));
    if ((timesOpened % 12 == 0) && !sameDay && lastDonation.before(threshold)) {
      preferences.edit()
          .putLong(LAST_PROMPT_KEY, now.getTimeInMillis())
          .apply();
      new AlertDialog.Builder(this)
          .setMessage(R.string.donation_prompt_message)
          .setPositiveButton(R.string.donation_prompt_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              dialog.dismiss();
              new DonationOptionAsyncTask(inAppBillingService, getPackageName(), navFragment).execute();
            }
          })
          .setNegativeButton(R.string.donation_prompt_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              dialog.dismiss();
            }
          })
          .show();
    }
  }

  @Override
  public void onServiceDisconnected(ComponentName name) {
    inAppBillingService = null;
  }
}
