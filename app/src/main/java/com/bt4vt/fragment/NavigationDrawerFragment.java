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
import android.widget.TextView;

import com.bt4vt.R;

import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;

/**
 * Represents the navigation drawer for layout {@link com.bt4vt.R.layout#navigation_drawer}.
 *
 * @author Ben Sechrist
 */
public class NavigationDrawerFragment extends RoboFragment implements AdapterView.OnItemClickListener {

  @InjectView(R.id.list_view)
  private ListView mDrawerList;

  @InjectView(R.id.empty_routes_text)
  private TextView emptyRoutesText;

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
    mDrawerList.setEmptyView(emptyRoutesText);
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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

  public void setRouteNames(String[] routeNames) {
    this.routeNames = routeNames;
    mDrawerList.setAdapter(new ArrayAdapter<>(getActivity(),
        android.R.layout.simple_list_item_activated_1, this.routeNames));
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
  }
}
