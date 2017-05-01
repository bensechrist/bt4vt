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

import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.android.vending.billing.IInAppBillingService;
import com.bt4vt.BuildConfig;
import com.bt4vt.MainActivity;
import com.bt4vt.R;
import com.bt4vt.async.DonationConsumeAsyncTask;
import com.bt4vt.external.bt4u.Route;
import com.google.inject.Inject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;

import static android.app.Activity.RESULT_OK;

/**
 * Represents the navigation drawer for layout {@link com.bt4vt.R.layout#navigation_drawer}.
 *
 * @author Ben Sechrist
 */
public class NavigationDrawerFragment extends RoboFragment implements
    NavigationView.OnNavigationItemSelectedListener {

  private static final String TAG = "NavigationDrawer";

  @Inject
  private SharedPreferences preferences;

  @InjectView(R.id.nav_view)
  private NavigationView navView;

  private TalkToActivity activity;

  private MenuItem lastMenuItem;

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
  }

  public void setRouteNames(List<Route> routes) {
    Menu menu = navView.getMenu();
    menu.findItem(R.id.nav_loading).setVisible(false);
    menu.findItem(R.id.nav_view_all_stops).setVisible(true);
    for (Route route : routes) {
      MenuItem item = menu.add(R.id.nav_routes_group, Menu.NONE, 1, route.getFullName());
      item.setTitleCondensed(route.getShortName());
      item.setCheckable(true);
    }
  }

  @Override
  public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
    if (activity.isLoadingContent()) {
      Snackbar.make(navView, R.string.content_loading, Snackbar.LENGTH_LONG)
          .show();
      lastMenuItem.setChecked(true);
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
      if (menuItemId == R.id.nav_view_all_stops) {
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
      } else if (menuItemId == R.id.nav_donation) {
        IInAppBillingService billingService = activity.getBillingService();
        if (billingService != null) {
          activity.closeDrawer();
          // TODO: implement donation service
//          new DonationOptionAsyncTask(billingService, getActivity().getPackageName(), this).execute();
        }
      }
    }
    return true;
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

  private void showDonationDialog(final List<JSONObject> products) throws JSONException {
    final int[] selected = {-1};
    Collections.sort(products, new Comparator<JSONObject>() {
      @Override
      public int compare(JSONObject o1, JSONObject o2) {
        try {
          return o1.getInt("price_amount_micros") - o2.getInt("price_amount_micros");
        } catch (JSONException e) {
          Log.e(TAG, e.getLocalizedMessage());
          return 0;
        }
      }
    });
    String[] prices = new String[products.size()];
    for (JSONObject product : products) {
      prices[products.indexOf(product)] = product.getString("title").replace(" (BT4VT)", "");
    }

    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    builder.setTitle(R.string.donation_dialog_title)
        .setSingleChoiceItems(prices, -1, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            selected[0] = which;
          }
        })
        .setPositiveButton(R.string.nav_donation, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            if (selected[0] > -1 && selected[0] < products.size()) {
              chargeDonation(products.get(selected[0]));
            }
          }
        })
        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
          }
        });
    builder.show();
  }

  private void chargeDonation(JSONObject product) {
    try {
      String productId = (BuildConfig.DEBUG ? "android.test.purchased" : product.getString("productId"));
      Bundle buyIntentBundle = activity.getBillingService()
          .getBuyIntent(3, getActivity().getPackageName(), productId, "inapp", null);
      int response = buyIntentBundle.getInt("RESPONSE_CODE");
      if (response == 0) {
        PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
        startIntentSenderForResult(pendingIntent.getIntentSender(),
            1001, new Intent(), Integer.valueOf(0), Integer.valueOf(0),
            Integer.valueOf(0), null);
      }
    } catch (RemoteException | JSONException | IntentSender.SendIntentException e) {
      Log.e(TAG, e.getLocalizedMessage());
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == 1001) {
      if (resultCode == RESULT_OK) {
        preferences.edit().putLong(MainActivity.LAST_DONATION_KEY, new Date().getTime()).apply();
        try {
          JSONObject purchaseData = new JSONObject(data.getStringExtra("INAPP_PURCHASE_DATA"));
          new DonationConsumeAsyncTask(activity.getBillingService(), getActivity().getPackageName(),
              purchaseData.getString("purchaseToken")).execute();
          View view = getView();
          if (view != null) {
            Snackbar.make(view, R.string.donation_thanks, Snackbar.LENGTH_LONG)
                .show();
          }
        } catch (JSONException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

// This will be handled with a callback from the donation service above
//  @Override
//  public void onSuccess(List<String> strings) {
//    Log.d(TAG, "Result " + strings.toString());
//    try {
//      List<JSONObject> products = new ArrayList<>();
//      for (String str : strings) {
//        products.add(new JSONObject(str));
//      }
//      showDonationDialog(products);
//    } catch (JSONException e) {
//      onException(e);
//    }
//  }
//
//  @Override
//  public void onException(Exception e) {
//    Log.e(TAG, e.getLocalizedMessage());
//    View view = getView();
//    if (view != null) {
//      Snackbar.make(view, R.string.get_donations_error, Snackbar.LENGTH_LONG)
//          .show();
//    }
//  }

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
     * Retrieves the {@link IInAppBillingService} object.
     *
     * @return the InAppBillingService
     */
    IInAppBillingService getBillingService();
  }

}
