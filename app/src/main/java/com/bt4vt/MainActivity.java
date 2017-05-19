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
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
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
import android.os.RemoteException;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.bt4vt.external.bt4u.Bus;
import com.bt4vt.external.bt4u.BusService;
import com.bt4vt.external.bt4u.Response;
import com.bt4vt.external.bt4u.Route;
import com.bt4vt.external.bt4u.RouteService;
import com.bt4vt.external.bt4u.Stop;
import com.bt4vt.external.bt4u.StopService;
import com.bt4vt.fragment.NavigationDrawerFragment;
import com.bt4vt.fragment.RetainedMapFragment;
import com.bt4vt.fragment.ScheduledDeparturesDialogFragment;
import com.bt4vt.geofence.BusStopGeofenceService;
import com.bt4vt.model.FavoriteStop;
import com.bt4vt.service.FavoriteStopService;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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
    View.OnClickListener {

  private static final String TAG = "MainActivity";

  private static final String DEPARTURES_DIALOG_TAG = "scheduled_departures_dialog_tag";

  public static final String EXTRA_STOP_CODE = "com.bt4vt.extra.stop";
  private static final String FIRST_TIME_OPEN_KEY = "first_time_open_app";
  private static final String SHOWCASE_ID = "com.bt4vt.nav_showcase";
  private static final int PURCHASE_REQUEST_CODE = 1001;

  @Inject
  private SharedPreferences preferences;

  @Inject
  private ConnectivityManager connectivityManager;

  @Inject
  private BusService busService;

  @Inject
  private RouteService routeService;

  @Inject
  private StopService stopService;

  @Inject
  private FavoriteStopService favoriteStopService;

  private BusStopGeofenceService busStopGeofenceService;

  @InjectView(R.id.drawer_layout)
  private DrawerLayout mDrawerLayout;

  @InjectView(R.id.button_nav_drawer)
  private ImageButton navButton;

  @InjectView(R.id.refresh_route_button)
  private FloatingActionButton refreshRouteButton;

  @InjectView(R.id.main_loading_view)
  private View mainLoadingView;

  @InjectView(R.id.banner_ad)
  private AdView bannerAd;

  private RetainedMapFragment mapFragment;

  private NavigationDrawerFragment navFragment;

  private Route currentRoute;

  private Timer busRefreshTimer;

  private IInAppBillingService billingService;

  private ServiceConnection billingServiceConnection;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    navButton.setOnClickListener(this);
    refreshRouteButton.setOnClickListener(this);

    if (navFragment == null) {
      navFragment = (NavigationDrawerFragment) getSupportFragmentManager()
          .findFragmentById(R.id.left_drawer);
    }

    if (mapFragment == null) {
      mapFragment = (RetainedMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
    }

    initData();

    busStopGeofenceService = new BusStopGeofenceService(this);

    checkForPurchase();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (billingService != null) {
      unbindService(billingServiceConnection);
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (busRefreshTimer != null) {
      busRefreshTimer.cancel();
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

    if (currentRoute != null) {
      startBusRefreshTask(currentRoute);
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
  public void onRouteSelected(final Route route) {
    onRouteSelected(route, false);
  }

  private void onRouteSelected(final Route route, boolean ignoreCache) {
    mainLoadingView.setVisibility(View.VISIBLE);
    refreshRouteButton.setVisibility(View.VISIBLE);
    currentRoute = route;
    mapFragment.clearMap();
    routeService.get(route.getShortName(), ignoreCache, new Response.Listener<Route>() {
      @Override
      public void onResult(Route route) {
        List<Stop> stops = route.getStops();
        if (stops.isEmpty()) {
          View view = mapFragment.getView();
          if (view != null) {
            Snackbar.make(view, R.string.no_stops, Snackbar.LENGTH_LONG)
                .show();
          }
          hideLoadingIcon();
          return;
        }
        mapFragment.showRoutePlot(route.getPlot(), route.getColor());
        mapFragment.showStops(route.getStops());
      }
    }, new ExceptionHandler(getString(R.string.stops_error), mapFragment.getView(), Snackbar.LENGTH_LONG));
    startBusRefreshTask(route);
  }

  @Override
  public void showAllStops() {
    mainLoadingView.setVisibility(View.VISIBLE);
    currentRoute = null;
    mapFragment.clearMap();
    stopService.getAll(new Response.Listener<List<Stop>>() {
      @Override
      public void onResult(List<Stop> stops) {
        mapFragment.showStops(stops);
      }
    }, new ExceptionHandler(getString(R.string.stops_error), mapFragment.getView(),
        Snackbar.LENGTH_LONG));
  }

  @Override
  public void fetchRoutes(boolean ignoreCache) {
    navFragment.showRoutesLoading();
    routeService.getAll(ignoreCache, new Response.Listener<List<Route>>() {
      @Override
      public void onResult(List<Route> result) {
        Collections.sort(result);
        navFragment.setRouteNames(result);
      }
    }, new ExceptionHandler(getString(R.string.routes_error), mapFragment.getView(),
        Snackbar.LENGTH_INDEFINITE));
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
    } else if (v.getId() == refreshRouteButton.getId()) {
      onRouteSelected(currentRoute, true);
    } else {
      initData();
      checkNetwork();
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == PURCHASE_REQUEST_CODE) {
      if (resultCode == RESULT_OK) {
        checkForPurchase();
        View view = mapFragment.getView();
        if (view != null) {
          Snackbar.make(view, R.string.purchase_dialog_thank_you, Snackbar.LENGTH_LONG)
              .show();
        }
      }
    }
  }

  @Override
  public void promptPurchase() {
    if (billingService == null) {
      if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) != ConnectionResult.SUCCESS) {
        Log.d(TAG, "Google Play Services is not available");
        return;
      }
      Log.d(TAG, "Connecting to billing service...");
      Intent serviceIntent =
          new Intent("com.android.vending.billing.InAppBillingService.BIND");
      serviceIntent.setPackage("com.android.vending");
      billingServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
          Log.d(TAG, "Connected to billing service");
          billingService = IInAppBillingService.Stub.asInterface(service);
          promptPurchase();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
          billingService = null;
        }
      };
      bindService(serviceIntent, billingServiceConnection, Context.BIND_AUTO_CREATE);
    } else {
      Log.d(TAG, "Checking for past purchases");
      try {
        Bundle purchases = billingService.getPurchases(3, getPackageName(), "inapp", null);
        if (purchases.getInt("RESPONSE_CODE") == 0) {
          List<String> purchaseList = purchases.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
          if (purchaseList != null) {
            if (purchaseList.size() > 0) {
              Log.d(TAG, "User already purchased");
              View view = mapFragment.getView();
              if (view != null) {
                Snackbar.make(view, R.string.purchase_dialog_already_purchased, Toast.LENGTH_LONG)
                    .show();
              }
            } else {
              new AlertDialog.Builder(this)
                  .setTitle(R.string.purchase_dialog_title)
                  .setMessage(R.string.purchase_dialog_message)
                  .setPositiveButton(R.string.purchase_dialog_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                      try {
                        String productId = (BuildConfig.DEBUG ? "android.test.purchased" : getString(R.string.purchase_dialog_product_id));
                        Bundle buyIntentBundle = billingService.getBuyIntent(3, getPackageName(),
                            productId, "inapp", null);
                        int response = buyIntentBundle.getInt("RESPONSE_CODE");
                        if (response == 0) {
                          PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                          startIntentSenderForResult(pendingIntent.getIntentSender(),
                              PURCHASE_REQUEST_CODE, new Intent(), 0, 0, 0, null);
                        }
                      } catch (RemoteException | IntentSender.SendIntentException e) {
                        Log.e(TAG, e.getLocalizedMessage());
                        View view = mapFragment.getView();
                        if (view != null) {
                          Snackbar.make(view, R.string.purchase_dialog_purchase_error, Snackbar.LENGTH_LONG)
                              .show();
                        }
                      }
                    }
                  })
                  .setNegativeButton(R.string.purchase_dialog_negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                      View view = mapFragment.getView();
                      if (view != null) {
                        Snackbar.make(view, R.string.purchase_dialog_negative_response, Toast.LENGTH_SHORT)
                            .show();
                      }
                    }
                  })
                  .show();
            }
          }
        }
      } catch (RemoteException e) {
        e.printStackTrace();
      }
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

  private void checkForPurchase() {
    if (billingService == null) {
      if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) != ConnectionResult.SUCCESS) {
        Log.d(TAG, "Google Play Services is not available");
        showBannerAd();
        return;
      }
      Log.d(TAG, "Connecting to billing service...");
      Intent serviceIntent =
          new Intent("com.android.vending.billing.InAppBillingService.BIND");
      serviceIntent.setPackage("com.android.vending");
      billingServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
          Log.d(TAG, "Connected to billing service");
          billingService = IInAppBillingService.Stub.asInterface(service);
          checkForPurchase();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
          billingService = null;
        }
      };
      bindService(serviceIntent, billingServiceConnection, Context.BIND_AUTO_CREATE);
    } else {
      Log.d(TAG, "Checking for past purchases");
      try {
        Bundle purchases = billingService.getPurchases(3, getPackageName(), "inapp", null);
        if (purchases.getInt("RESPONSE_CODE") == 0) {
          List<String> purchaseList = purchases.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
          if (purchaseList != null) {
            if (purchaseList.size() == 0) {
              Log.d(TAG, "No past purchases");
              showBannerAd();
              navFragment.showRemoveAdsMenuItem();
            } else {
              Log.d(TAG, "Past purchases");
              Log.d(TAG, purchaseList.toString());
              bannerAd.destroy();
            }
          }
        }
      } catch (RemoteException e) {
        e.printStackTrace();
      }
    }
  }

  private void showBannerAd() {
    Log.d(TAG, "Loading banner ad...");
    MobileAds.initialize(getApplicationContext(), getString(R.string.admob_app_id));
    AdRequest adRequest = new AdRequest.Builder().build();
    bannerAd.loadAd(adRequest);
  }

  private void startBusRefreshTask(final Route route) {
    if (busRefreshTimer != null) {
      busRefreshTimer.cancel();
    }
    busRefreshTimer = new Timer();
    busRefreshTimer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        Log.d(TAG, "Refreshing buses for route " + route.getShortName());
        busService.get(route.getShortName(), new Response.Listener<List<Bus>>() {
          @Override
          public void onResult(List<Bus> buses) {
            mapFragment.showBuses(buses);
          }
        }, new ExceptionHandler(getString(R.string.bus_error), mapFragment.getView(),
            Snackbar.LENGTH_SHORT));
      }
    }, 0, getResources().getInteger(R.integer.bus_refresh_rate_ms));
  }

  private void setShortcuts() {
    if (Build.VERSION.SDK_INT >= 25) {
      stopService.getAll(new Response.Listener<List<Stop>>() {
        @TargetApi(Build.VERSION_CODES.N_MR1)
        @Override
        public void onResult(List<Stop> result) {
          List<FavoriteStop> favoriteStops = favoriteStopService.getFavoriteStops();
          ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);

          int maxShortcutCountPerActivity = shortcutManager.getMaxShortcutCountPerActivity();
          List<ShortcutInfo> shortcuts = new ArrayList<>();

          for (FavoriteStop favoriteStop : favoriteStops) {
            Stop stopInfo = null;
            for (Stop stop : result) {
              if (stop.getCode().equals(favoriteStop.getCode())) {
                stopInfo = stop;
                break;
              }
            }
            if (stopInfo == null) {
              continue;
            }
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            intent.setAction(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra(MainActivity.EXTRA_STOP_CODE, stopInfo.getCode());
            ShortcutInfo shortcut = new ShortcutInfo.Builder(MainActivity.this, stopInfo.getCode())
                .setShortLabel(stopInfo.getName())
                .setLongLabel(stopInfo.getName())
                .setIcon(Icon.createWithResource(MainActivity.this, R.drawable.bus_stop_icon))
                .setIntent(intent)
                .build();
            shortcuts.add(shortcut);
            // Once we reach the max stop adding shortcuts
            if (shortcuts.size() >= maxShortcutCountPerActivity)
              break;
          }

          shortcutManager.setDynamicShortcuts(shortcuts);
        }
      }, new Response.ExceptionListener() {
        @Override
        public void onException(Exception e) {
          e.printStackTrace();
        }
      });
    }
  }

  private void initData() {
    if (isNetworkAvailable()) {
      Intent intent = getIntent();
      String stopCode = intent.getStringExtra(EXTRA_STOP_CODE);
      if (stopCode != null) {
        Log.i(TAG, String.format("Stop code: %s", stopCode));
        mainLoadingView.setVisibility(View.VISIBLE);
        stopService.get(stopCode, new Response.Listener<Stop>() {
          @Override
          public void onResult(Stop result) {
            Log.d(TAG, result.toString());
            mapFragment.showStops(Collections.singletonList(result));
          }
        }, new ExceptionHandler(getString(R.string.stop_error), mapFragment.getView(),
            Snackbar.LENGTH_LONG));
      }
      fetchRoutes(false);
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

  @Override
  public void showDeparturesDialog(Stop stop, Route route) {
    ScheduledDeparturesDialogFragment.newInstance(stop, route, busStopGeofenceService)
        .show(getSupportFragmentManager(), DEPARTURES_DIALOG_TAG);
  }

  @Override
  public Route getCurrentRoute() {
    return currentRoute;
  }

  private boolean isNetworkAvailable() {
    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
  }

  private class ExceptionHandler implements Response.ExceptionListener {

    private String message;
    private View view;
    private int displayLength;

    ExceptionHandler(String message, View view, int displayLength) {
      this.message = message;
      this.view = view;
      this.displayLength = displayLength;
    }

    @Override
    public void onException(Exception e) {
      e.printStackTrace();
      hideLoadingIcon();
      if (view != null) {
        Snackbar.make(view, message, displayLength)
            .show();
      }
    }
  }
}
