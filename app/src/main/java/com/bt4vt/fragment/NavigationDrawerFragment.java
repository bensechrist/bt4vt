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
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.bt4vt.R;
import com.bt4vt.external.bt4u.Route;
import com.bt4vt.util.ViewUtils;
import com.google.inject.Inject;

import java.util.List;

import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;

/**
 * Represents the navigation drawer for layout {@link com.bt4vt.R.layout#navigation_drawer}.
 *
 * @author Ben Sechrist
 */
public class NavigationDrawerFragment extends RoboFragment implements
    NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

  private static final String TAG = "NavigationDrawer";

  private static final int MENU_ITEM_ALL_STOPS = ViewUtils.generateViewId();

  @Inject
  private SharedPreferences preferences;

  @InjectView(R.id.nav_view)
  private NavigationView navView;

  private TalkToActivity activity;

  private MenuItem lastMenuItem;

  private ImageButton refreshRoutesButton;

  @Override
  public void onCreate(Bundle savedInstanceState) {
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
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.navigation_drawer, container, false);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    navView.setNavigationItemSelectedListener(this);
    refreshRoutesButton = (ImageButton) navView.getHeaderView(0).findViewById(R.id.refresh_routes_button);
    refreshRoutesButton.setOnClickListener(this);
    refreshRoutesButton.setVisibility(View.VISIBLE);
  }

  public void setRouteNames(List<Route> routes) {
    refreshRoutesButton.setEnabled(true);
    Menu menu = navView.getMenu();
    menu.findItem(R.id.nav_loading).setVisible(false);
    for (Route route : routes) {
      MenuItem item = menu.add(R.id.nav_routes_group, Menu.NONE, 1, route.getFullName());
      item.setTitleCondensed(route.getShortName());
      item.setCheckable(true);
    }
    MenuItem item = menu.add(R.id.nav_routes_group, MENU_ITEM_ALL_STOPS, 2,
        getString(R.string.nav_item_view_all_stops));
    item.setCheckable(true);
  }

  public void showRemoveAdsMenuItem() {
    Menu menu = navView.getMenu();
    MenuItem item = menu.findItem(R.id.remove_ads);
    item.setVisible(true);
  }

  public void hideRemoveAdsMenuItem() {
    Menu menu = navView.getMenu();
    MenuItem item = menu.findItem(R.id.remove_ads);
    item.setVisible(false);
  }

  public void showRoutesLoading() {
    refreshRoutesButton.setEnabled(false);
    Menu menu = navView.getMenu();
    menu.findItem(R.id.nav_loading).setVisible(true);
  }

  @Override
  public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
    if (activity.isLoadingContent()) {
      Snackbar.make(navView, R.string.content_loading, Snackbar.LENGTH_LONG)
          .show();
      if (lastMenuItem != null) {
        lastMenuItem.setChecked(true);
      }
      return true;
    }
    if (menuItem.equals(lastMenuItem)) {
      activity.closeDrawer();
      return true;
    }
    int menuItemId = menuItem.getItemId();
    if (menuItem.getGroupId() == R.id.nav_routes_group) {
      if (menuItemId == R.id.nav_loading) {
        return true;
      }
      if (lastMenuItem != null) {
        lastMenuItem.setChecked(false);
      }
      lastMenuItem = menuItem;
      menuItem.setChecked(true);
      if (menuItemId == MENU_ITEM_ALL_STOPS) {
        activity.showAllStops();
      } else {
        Route route = new Route(menuItem.getTitleCondensed().toString());
        route.setFullName(menuItem.getTitle().toString());
        activity.onRouteSelected(route);
      }
      activity.closeDrawer();
    } else {
      // Non-route item
      if (menuItemId == R.id.nav_feedback) {
        activity.closeDrawer();
        showFeedbackDialog();
      } else if (menuItemId == R.id.remove_ads) {
        activity.closeDrawer();
        activity.promptPurchase();
      } else if (menuItemId == R.id.donate) {
        activity.closeDrawer();
        activity.promptDonation();
      }
    }
    return true;
  }

  @Override
  public void onClick(View v) {
    navView.getMenu().removeGroup(R.id.nav_routes_group);
    activity.fetchRoutes(true);
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
    void onRouteSelected(Route routeName);

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

    /**
     * Fetch all routes.
     *
     * @param ignoreCache whether cache should be ignored
     */
    void fetchRoutes(boolean ignoreCache);

    /**
     * Prompt user for inapp purchase to remove ads.
     */
    void promptPurchase();

    /**
     * Prompt user for inapp purchase to donate.
     */
    void promptDonation();
  }

}
