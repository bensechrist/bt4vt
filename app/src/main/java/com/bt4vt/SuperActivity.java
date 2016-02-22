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

package com.bt4vt;

import android.content.Intent;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.ui.auth.core.AuthProviderType;
import com.firebase.ui.auth.core.FirebaseLoginDialog;
import com.firebase.ui.auth.core.FirebaseLoginError;
import com.firebase.ui.auth.core.TokenAuthHandler;

import roboguice.activity.RoboFragmentActivity;

/**
 * Abstract super-class for {@link MainActivity}.
 *
 * @author Ben Sechrist
 */
public abstract class SuperActivity extends RoboFragmentActivity {

  private Firebase.AuthStateListener mAuthStateListener;
  private AuthData mAuthData;
  private FirebaseLoginDialog mDialog;
  private TokenAuthHandler mHandler;

  /**
   * Subclasses of this activity must implement this method and return a valid Firebase reference that
   * can be used to call authentication related methods on. The method is guaranteed *not* to be called
   * before onCreate() has finished.
   *
   * @return a Firebase reference that can be used to call authentication related methods on
   */
  protected abstract Firebase getFirebaseRef();

  /**
   * Returns the data for the currently authenticated users, or null if no user is authenticated.
   *
   * @return the data for the currently authenticated users, or null if no user is authenticated.
   */
  public AuthData getAuth() {
    return mAuthData;
  }

  /**
   * Subclasses of this activity may implement this method to handle when a user logs in.
   *
   * @return void
   */
  protected void onFirebaseLoggedIn(AuthData authData) {
  }

  /**
   * Subclasses of this activity may implement this method to handle when a user logs out.
   *
   * @return void
   */
  protected void onFirebaseLoggedOut() {
  }

  /**
   * Subclasses of this activity should implement this method to handle any potential provider errors
   * like OAuth or other internal errors.
   *
   * @return void
   */
  protected abstract void onFirebaseLoginProviderError(FirebaseLoginError firebaseError);

  /**
   * Subclasses of this activity should implement this method to handle any potential user errors
   * like entering an incorrect password or closing the login dialog.
   *
   * @return void
   */
  protected abstract void onFirebaseLoginUserError(FirebaseLoginError firebaseError);

  /**
   * Calling this method will log out the currently authenticated user. It is only legal to call
   * this method after the `onStart()` method has completed.
   */
  public void logout() {
    mDialog.logout();
  }

  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    mDialog.onActivityResult(requestCode, resultCode, data);
  }

  /**
   * Calling this method displays the Firebase login dialog over the current activity, allowing the
   * user to authenticate with any of the configured providers. It is only legal to call this
   * method after the `onStart()` method has completed.
   */
  public void showFirebaseLoginPrompt() {
    mDialog.show(getFragmentManager(), "");
  }

  public void dismissFirebaseLoginPrompt() {
    mDialog.dismiss();
  }

  public void resetFirebaseLoginPrompt() {
    mDialog.reset();
  }

  /**
   * Enables authentication with the specified provider.
   *
   * @param provider the provider to enable.
   */
  public void setEnabledAuthProvider(AuthProviderType provider) {
    mDialog.setEnabledProvider(provider);
  }

  @Override
  protected void onStart() {
    super.onStart();
    mHandler = new TokenAuthHandler() {
      @Override
      public void onSuccess(AuthData data) {
               /* onFirebaseLoginSuccess is called by the AuthStateListener below */
      }

      @Override
      public void onUserError(FirebaseLoginError err) {
        onFirebaseLoginUserError(err);
      }

      @Override
      public void onProviderError(FirebaseLoginError err) {
        onFirebaseLoginProviderError(err);
      }
    };

    mAuthStateListener = new Firebase.AuthStateListener() {
      @Override
      public void onAuthStateChanged(AuthData authData) {
        if (authData != null) {
          mAuthData = authData;
          onFirebaseLoggedIn(authData);
        } else {
          mAuthData = null;
          onFirebaseLoggedOut();
        }
      }
    };

    mDialog = new FirebaseLoginDialog();
    mDialog
        .setContext(this)
        .setRef(getFirebaseRef())
        .setHandler(mHandler);

    getFirebaseRef().addAuthStateListener(mAuthStateListener);
  }

  @Override
  protected void onStop() {
    super.onStop();
    getFirebaseRef().removeAuthStateListener(mAuthStateListener);
    mDialog.cleanUp();
  }

  @Override
  protected void onPause() {
    super.onPause();
    mDialog.cleanUp();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mDialog.cleanUp();
  }

}
