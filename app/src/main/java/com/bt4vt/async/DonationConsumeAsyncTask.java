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
import android.os.RemoteException;
import android.util.Log;

import com.android.vending.billing.IInAppBillingService;

/**
 * @author Ben Sechrist
 */
public class DonationConsumeAsyncTask extends AsyncTask<Void, Void, Void> {

  private static final String TAG = "ConsumePurchaseTask";

  private final IInAppBillingService billingService;
  private final String packageName;
  private final String token;

  public DonationConsumeAsyncTask(IInAppBillingService billingService, String packageName, String token) {
    this.billingService = billingService;
    this.packageName = packageName;
    this.token = token;
  }

  @Override
  protected Void doInBackground(Void... params) {
    try {
      billingService.consumePurchase(3, packageName, token);
    } catch (RemoteException e) {
      Log.e(TAG, e.getLocalizedMessage());
    }
    return null;
  }
}
