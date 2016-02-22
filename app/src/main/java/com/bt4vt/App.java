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

import android.app.Application;

import com.bt4vt.repository.module.TransitModule;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import roboguice.RoboGuice;

/**
 * Custom application
 *
 * @author Ben Sechrist
 */
@ReportsCrashes(
    mailTo = "bensechrist@gmail.com",
    mode = ReportingInteractionMode.DIALOG,
    resToastText = R.string.crash_toast_text,
    resDialogIcon = R.drawable.bus,
    resDialogTitle = R.string.app_name,
    resDialogText = R.string.crash_dialog_text,
    resDialogCommentPrompt = R.string.crash_dialog_comment_prompt
)
public class App extends Application {

  @Override
  public void onCreate() {
    super.onCreate();

    RoboGuice.setUseAnnotationDatabases(false);
    // Setup RoboGuice modules
    RoboGuice.getOrCreateBaseApplicationInjector(this,
        RoboGuice.DEFAULT_STAGE,
        RoboGuice.newDefaultRoboModule(this), new TransitModule());

    // Setup ACRA
    ACRA.init(this);
  }
}
