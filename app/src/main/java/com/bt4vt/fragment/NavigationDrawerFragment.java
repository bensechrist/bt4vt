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
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bt4vt.R;
import com.bt4vt.async.AsyncCallback;
import com.bt4vt.async.FetchBitmapFromUrlTask;
import com.bt4vt.async.FetchGoogleTokenTask;
import com.bt4vt.async.RouteAsyncTask;
import com.bt4vt.repository.FirebaseService;
import com.bt4vt.repository.TransitRepository;
import com.bt4vt.repository.domain.Route;
import com.bt4vt.util.ViewUtils;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.inject.Inject;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;

/**
 * Represents the navigation drawer for layout {@link com.bt4vt.R.layout#navigation_drawer}.
 *
 * @author Ben Sechrist
 */
public class NavigationDrawerFragment extends RoboFragment implements View.OnClickListener,
    NavigationView.OnNavigationItemSelectedListener, AsyncCallback<String>, Firebase.AuthResultHandler {

  private static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
  private static final int REQUEST_AUTHORIZATION = 2000;
  private static final int NAV_SIGNOUT_ID = ViewUtils.generateViewId();

  @Inject
  private TransitRepository transitRepository;

  @Inject
  private SharedPreferences preferences;

  @InjectView(R.id.nav_view)
  private NavigationView navView;

  private TalkToActivity activity;

  private FirebaseService firebaseService;
  private boolean serviceBound = false;

  private View navHeader;

  private MenuItem lastMenuItem;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRetainInstance(true);
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
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.navigation_drawer, container, false);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    navView.setNavigationItemSelectedListener(this);
  }

  @Override
  public void onClick(View v) {
    fetchRoutes();
  }

  public void fetchRoutes() {
    RouteAsyncTask task = new RouteAsyncTask(transitRepository, new AsyncCallback<List<Route>>() {
      @Override
      public void onSuccess(List<Route> routes) {
        String[] routeNames = new String[routes.size()];
        for (int i = 0; i < routes.size(); i++) {
          routeNames[i] = routes.get(i).getName();
        }
        setRouteNames(routeNames);
      }

      @Override
      public void onException(Exception e) {
        setRouteNames(new String[]{});
        e.printStackTrace();
        View view = getView();
        if (view != null) {
          Snackbar.make(view, R.string.routes_error, Snackbar.LENGTH_LONG)
              .setAction(R.string.retry, NavigationDrawerFragment.this)
              .show();
        }
      }
    });
    task.execute();
  }

  private void setRouteNames(String[] routeNames) {
    Menu menu = navView.getMenu();
    for (String routeName : routeNames) {
      MenuItem item = menu.add(R.id.nav_routes_group, Menu.NONE, 1, routeName);
      item.setCheckable(true);
    }
  }

  @Override
  public boolean onNavigationItemSelected(MenuItem menuItem) {
    String menuTitle = menuItem.getTitle().toString();
    if (activity.isLoadingContent()) {
      Snackbar.make(navView, R.string.content_loading, Snackbar.LENGTH_LONG)
          .show();
      return true;
    }
    if (menuItem.isChecked()) {
      activity.closeDrawer();
      return true;
    }
    int menuItemId = menuItem.getItemId();
    if (menuItem.getGroupId() == R.id.nav_routes_group) {
      if (lastMenuItem != null) {
        lastMenuItem.setChecked(false);
      }
      lastMenuItem = menuItem;
      menuItem.setChecked(true);
      if (menuItemId == R.id.nav_view_all_stops) {
        activity.showAllStops();
      } else {
        activity.onRouteSelected(menuTitle);
      }
      activity.closeDrawer();
    } else {
      // Non-route item
      if (menuItemId == R.id.nav_signin) {
        signIn();
      } else if (menuItemId == NAV_SIGNOUT_ID) {
        firebaseService.logout();
        initHeader();
      } else if (menuItemId == R.id.nav_feedback) {
        activity.closeDrawer();
        showFeedbackDialog();
      }
    }
    return true;
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_CODE_PICK_ACCOUNT || requestCode == REQUEST_AUTHORIZATION) {
      if (resultCode == Activity.RESULT_OK) {
        String userEmail = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        preferences.edit().putString(FirebaseService.USER_EMAIL_KEY, userEmail).apply();
        new FetchGoogleTokenTask(getActivity(), userEmail, this).execute();
      } else if (resultCode == Activity.RESULT_CANCELED) {
        // The account picker dialog closed without selecting an account.
        View.OnClickListener listener = new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            signIn();
          }
        };
        View view = getView();
        if (view != null) {
          Snackbar.make(view, R.string.not_logged_in, Snackbar.LENGTH_LONG)
              .setAction(R.string.login, listener)
              .show();
        }
      }
    }
  }

  private void signIn() {
    String userEmail = preferences.getString(FirebaseService.USER_EMAIL_KEY, null);
    if (userEmail != null) {
      new FetchGoogleTokenTask(getActivity(), userEmail, this).execute();
    } else {
      showAccountPicker();
    }
  }

  @Override
  public void onSuccess(String token) {
    firebaseService.loginGoogle(token, this);
  }

  @Override
  public void onException(Exception e) {
    e.printStackTrace();
    if (e instanceof UserRecoverableAuthException) {
      startActivityForResult(((UserRecoverableAuthException) e).getIntent(), REQUEST_AUTHORIZATION);
    }
  }

  @Override
  public void onAuthenticated(AuthData authData) {
    initHeader();
  }

  @Override
  public void onAuthenticationError(FirebaseError firebaseError) {
    // Ignore?
  }

  private void showAccountPicker() {
    String[] accountTypes = new String[]{"com.google"};
    Intent intent = AccountPicker.newChooseAccountIntent(null, null,
        accountTypes, false, null, null, null, null);
    startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
  }

  private void initHeader() {
    if (serviceBound) {
      if (firebaseService.isAuthenticated()) {
        if (navHeader == null) {
          View headerView = View.inflate(getActivity(), R.layout.drawer_header, null);
          final CircleImageView profileImage = (CircleImageView) headerView.findViewById(R.id.profile_image);
          TextView profileName = (TextView) headerView.findViewById(R.id.profile_name);
          TextView profileEmail = (TextView) headerView.findViewById(R.id.profile_email);
          new FetchBitmapFromUrlTask(new AsyncCallback<Bitmap>() {
            @Override
            public void onSuccess(Bitmap bitmap) {
              profileImage.setImageBitmap(bitmap);
            }

            @Override
            public void onException(Exception e) {
              View view = getView();
              if (view != null) {
                Snackbar.make(view, R.string.fetch_bitmap_error, Snackbar.LENGTH_SHORT)
                    .show();
              }
            }
          }).execute(firebaseService.getUserProfileImageUrl());
          profileName.setText(firebaseService.getUserDisplayName());
          profileEmail.setText(firebaseService.getUserEmail());
          navHeader = headerView;
          navView.addHeaderView(navHeader);

          Menu menu = navView.getMenu();
          menu.removeItem(R.id.nav_signin);
          menu.add(R.id.nav_other_group, NAV_SIGNOUT_ID, 50, R.string.nav_signout);
        }
      } else {
        if (navHeader != null) {
          navView.removeHeaderView(navHeader);
          navHeader = null;

          Menu menu = navView.getMenu();
          menu.removeItem(NAV_SIGNOUT_ID);
          menu.add(R.id.nav_other_group, R.id.nav_signin, 50, R.string.nav_signin);
        }
      }
    }
  }

  private void showFeedbackDialog() {
    Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
    sendIntent.setData(Uri.parse("mailto:")); // only email apps should handle this
    sendIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_feedback));
    sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.app_feedback_email)});
    if (sendIntent.resolveActivity(getActivity().getPackageManager()) != null) {
      startActivity(sendIntent);
    }
  }

  /**
   * Used to communicate with the main activity from a fragment.
   *
   * @author Ben Sechrist
   */
  public interface TalkToActivity {

    /**
     * Gives the route name of the route selected.
     *
     * @param routeName name of the route
     */
    void onRouteSelected(String routeName);

    /**
     * Closes the drawer.
     */
    void closeDrawer();

    /**
     * Returns whether the activity is currently loading something else.
     *
     * @return true if loading in progress, false otherwise
     */
    boolean isLoadingContent();

    /**
     * Show all bus stops.
     */
    void showAllStops();
  }

  private ServiceConnection connection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
      FirebaseService.FirebaseServiceBinder binder = (FirebaseService.FirebaseServiceBinder) service;
      firebaseService = binder.getService();
      serviceBound = true;
      initHeader();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
      serviceBound = false;
    }
  };
}
