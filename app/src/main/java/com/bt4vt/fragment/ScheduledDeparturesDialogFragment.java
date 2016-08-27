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

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.bt4vt.MainActivity;
import com.bt4vt.R;
import com.bt4vt.adapter.DepartureArrayAdapter;
import com.bt4vt.async.AsyncCallback;
import com.bt4vt.async.DepartureAsyncTask;
import com.bt4vt.repository.TransitRepository;
import com.bt4vt.repository.model.DepartureModel;
import com.bt4vt.repository.model.RouteModel;
import com.bt4vt.repository.model.StopModel;
import com.bt4vt.repository.model.StopModelFactory;
import com.firebase.client.AuthData;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
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
    implements AsyncCallback<List<DepartureModel>>, View.OnClickListener,
    Firebase.AuthStateListener, ChildEventListener {

  private static final String TAG = "DeparturesDialog";
  private static final String STOP_FORMAT = "Stop: %s";
  private static final String ROUTE_FORMAT = "Route: %s";
  private static final String ALL_ROUTES = "ALL";

  @Inject
  private TransitRepository transitRepository;

  @Inject
  private StopModelFactory stopModelFactory;

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

  private Firebase firebaseRef;

  private StopModel stop;
  private RouteModel route;

  private boolean isFavorited = false;
  private boolean isAuthenticated = false;

  public static ScheduledDeparturesDialogFragment newInstance(StopModel stop, RouteModel route) {
    if (stop == null) {
      throw new NullPointerException("Stop was null");
    }
    ScheduledDeparturesDialogFragment fragment = new ScheduledDeparturesDialogFragment();
    fragment.stop = stop;
    fragment.route = route;
    return fragment;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
    return inflater.inflate(R.layout.departures_dialog, container);
  }

  @Override
  public void onStart() {
    super.onStart();
    Firebase.setAndroidContext(getActivity());
    firebaseRef = new Firebase(MainActivity.FIREBASE_BASE_URL);
    firebaseRef.addAuthStateListener(this);
  }

  @Override
  public void onStop() {
    super.onStop();
    if (firebaseRef != null) {
      firebaseRef.removeAuthStateListener(this);
      if (firebaseRef.getAuth() != null) {
        firebaseRef.child("favorite-stops")
            .child(String.valueOf(stop.getCode()))
            .removeEventListener(this);
      }
    }
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

    stopTextView.setText(String.format(STOP_FORMAT, stop.toString()));
    String routeText;
    if (route != null) {
      routeText = route.toString();
    } else {
      routeText = ALL_ROUTES;
    }
    routeTextView.setText(String.format(ROUTE_FORMAT, routeText));

    new DepartureAsyncTask(transitRepository, stop, route, this, getActivity()).execute();

    emptyDeparturesView.findViewById(R.id.refresh_departures_button).setOnClickListener(this);
  }

  @Override
  public void onSuccess(List<DepartureModel> departures) {
    if (isAdded()) {
      final int MAX_DEPARTURES = getResources().getInteger(R.integer.max_departures_shown);
      if (departures.size() > MAX_DEPARTURES) {
        departures = departures.subList(0, MAX_DEPARTURES);
      }
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
        Snackbar.make(view, R.string.departures_error, Snackbar.LENGTH_LONG)
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
      default:
        listView.setEmptyView(null);
        loadingView.setVisibility(View.VISIBLE);
        new DepartureAsyncTask(transitRepository, stop, route, this, getActivity()).execute();
        break;
    }
  }

  @Override
  public void onAuthStateChanged(AuthData authData) {
    if (stop == null) return;
    isAuthenticated = authData != null;
    if (isAuthenticated) {
      Log.i(TAG, "User is authenticated");
      if (firebaseRef.getParent() == null) {
        firebaseRef = firebaseRef.child(authData.getUid());
      }
      firebaseRef.child("favorite-stops")
          .child(String.valueOf(stop.getCode()))
          .addChildEventListener(this);
      favoriteButton.setVisibility(View.VISIBLE);
    } else {
      Log.i(TAG, "User is not authenticated");
      firebaseRef.child("favorite-stops")
          .child(String.valueOf(stop.getCode()))
          .removeEventListener(this);
      favoriteButton.setVisibility(View.INVISIBLE);
    }
  }

  private void onFavClick() {
    if (isAuthenticated) {
      if (isFavorited) {
        Log.i(TAG, "Removing favorite");
        firebaseRef.child("favorite-stops").child(String.valueOf(stop.getCode())).removeValue();
      } else {
        Log.i(TAG, "Adding favorite");
        firebaseRef.child("favorite-stops").child(String.valueOf(stop.getCode())).setValue(stop);
      }
    } else {
      View view = getView();
      if (view != null) {
        Snackbar.make(view, R.string.not_logged_in, Snackbar.LENGTH_LONG).show();
      }
    }
  }

  @Override
  public void onChildAdded(DataSnapshot dataSnapshot, String s) {
    Log.i(TAG, "Stop added to favorites");
    isFavorited = true;
    Context context = getActivity();
    if (context != null)
      favoriteButton.setImageDrawable(ContextCompat.getDrawable(context,
          R.drawable.ic_action_star_full));
  }

  @Override
  public void onChildChanged(DataSnapshot dataSnapshot, String s) {
    stop = stopModelFactory.createModel(dataSnapshot);
  }

  @Override
  public void onChildRemoved(DataSnapshot dataSnapshot) {
    Log.i(TAG, "Stop removed from favorites");
    isFavorited = false;
    Context context = getActivity();
    if (context != null)
      favoriteButton.setImageDrawable(ContextCompat.getDrawable(getActivity(),
          R.drawable.ic_action_star_empty));
  }

  @Override
  public void onChildMoved(DataSnapshot dataSnapshot, String s) {
    // We don't care about this for now
  }

  @Override
  public void onCancelled(FirebaseError firebaseError) {
    // We don't care about this for now
  }
}
