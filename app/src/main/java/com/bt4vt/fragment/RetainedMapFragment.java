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
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.bt4vt.R;
import com.bt4vt.external.bt4u.Bus;
import com.bt4vt.external.bt4u.Route;
import com.bt4vt.external.bt4u.Stop;
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
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import roboguice.RoboGuice;
import roboguice.inject.InjectResource;

/**
 * Handles all google map related actions.
 *
 * @author Ben Sechrist
 */
public class RetainedMapFragment extends SupportMapFragment implements OnMapReadyCallback,
    GoogleMap.OnInfoWindowClickListener, View.OnClickListener {

  public static final int REQUEST_LOCATION_PERMISSION = 1;

  private static final double BBURG_LAT = 37.2304516;
  private static final double BBURG_LNG = -80.4294548;
  private static final float BBURG_ZOOM = 13;

  @InjectResource(R.string.stop_marker_snippet)
  private String stopMarkerSnippet;

  private GoogleMap mMap; // Might be null if Google Play services APK is not available.

  private final Map<Marker, Stop> currentStopMarkers = new HashMap<>();

  private final Map<Bus, Marker> currentBusMarkers = new HashMap<>();

  private TalkToActivity activity;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    RoboGuice.getInjector(getActivity()).injectMembers(this);

    super.onCreate(savedInstanceState);
    setRetainInstance(true);
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

  /**
   * This adds markers for the given <code>stops</code>.
   *
   * @param stops the stops
   */
  public void showStops(final List<Stop> stops) {
    if (!isAdded()) {
      return;
    }
    if (mMap == null) {
      final RetainedMapFragment that = this;
      getMapAsync(new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
          that.onMapReady(googleMap);
          mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
              showStops(stops);
            }
          });
        }
      });
      return;
    }

    if (stops.isEmpty()) {
      if (isAdded()) {
        activity.hideLoadingIcon();
      }
      View view = getView();
      if (view != null) {
        Snackbar.make(view, R.string.no_stops, Snackbar.LENGTH_LONG)
            .show();
      }
      return;
    }

    LatLngBounds.Builder builder = new LatLngBounds.Builder();
    for (Stop stop : stops) {
      Marker marker = mMap.addMarker(getStopMarker(stop));
      currentStopMarkers.put(marker, stop);
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
    if (currentStopMarkers.containsKey(marker)) {
      Stop stop = currentStopMarkers.get(marker);
      activity.showDeparturesDialog(stop, activity.getCurrentRoute());
    }
  }

  /**
   * This shows all buses.
   *
   * @param buses the buses
   */
  public void showBuses(List<Bus> buses) {
    if (mMap == null || buses.isEmpty() || !isAdded()) {
      return;
    }

    if (!buses.get(0).getRoute().equals(activity.getCurrentRoute())) {
      return;
    }

    if (currentBusMarkers.size() != buses.size()) {
      for (Bus bus : buses) {
        Marker marker = mMap.addMarker(getBusMarker(bus));
        currentBusMarkers.put(bus, marker);
      }
    } else {
      for (Bus bus : buses) {
        Marker marker = currentBusMarkers.get(bus);
        if (marker == null) {
          marker = mMap.addMarker(getBusMarker(bus));
          currentBusMarkers.put(bus, marker);
        } else {
          marker.setPosition(bus.getLatLng());
          marker.setTitle(getString(R.string.bus_marker_title_format, bus.getRoute().getFullName(),
              bus.getId()));
          marker.setSnippet(getString(R.string.bus_marker_snippet_format, bus.getPassengers(),
              SimpleDateFormat.getTimeInstance().format(bus.getTimestamp())));
          marker.setRotation(bus.getDirection());
        }
      }
    }
  }

  public void clearMap() {
    if (mMap != null) {
      clearStops();
      clearBuses();
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
   * This draws the pattern from the given route on the map.
   *
   * @param plot  the route plot
   * @param color the color of the plot
   */
  public void showRoutePlot(String plot, Integer color) {
    if (mMap == null || plot == null) {
      return;
    }

    PolylineOptions polylineOptions = new PolylineOptions();
    polylineOptions.addAll(PolyUtil.decode(plot));
    polylineOptions.width(8);
    if (color != null)
      polylineOptions.color(color);
    else
      polylineOptions.color(ContextCompat.getColor(getContext(), R.color.AccentColor));
    mMap.addPolyline(polylineOptions);
  }

  /**
   * This is where we initialize the map.
   * <p/>
   * This should only be called once and when we are sure that {@link #mMap} is not null.
   */
  public void setUpMap() {
    if (!checkLocationPermission()) {
      return;
    }
    mMap.setMyLocationEnabled(true);
    mMap.getUiSettings().setCompassEnabled(false);
    mMap.getUiSettings().setRotateGesturesEnabled(false);
    mMap.getUiSettings().setTiltGesturesEnabled(false);
    mMap.setOnInfoWindowClickListener(this);
    mMap.setMaxZoomPreference(17);

    CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(new LatLng(BBURG_LAT, BBURG_LNG), BBURG_ZOOM);
    mMap.animateCamera(cu);
  }

  /**
   * Verify that the user has granted coarse and find location permissions.
   * <p/>
   * If permissions are not granted and the user hasn't denied them in the past, ask for permissions.
   *
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
    currentStopMarkers.clear();
  }

  /**
   * Removes all bus markers from the map.
   */
  private void clearBuses() {
    currentBusMarkers.clear();
  }

  /**
   * Returns a prebuilt {@link MarkerOptions} for the given <code>stop</code>.
   *
   * @param stop the stop
   * @return the marker options
   */
  private MarkerOptions getStopMarker(Stop stop) {
    BitmapFactory.Options opts = new BitmapFactory.Options();
    opts.inSampleSize = 6;
    return new MarkerOptions()
        .position(stop.getLatLng())
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
  private MarkerOptions getBusMarker(Bus bus) {
    return new MarkerOptions()
        .position(bus.getLatLng())
        .rotation(bus.getDirection())
        .infoWindowAnchor(0.5f, 0.5f)
        .zIndex(10)
        .title(getString(R.string.bus_marker_title_format, bus.getRoute().getFullName(), bus.getId()))
        .snippet(getString(R.string.bus_marker_snippet_format, bus.getPassengers(),
            SimpleDateFormat.getTimeInstance().format(bus.getTimestamp())))
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
     * Hides the loading icon.
     */
    void hideLoadingIcon();

    /**
     * Shows the loading icon.
     */
    void showLoadingIcon();

    /**
     * Shows the departures dialog for the stop and route.
     *
     * @param stop  the bus stop
     * @param route the bus route
     */
    void showDeparturesDialog(Stop stop, Route route);

    /**
     * Returns the current bus route.
     *
     * @return the current route
     */
    Route getCurrentRoute();
  }
}
