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

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
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
import com.bt4vt.async.FetchGoogleTokenTask;
import com.bt4vt.repository.FirebaseService;
import com.bt4vt.repository.TransitRepository;
import com.bt4vt.repository.domain.Departure;
import com.bt4vt.repository.domain.Route;
import com.bt4vt.repository.domain.Stop;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
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
    implements AsyncCallback<List<Departure>>, View.OnClickListener, Firebase.AuthResultHandler {

  private static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
  private static final int REQUEST_AUTHORIZATION = 2000;

  private static final String STOP_FORMAT = "Stop: %s";
  private static final String ROUTE_FORMAT = "Route: %s";
  private static final String ALL_ROUTES = "ALL";
  public static final String FIRST_DEPARTURES_VIEW_KEY = "first_departures_view";

  @Inject
  private LayoutInflater inflater;

  @Inject
  private TransitRepository transitRepository;

  @Inject
  private SharedPreferences preferences;

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

  private FirebaseService firebaseService;
  private boolean serviceBound = false;

  private Stop stop;
  private Route route;

  public static ScheduledDeparturesDialogFragment newInstance(Stop stop, Route route) {
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
    Intent intent = new Intent(getActivity(), FirebaseService.class);
    getActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE);
  }

  @Override
  public void onStop() {
    super.onStop();
    if (serviceBound) {
      getActivity().unbindService(connection);
      serviceBound = false;
    }
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    loadingView.setVisibility(View.VISIBLE);

    stopTextView.setText(String.format(STOP_FORMAT, stop.toString()));
    String routeText;
    if (route != null) {
      routeText = route.toString();
    } else {
      routeText = ALL_ROUTES;
    }
    routeTextView.setText(String.format(ROUTE_FORMAT, routeText));

    new DepartureAsyncTask(transitRepository, stop, route, this).execute();

    emptyDeparturesView.findViewById(R.id.refresh_departures_button).setOnClickListener(this);
  }

  @Override
  public void onSuccess(List<Departure> departures) {
    listView.setAdapter(new DepartureArrayAdapter(getActivity(), departures));
    listView.setEmptyView(emptyDeparturesView);
    loadingView.setVisibility(View.INVISIBLE);
  }

  @Override
  public void onException(Exception e) {
    e.printStackTrace();
    listView.setEmptyView(emptyDeparturesView);
    loadingView.setVisibility(View.INVISIBLE);
    Snackbar.make(getView(), R.string.departures_error, Snackbar.LENGTH_LONG)
        .setAction(R.string.retry, this)
        .show();
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.refresh_departures_button:
        listView.setEmptyView(null);
        loadingView.setVisibility(View.VISIBLE);
        new DepartureAsyncTask(transitRepository, stop, route, this).execute();
        break;
      case R.id.button_favorite_stop:
        onFavClick();
        break;
      default:
        // Most likely from a snackbar action
        new DepartureAsyncTask(transitRepository, stop, route, this).execute();
        break;
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_CODE_PICK_ACCOUNT || requestCode == REQUEST_AUTHORIZATION) {
      if (resultCode == Activity.RESULT_OK) {
        AsyncCallback<String> callback = new AsyncCallback<String>() {
          @Override
          public void onSuccess(String token) {
            firebaseService.loginGoogle(token, ScheduledDeparturesDialogFragment.this);
          }

          @Override
          public void onException(Exception e) {
            e.printStackTrace();
            if (e instanceof UserRecoverableAuthException) {
              startActivityForResult(((UserRecoverableAuthException) e).getIntent(), REQUEST_AUTHORIZATION);
            }
          }
        };
        String userEmail = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        preferences.edit().putString(FirebaseService.USER_EMAIL_KEY, userEmail).apply();
        new FetchGoogleTokenTask(getActivity(), userEmail, callback).execute();
      } else if (resultCode == Activity.RESULT_CANCELED) {
        // The account picker dialog closed without selecting an account.
        View.OnClickListener listener = new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            onFavClick();
          }
        };
        Snackbar.make(getView(), R.string.not_logged_in, Snackbar.LENGTH_LONG)
            .setAction(R.string.login, listener)
            .show();
      }
    }
  }

  @Override
  public void onAuthenticated(AuthData authData) {
    setFavButtonText();
  }

  @Override
  public void onAuthenticationError(FirebaseError firebaseError) {
    // Ignore?
  }

  private void onFavClick() {
    if (serviceBound) {
      if (!firebaseService.isAuthenticated()) {
        String[] accountTypes = new String[]{"com.google"};
        Intent intent = AccountPicker.newChooseAccountIntent(null, null,
            accountTypes, false, null, null, null, null);
        startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
      } else {
        if (firebaseService.isFavorited(stop)) {
          firebaseService.removeFavorite(stop);
        } else {
          firebaseService.addFavorite(stop);
        }
        setFavButtonText();
      }
    }
  }

  private void setFavButtonText() {
    if (serviceBound) {
      if (firebaseService.isFavorited(stop)) {
        favoriteButton.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.heart));
      } else {
        favoriteButton.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.empty_heart));
      }
    }
  }

  private ServiceConnection connection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
      FirebaseService.FirebaseServiceBinder binder = (FirebaseService.FirebaseServiceBinder) service;
      firebaseService = binder.getService();
      serviceBound = true;
      setFavButtonText();
      favoriteButton.setOnClickListener(ScheduledDeparturesDialogFragment.this);
      favoriteButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
      serviceBound = false;
    }
  };
}
