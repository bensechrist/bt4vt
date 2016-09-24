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

package com.bt4vt.async;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;

import com.android.vending.billing.IInAppBillingService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ben Sechrist
 */
public class DonationOptionAsyncTask extends AsyncTask<Void, Void, List<String>> {

  private final IInAppBillingService billingService;
  private final String packageName;
  private final AsyncCallback<List<String>> callback;

  public DonationOptionAsyncTask(IInAppBillingService billingService, String packageName,
                                 AsyncCallback<List<String>> callback) {
    this.billingService = billingService;
    this.packageName = packageName;
    this.callback = callback;
  }

  @Override
  protected List<String> doInBackground(Void... params) {
    try {
      ArrayList<String> skuList = new ArrayList<String>();
      skuList.add("donation.1");
      skuList.add("donation.2");
      skuList.add("donation.5");
      skuList.add("donation.10");
      Bundle querySkus = new Bundle();
      querySkus.putStringArrayList("ITEM_ID_LIST", skuList);
      Bundle skuDetails = billingService.getSkuDetails(3, packageName, "inapp", querySkus);
      int response = skuDetails.getInt("RESPONSE_CODE");
      if (response == 0) {
        return skuDetails.getStringArrayList("DETAILS_LIST");
      }
    } catch (RemoteException e) {
      new Handler(Looper.getMainLooper()).post(new CallbackExceptionRunnable(callback, e));
    }
    return null;
  }

  @Override
  protected void onPostExecute(List<String> strings) {
    if (strings != null) {
      callback.onSuccess(strings);
    }
  }
}
