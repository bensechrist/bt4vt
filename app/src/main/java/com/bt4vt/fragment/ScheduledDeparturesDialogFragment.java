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
import com.bt4vt.async.AsyncCallback;
import com.bt4vt.async.DepartureAsyncTask;
import com.bt4vt.async.StopAsyncTask;
import com.bt4vt.geofence.BusStopGeofenceService;
import com.bt4vt.repository.TransitRepository;
import com.bt4vt.repository.model.DepartureModel;
import com.bt4vt.repository.model.RouteModel;
import com.bt4vt.repository.model.StopModel;
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
    implements AsyncCallback, View.OnClickListener {

  private static final String TAG = "DeparturesDialog";
  private static final String STOP_FORMAT = "Stop: %s";
  private static final String ROUTE_FORMAT = "Route: %s";
  private static final String ALL_ROUTES = "ALL";

  @Inject
  private TransitRepository transitRepository;

  @Inject
  private BusStopGeofenceService busStopGeofenceService;

  @InjectView(R.id.stop_text)
  private TextView stopTextView;

  @InjectView(R.id.route_text)
  private TextView routeTextView;

  @InjectView(R.id.list_view)
  private ListView listView;

  @InjectView(R.id.departure_loading_view)
  private View loadingView;

  @InjectView(R.id.empty_departures_view)
  private View emptyDeparturesView;

  @InjectView(R.id.button_favorite_stop)
  private ImageButton favoriteButton;

  private Integer stopCode;
  private StopModel stop;
  private RouteModel route;

  public static ScheduledDeparturesDialogFragment newInstance(Integer stopCode, RouteModel route) {
    if (stopCode == null) {
      throw new NullPointerException("Stop was null");
    }
    ScheduledDeparturesDialogFragment fragment = new ScheduledDeparturesDialogFragment();
    fragment.stopCode = stopCode;
    fragment.route = route;
    fragment.setRetainInstance(true);
    return fragment;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
    return inflater.inflate(R.layout.departures_dialog, container);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    // If we get here and the stop is null then we should exit the departures dialog
    if (stopCode == null) {
      getDialog().dismiss();
      return;
    }

    loadingView.setVisibility(View.VISIBLE);

    favoriteButton.setOnClickListener(this);

    new StopAsyncTask(transitRepository, stopCode, this).execute();

    String routeText;
    if (route != null) {
      routeText = route.toString();
    } else {
      routeText = ALL_ROUTES;
    }
    routeTextView.setText(String.format(ROUTE_FORMAT, routeText));

    emptyDeparturesView.findViewById(R.id.refresh_departures_button).setOnClickListener(this);
  }

  @Override
  public void onSuccess(Object o) {
    if (isAdded()) {
      if (o instanceof List) {
        List<DepartureModel> departures = (List<DepartureModel>) o;
        final int MAX_DEPARTURES = getResources().getInteger(R.integer.max_departures_shown);
        if (departures.size() > MAX_DEPARTURES) {
          departures = departures.subList(0, MAX_DEPARTURES);
        }
        listView.setAdapter(new DepartureArrayAdapter(getActivity(), departures));
        listView.setEmptyView(emptyDeparturesView);
        loadingView.setVisibility(View.INVISIBLE);
      } else if (o instanceof StopModel) {
        stop = (StopModel) o;
        stopTextView.setText(String.format(STOP_FORMAT, stop.toString()));
        setFavButton();
        new DepartureAsyncTask(transitRepository, stop, route, this, getActivity()).execute();
      }
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
        new DepartureAsyncTask(transitRepository, stop, route, this, getActivity()).execute();
        break;
      default:
        listView.setEmptyView(null);
        loadingView.setVisibility(View.VISIBLE);
        new StopAsyncTask(transitRepository, stopCode, this).execute();
        break;
    }
  }

  private void onFavClick() {
    if (stop != null) {
      stop.setFavorited(!stop.isFavorited());
      transitRepository.updateStop(stop);
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
