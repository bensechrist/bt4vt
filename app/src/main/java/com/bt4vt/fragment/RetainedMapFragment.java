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

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.bt4vt.R;
import com.bt4vt.async.AsyncCallback;
import com.bt4vt.async.StopAsyncTask;
import com.bt4vt.repository.TransitRepository;
import com.bt4vt.repository.domain.Bus;
import com.bt4vt.repository.domain.Route;
import com.bt4vt.repository.domain.Stop;
import com.bt4vt.repository.listener.BusListener;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import roboguice.RoboGuice;
import roboguice.inject.InjectResource;

/**
 * Handles all google map related actions.
 *
 * @author Ben Sechrist
 */
public class RetainedMapFragment extends SupportMapFragment implements OnMapReadyCallback,
    BusListener, GoogleMap.OnInfoWindowClickListener {

  private static final String DEPARTURES_DIALOG_TAG = "scheduled_departures_dialog_tag";

  @Inject
  private TransitRepository transitRepository;

  @InjectResource(R.string.stop_marker_snippet)
  private String stopMarkerSnippet;

  private GoogleMap mMap; // Might be null if Google Play services APK is not available.

  private final List<Marker> currentStopMarkers = new ArrayList<>();

  private final List<Marker> currentBusMarkers = new ArrayList<>();

  private Polyline currentRoutePattern;

  private TalkToActivity activity;

  private Route currentRoute;

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
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    this.activity = (TalkToActivity) activity;
  }

  @Override
  public void onDetach() {
    super.onDetach();
    this.activity = null;
    transitRepository.clearBusListeners();
  }

  @Override
  public void onMapReady(GoogleMap googleMap) {
    this.mMap = googleMap;
    setUpMap();
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

  public void setCurrentRoute(Route currentRoute) {
    this.currentRoute = currentRoute;
  }

  /**
   * This gets all stops on the given <code>route</code>.
   * If route is null it will get all stops.
   *
   * @param route the route
   */
  public void fetchStops(Route route) {
    StopAsyncTask task = new StopAsyncTask(transitRepository, new AsyncCallback<List<Stop>>() {
      @Override
      public void onSuccess(List<Stop> stops) {
        activity.onStopsReady(stops);
      }

      @Override
      public void onException(Exception e) {
        activity.hideLoadingIcon();
        e.printStackTrace();
        Toast.makeText(getActivity(), "Error getting stops for route", Toast.LENGTH_SHORT).show();
      }
    });
    task.execute(route);
  }

  public void fetchStop(final String stopString) {
    StopAsyncTask task = new StopAsyncTask(transitRepository, new AsyncCallback<List<Stop>>() {
      @Override
      public void onSuccess(List<Stop> stops) {
        for (Stop stop : stops) {
          if (stop.toString().equals(stopString)) {
            activity.onStopsReady(Collections.singletonList(stop));
          }
        }
      }

      @Override
      public void onException(Exception e) {
        activity.hideLoadingIcon();
        e.printStackTrace();
        Toast.makeText(getActivity(), "Error getting stop", Toast.LENGTH_SHORT).show();
      }
    });
    task.execute(null, null);
  }

  /**
   * This adds markers for the given <code>stops</code>.
   *
   * @param stops the stops
   */
  public void showStops(List<Stop> stops) {
    LatLngBounds.Builder builder = new LatLngBounds.Builder();
    for (Stop stop : stops) {
      Marker marker = mMap.addMarker(getStopMarker(stop));
      currentStopMarkers.add(marker);
      builder.include(marker.getPosition());
    }
    LatLngBounds bounds = builder.build();
    int padding = 100; // offset from edges of the map in pixels
    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
    mMap.animateCamera(cu);

    activity.hideLoadingIcon();
  }

  @Override
  public void onInfoWindowClick(Marker marker) {
    if (currentStopMarkers.contains(marker)) {
      Stop stop = Stop.valueOf(marker.getTitle());
      stop.setLatLng(marker.getPosition());
      ScheduledDeparturesDialogFragment.newInstance(stop, currentRoute)
          .show(getFragmentManager(), DEPARTURES_DIALOG_TAG);
    }
  }

  /**
   * This shows all buses on the given <code>route</code>.
   *
   * @param route the route
   */
  public void showBuses(Route route) {
    transitRepository.registerBusListener(route, this);
  }

  @Override
  public void onUpdateBuses(final List<Bus> buses) {
    if (buses.isEmpty()) {
      return;
    }

    if (currentRoute != null && !buses.get(0).getRouteName().equals(currentRoute.getName())) {
      return;
    }

    new Handler(Looper.getMainLooper()).post(new Runnable() {
      @Override
      public void run() {
        if (currentBusMarkers.size() != buses.size()) {
          for (Bus bus : buses) {
            Marker marker = mMap.addMarker(getBusMarker(bus));
            currentBusMarkers.add(marker);
          }

          showRoutePattern(buses.get(0));
        } else {
          for (int i = 0; i < currentBusMarkers.size(); i++) {
            Marker marker = currentBusMarkers.get(i);
            Bus bus = buses.get(i);
            marker.setPosition(new LatLng(bus.getLatitude(), bus.getLongitude()));
            marker.setTitle(getString(R.string.bus_marker_title_format, bus.getRouteName(), bus.getId()));
            marker.setSnippet(getString(R.string.bus_marker_snippet_format, bus.getPassengerLoad()));
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
   * This draws the pattern from the given bus on the map.
   *
   * @param bus the bus
   */
  private void showRoutePattern(Bus bus) {
    PolylineOptions polylineOptions = new PolylineOptions();
    List<LatLng> latLngPoints = new ArrayList<>();
    String[] patternPoints = bus.getPatternPoints();
    for (int i = 0; i < patternPoints.length; i = i + 2) {
      String point1 = patternPoints[i];
      String point2 = (patternPoints.length < i) ?
          patternPoints[0] :
          patternPoints[i + 1];
      latLngPoints.add(new LatLng(Double.valueOf(point1), Double.valueOf(point2)));
    }
    polylineOptions.addAll(latLngPoints);
    polylineOptions.width(8);
    polylineOptions.color(Color.parseColor(bus.getPatternColor()));
    currentRoutePattern = mMap.addPolyline(polylineOptions);
  }

  /**
   * This is where we initialize the map.
   * <p/>
   * This should only be called once and when we are sure that {@link #mMap} is not null.
   */
  private void setUpMap() {
    mMap.setMyLocationEnabled(true);
    mMap.getUiSettings().setCompassEnabled(false);
    mMap.getUiSettings().setRotateGesturesEnabled(false);
    mMap.getUiSettings().setTiltGesturesEnabled(false);
    mMap.setOnInfoWindowClickListener(this);
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
    if (currentRoutePattern != null) {
      currentRoutePattern.remove();
    }
    currentRoutePattern = null;
    currentRoute = null;
  }

  private void removeMarkers(List<Marker> markers) {
    Iterator<Marker> it = markers.iterator();
    while (it.hasNext()) {
      it.next().remove();
      it.remove();
    }
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
  private MarkerOptions getBusMarker(Bus bus) {
    BitmapFactory.Options opts = new BitmapFactory.Options();
    opts.inSampleSize = 10;
    return new MarkerOptions()
        .position(new LatLng(bus.getLatitude(), bus.getLongitude()))
        .title(getString(R.string.bus_marker_title_format, bus.getRouteName(), bus.getId()))
        .snippet(getString(R.string.bus_marker_snippet_format, bus.getPassengerLoad()))
        .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(),
            R.drawable.bus, opts)));
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
    void onStopsReady(List<Stop> stops);

    /**
     * Hides the loading icon.
     */
    void hideLoadingIcon();
  }
}
