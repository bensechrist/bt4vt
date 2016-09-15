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

package com.bt4vt.fragment;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.bt4vt.R;
import com.bt4vt.async.AsyncCallback;
import com.bt4vt.async.StopsAsyncTask;
import com.bt4vt.repository.TransitRepository;
import com.bt4vt.repository.listener.BusListener;
import com.bt4vt.repository.model.BusModel;
import com.bt4vt.repository.model.RouteModel;
import com.bt4vt.repository.model.StopModel;
import com.bt4vt.repository.model.StopModelFactory;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.inject.Inject;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import roboguice.RoboGuice;
import roboguice.inject.InjectResource;

/**
 * Handles all google map related actions.
 *
 * @author Ben Sechrist
 */
public class RetainedMapFragment extends SupportMapFragment implements OnMapReadyCallback,
    BusListener, GoogleMap.OnInfoWindowClickListener, View.OnClickListener {

  public static final int REQUEST_LOCATION_PERMISSION = 1;

  private static final String DEPARTURES_DIALOG_TAG = "scheduled_departures_dialog_tag";
  private static final double BBURG_LAT = 37.2304516;
  private static final double BBURG_LNG = -80.4294548;
  private static final float BBURG_ZOOM = 13;

  @Inject
  private TransitRepository transitRepository;

  @Inject
  private StopModelFactory stopModelFactory;

  @InjectResource(R.string.stop_marker_snippet)
  private String stopMarkerSnippet;

  private GoogleMap mMap; // Might be null if Google Play services APK is not available.

  private final List<Marker> currentStopMarkers = new ArrayList<>();

  private final List<Marker> currentBusMarkers = new ArrayList<>();

  private Polyline currentRoutePattern;

  private TalkToActivity activity;

  private RouteModel currentRoute;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    RoboGuice.getInjector(getActivity()).injectMembers(this);

    super.onCreate(savedInstanceState);
    setRetainInstance(true);
  }

  @Override
  public void onResume() {
    super.onResume();
    if (currentRoute != null) {
      showBuses(currentRoute);
    }
  }

  @Override
  public void onPause() {
    super.onPause();
    transitRepository.clearBusListener(this);
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    this.activity = (TalkToActivity) context;
  }

  @Override
  public void onDetach() {
    super.onDetach();
    this.activity = null;
    transitRepository.clearBusListener(this);
  }

  @Override
  public void onMapReady(GoogleMap googleMap) {
    this.mMap = googleMap;
    if (checkLocationPermission())
      setUpMap();
  }

  @Override
  public void onClick(View view) {
    requestLocationPermission();
  }

  /**
   * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
   * installed) and the map has not already been instantiated.. This will ensure that we only ever
   * call getMapAsync() once when {@link #mMap} is null.
   * <p/>
   * If it isn't installed {@link SupportMapFragment} (and
   * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
   * install/update the Google Play services APK on their device.
   * <p/>
   * A user can return to this FragmentActivity after following the prompt and correctly
   * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
   * have been completely destroyed during this process (it is likely that it would only be
   * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
   * method in {@link #onResume()} to guarantee that it will be called.
   */
  public void setUpMapIfNeeded() {
    // Do a null check to confirm that we have not already instantiated the map.
    if (mMap == null) {
      // Try to obtain the map from the SupportMapFragment.
      getMapAsync(this);
    }
  }

  public void setCurrentRoute(RouteModel currentRoute) {
    this.currentRoute = currentRoute;
  }

  /**
   * This gets all stops on the given <code>route</code>.
   * If route is null it will get all stops.
   *
   * @param route the route
   */
  public void fetchStops(final RouteModel route) {
    final View.OnClickListener retryListener = new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        activity.showLoadingIcon();
        fetchStops(route);
      }
    };
    StopsAsyncTask task = new StopsAsyncTask(transitRepository, new AsyncCallback<List<StopModel>>() {
      @Override
      public void onSuccess(List<StopModel> stops) {
        if (isAdded()) {
          activity.onStopsReady(stops);
        }
      }

      @Override
      public void onException(Exception e) {
        if (isAdded()) {
          activity.hideLoadingIcon();
        }
        e.printStackTrace();
        View view = getView();
        if (view != null) {
          Snackbar.make(view, R.string.stops_error, Snackbar.LENGTH_LONG)
              .setAction(R.string.retry, retryListener)
              .show();
        }
      }
    });
    task.execute(route);
  }

  public void fetchStop(final String stopString) {
    final View.OnClickListener retryListener = new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        activity.showLoadingIcon();
        fetchStop(stopString);
      }
    };
    StopsAsyncTask task = new StopsAsyncTask(transitRepository, new AsyncCallback<List<StopModel>>() {
      @Override
      public void onSuccess(List<StopModel> stops) {
        if (!isAdded()) {
          return;
        }
        for (StopModel stop : stops) {
          if (stop.toString().equals(stopString)) {
            activity.onStopsReady(Collections.singletonList(stop));
            return;
          }
        }
        activity.onStopsReady(Collections.<StopModel>emptyList());
      }

      @Override
      public void onException(Exception e) {
        if (isAdded()) {
          activity.hideLoadingIcon();
        }
        e.printStackTrace();
        View view = getView();
        if (view != null) {
          Snackbar.make(view, R.string.stop_error, Snackbar.LENGTH_LONG)
              .setAction(R.string.retry, retryListener)
              .show();
        }
      }
    });
    task.execute(null, null);
  }

  /**
   * This adds markers for the given <code>stops</code>.
   *
   * @param stops the stops
   */
  public void showStops(List<StopModel> stops) {
    if (mMap == null) {
      return;
    }

    if (stops.isEmpty()) {
      View view = getView();
      if (view != null) {
        Snackbar.make(view, R.string.no_stops, Snackbar.LENGTH_LONG)
            .show();
      }
      if (isAdded()) {
        activity.hideLoadingIcon();
      }
      return;
    }

    if (currentRoutePattern == null && areFromSameRoute(stops)) {
      showRoutePattern(stops.get(0));
    }

    LatLngBounds.Builder builder = new LatLngBounds.Builder();
    for (StopModel stop : stops) {
      Marker marker = mMap.addMarker(getStopMarker(stop));
      currentStopMarkers.add(marker);
      builder.include(marker.getPosition());
    }
    LatLngBounds bounds = builder.build();
    int padding = 100; // offset from edges of the map in pixels
    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
    mMap.animateCamera(cu);

    if (isAdded()) {
      activity.hideLoadingIcon();
    }
  }

  @Override
  public void onInfoWindowClick(Marker marker) {
    if (currentStopMarkers.contains(marker)) {
      StopModel stop = stopModelFactory.createModel(marker);
      ScheduledDeparturesDialogFragment.newInstance(stop.getCode(), this.currentRoute)
          .show(getFragmentManager(), DEPARTURES_DIALOG_TAG);
    }
  }

  /**
   * This shows all buses on the given <code>route</code>.
   *
   * @param route the route
   */
  public void showBuses(RouteModel route) {
    if (mMap == null) {
      return;
    }

    transitRepository.registerBusListener(route, this);
  }

  @Override
  public void onUpdateBuses(final List<BusModel> buses) {
    if (mMap == null) {
      transitRepository.clearBusListener(this);
      return;
    }

    if (buses.isEmpty() || !isAdded()) {
      return;
    }

    if (currentRoute != null && !buses.get(0).getRouteShortName().equals(currentRoute.getShortName())) {
      return;
    }

    new Handler(Looper.getMainLooper()).post(new Runnable() {
      @Override
      public void run() {
        if (!isAdded()) {
          return;
        }

        if (currentBusMarkers.size() != buses.size()) {
          for (BusModel bus : buses) {
            Marker marker = mMap.addMarker(getBusMarker(bus));
            currentBusMarkers.add(marker);
          }
        } else {
          for (int i = 0; i < currentBusMarkers.size(); i++) {
            Marker marker = currentBusMarkers.get(i);
            BusModel bus = buses.get(i);
            marker.setPosition(bus.getLatLng());
            marker.setTitle(getString(R.string.bus_marker_title_format, bus.getRouteShortName(), bus.getId()));
            marker.setSnippet(getString(R.string.bus_marker_snippet_format, bus.getPassengers(),
                bus.getLastUpdated()));
            marker.setRotation(bus.getDirection());
          }
        }
      }
    });
  }

  public void clearMap() {
    if (mMap != null) {
      clearStops();
      clearBuses();
      clearRoute();
      mMap.clear();
    }
  }

  /**
   * Displays location permission rationale to the user.
   */
  public void showLocationPermissionRationale() {
    View view = getView();
    if (view != null) {
      Snackbar.make(view, R.string.location_permission_rationale, Snackbar.LENGTH_INDEFINITE)
          .setAction(R.string.permission_grant, this)
          .show();
    }
  }

  /**
   * This draws the pattern from the given stop on the map.
   *
   * @param stop the stop
   */
  private void showRoutePattern(StopModel stop) {
    if (mMap == null) {
      return;
    }

    PolylineOptions polylineOptions = new PolylineOptions();
    polylineOptions.addAll(PolyUtil.decode(stop.getRoutePattern()));
    polylineOptions.width(8);
    if (stop.getRoutePatternColor() != null)
      polylineOptions.color(stop.getRoutePatternColor());
    else
      polylineOptions.color(ContextCompat.getColor(getContext(), R.color.AccentColor));
    currentRoutePattern = mMap.addPolyline(polylineOptions);
  }

  /**
   * This is where we initialize the map.
   * <p/>
   * This should only be called once and when we are sure that {@link #mMap} is not null.
   */
  public void setUpMap() {
    mMap.setMyLocationEnabled(true);
    mMap.getUiSettings().setCompassEnabled(false);
    mMap.getUiSettings().setRotateGesturesEnabled(false);
    mMap.getUiSettings().setTiltGesturesEnabled(false);
    mMap.setOnInfoWindowClickListener(this);

    CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(new LatLng(BBURG_LAT, BBURG_LNG), BBURG_ZOOM);
    mMap.animateCamera(cu);
  }

  /**
   * Verify that the user has granted coarse and find location permissions.
   * <p/>
   * If permissions are not granted and the user hasn't denied them in the past, ask for permissions.
   * @return true if permission was granted, false otherwise
   */
  private boolean checkLocationPermission() {
    if (ContextCompat.checkSelfPermission(getActivity(),
          Manifest.permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED) {
      if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
          Manifest.permission.ACCESS_FINE_LOCATION)) {
        showLocationPermissionRationale();
      } else {
        requestLocationPermission();
      }
      return false;
    }
    return true;
  }

  /**
   * Requests the coarse and fine location permissions.
   */
  private void requestLocationPermission() {
    ActivityCompat.requestPermissions(getActivity(),
        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION},
        REQUEST_LOCATION_PERMISSION);
  }

  /**
   * Removes all stop markers from the map.
   */
  private void clearStops() {
    removeMarkers(currentStopMarkers);
  }

  /**
   * Removes all bus markers from the map.
   */
  private void clearBuses() {
    transitRepository.clearBusListener(this);
    removeMarkers(currentBusMarkers);
  }

  private void clearRoute() {
    currentRoutePattern = null;
  }

  private void removeMarkers(List<Marker> markers) {
    markers.clear();
  }

  private boolean areFromSameRoute(List<StopModel> stops) {
    return stops.get(0).getRoutePattern().equals(stops.get(stops.size()-1).getRoutePattern());
  }

  /**
   * Returns a prebuilt {@link MarkerOptions} for the given <code>stop</code>.
   *
   * @param stop the stop
   * @return the marker options
   */
  private MarkerOptions getStopMarker(StopModel stop) {
    BitmapFactory.Options opts = new BitmapFactory.Options();
    opts.inSampleSize = 6;
    return new MarkerOptions()
        .position(new LatLng(stop.getLatitude(), stop.getLongitude()))
        // DO NOT CHANGE TITLE: Title being used on click to retrieve scheduled departures
        .title(stop.toString())
        .snippet(stopMarkerSnippet)
        .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(),
            R.drawable.bus_stop_icon, opts)));
  }

  /**
   * Returns a prebuilt {@link MarkerOptions} for the given <code>bus</code>.
   *
   * @param bus the bus
   * @return the marker options
   */
  private MarkerOptions getBusMarker(BusModel bus) {
    return new MarkerOptions()
        .position(bus.getLatLng())
        .rotation(bus.getDirection())
        .infoWindowAnchor(0.5f, 0.5f)
        .zIndex(10)
        .title(getString(R.string.bus_marker_title_format, bus.getRouteShortName(), bus.getId()))
        .snippet(getString(R.string.bus_marker_snippet_format, bus.getPassengers(),
            bus.getLastUpdated()))
        .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(),
            R.drawable.bus_arrow)));
  }

  /**
   * Used to communicate with the main activity from a fragment.
   *
   * @author Ben Sechrist
   */
  public interface TalkToActivity {

    /**
     * Gives the stops that were fetched.
     *
     * @param stops the stops
     */
    void onStopsReady(List<StopModel> stops);

    /**
     * Hides the loading icon.
     */
    void hideLoadingIcon();

    /**
     * Shows the loading icon.
     */
    void showLoadingIcon();
  }
}
