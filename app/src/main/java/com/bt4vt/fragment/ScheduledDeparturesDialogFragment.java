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

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.bt4vt.R;
import com.bt4vt.adapter.DepartureArrayAdapter;
import com.bt4vt.external.bt4u.Departure;
import com.bt4vt.external.bt4u.DepartureService;
import com.bt4vt.external.bt4u.Response;
import com.bt4vt.external.bt4u.Route;
import com.bt4vt.external.bt4u.Stop;
import com.bt4vt.geofence.BusStopGeofenceService;
import com.bt4vt.service.FavoriteStopService;
import com.google.inject.Inject;

import java.util.List;

import roboguice.fragment.RoboDialogFragment;
import roboguice.inject.InjectView;

/**
 * Shows the scheduled departures for the given stop in a dialog.
 *
 * @author Ben Sechrist
 */
public class ScheduledDeparturesDialogFragment extends RoboDialogFragment
    implements View.OnClickListener, Response.Listener<List<Departure>>, Response.ExceptionListener {

  private static final String STOP_FORMAT = "Stop: %s";

  @Inject
  private DepartureService departureService;

  @Inject
  private FavoriteStopService favoriteStopService;

  private BusStopGeofenceService busStopGeofenceService;

  @InjectView(R.id.stop_text)
  private TextView stopTextView;

  @InjectView(R.id.list_view)
  private ListView listView;

  @InjectView(R.id.departure_loading_view)
  private View loadingView;

  @InjectView(R.id.empty_departures_view)
  private View emptyDeparturesView;

  @InjectView(R.id.button_favorite_stop)
  private ImageButton favoriteButton;

  private Stop stop;
  private Route route;

  public static ScheduledDeparturesDialogFragment newInstance(Stop stop, Route route, BusStopGeofenceService busStopGeofenceService) {
    if (stop == null) {
      throw new NullPointerException("Stop was null");
    }
    ScheduledDeparturesDialogFragment fragment = new ScheduledDeparturesDialogFragment();
    fragment.stop = stop;
    fragment.route = route;
    fragment.busStopGeofenceService = busStopGeofenceService;
    fragment.setRetainInstance(true);
    return fragment;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    Window window = getDialog().getWindow();
    if (window != null)
      window.requestFeature(Window.FEATURE_NO_TITLE);
    return inflater.inflate(R.layout.departures_dialog, container);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    // If we get here and the stop is null then we should exit the departures dialog
    if (stop == null) {
      getDialog().dismiss();
      return;
    }

    loadingView.setVisibility(View.VISIBLE);

    favoriteButton.setOnClickListener(this);

    setFavButton();

    stopTextView.setText(String.format(STOP_FORMAT, stop.toString()));

    emptyDeparturesView.findViewById(R.id.refresh_departures_button).setOnClickListener(this);

    departureService.getAll((route == null ? "" : route.getFullName()), stop.getCode(), this,
        this);
  }

  @Override
  public void onResult(List<Departure> departures) {
    if (isAdded()) {
      listView.setAdapter(new DepartureArrayAdapter(getActivity(), departures));
      listView.setEmptyView(emptyDeparturesView);
      loadingView.setVisibility(View.INVISIBLE);
    }
  }

  @Override
  public void onException(Exception e) {
    if (isAdded()) {
      e.printStackTrace();
      listView.setEmptyView(emptyDeparturesView);
      loadingView.setVisibility(View.INVISIBLE);
      View view = getView();
      if (view != null) {
        Snackbar.make(view, R.string.stop_dialog_error, Snackbar.LENGTH_LONG)
            .setAction(R.string.retry, this)
            .show();
      }
    }
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.button_favorite_stop:
        onFavClick();
        break;
      case R.id.refresh_departures_button:
        listView.setEmptyView(null);
        loadingView.setVisibility(View.VISIBLE);
        if (departureService != null && stop != null) {
          departureService.getAll((route == null ? null : route.getFullName()), stop.getCode(),
              this, this);
        }
        break;
    }
  }

  private void onFavClick() {
    if (stop != null) {
      stop.setFavorited(!stop.isFavorited());
      favoriteStopService.upsert(stop);
      setFavButton();
    }
  }

  private void setFavButton() {
    if (stop.isFavorited()) {
      favoriteButton.setImageDrawable(ContextCompat.getDrawable(getActivity(),
          R.drawable.ic_action_star_full));
      busStopGeofenceService.registerGeofence(stop);
    } else {
      favoriteButton.setImageDrawable(ContextCompat.getDrawable(getActivity(),
          R.drawable.ic_action_star_empty));
      busStopGeofenceService.unregisterGeofence(stop);
    }
  }
}
