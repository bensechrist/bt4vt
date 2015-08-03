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
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.bt4vt.R;
import com.bt4vt.async.AsyncCallback;
import com.bt4vt.async.RouteAsyncTask;
import com.bt4vt.repository.TransitRepository;
import com.bt4vt.repository.domain.Route;
import com.google.inject.Inject;

import java.util.List;

import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;

/**
 * Represents the navigation drawer for layout {@link com.bt4vt.R.layout#navigation_drawer}.
 *
 * @author Ben Sechrist
 */
public class NavigationDrawerFragment extends RoboFragment implements AdapterView.OnItemClickListener, View.OnClickListener {

  @Inject
  private TransitRepository transitRepository;

  @InjectView(R.id.list_view)
  private ListView mDrawerList;

  @InjectView(R.id.empty_routes_view)
  private View emptyRoutesView;

  @InjectView(R.id.nav_loading_view)
  private View navLoadingView;

  private String[] routeNames;

  private Integer selectedItem;

  private TalkToActivity activity;

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
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.navigation_drawer, container, false);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    mDrawerList.setOnItemClickListener(this);
    emptyRoutesView.findViewById(R.id.refresh_routes_button).setOnClickListener(this);
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    if (activity.isLoadingContent()) {
      Toast.makeText(getActivity(), "Waiting on other content to load first", Toast.LENGTH_SHORT).show();
      return;
    }
    if ((selectedItem != null) && (position == selectedItem)) {
      activity.closeDrawer();
      return;
    }
    if (routeNames != null) {
      mDrawerList.setItemChecked(position, true);
      selectedItem = position;
      activity.closeDrawer();

      activity.onRouteSelected(routeNames[position]);
    }
  }

  @Override
  public void onClick(View v) {
    fetchRoutes();
  }

  public void fetchRoutes() {
    emptyRoutesView.setVisibility(View.INVISIBLE);
    navLoadingView.setVisibility(View.VISIBLE);
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
        Toast.makeText(getActivity(), "Error getting routes", Toast.LENGTH_SHORT).show();
      }
    });
    task.execute();
  }

  private void setRouteNames(String[] routeNames) {
    this.routeNames = routeNames;
    mDrawerList.setAdapter(new ArrayAdapter<>(getActivity(),
        android.R.layout.simple_list_item_activated_1, this.routeNames));
    navLoadingView.setVisibility(View.INVISIBLE);
    mDrawerList.setEmptyView(emptyRoutesView);
  }

  /**
   * Used to communicate with the main activity from a fragment.
   *
   * @author Ben Sechrist
   */
  public interface TalkToActivity {

    /**
     * Gives the route name of the route selected.
     * @param routeName name of the route
     */
    void onRouteSelected(String routeName);

    /**
     * Closes the drawer.
     */
    void closeDrawer();

    /**
     * Returns whether the activity is currently loading something else.
     * @return true if loading in progress, false otherwise
     */
    boolean isLoadingContent();
  }
}
