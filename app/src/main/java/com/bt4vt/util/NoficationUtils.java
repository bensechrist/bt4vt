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

package com.bt4vt.util;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.bt4vt.R;

import java.util.List;

/**
 * Utility for working with notifications.
 *
 * @author Ben Sechrist
 */
public class NoficationUtils {

  public static NotificationCompat.Builder generateInboxBuilder(Context context, String title,
                                                                List<String> content,
                                                                Intent resultIntent,
                                                                int pIntentCode) {
    if (content.isEmpty()) {
      content.add(context.getString(R.string.empty_departures_text));
    }

    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
        .setSmallIcon(R.drawable.bus)
        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher))
        .setAutoCancel(true)
        .setShowWhen(true)
        .setDefaults(NotificationCompat.DEFAULT_ALL)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .setContentTitle(title)
        .setContentText(content.get(0));

    NotificationCompat.InboxStyle inboxStyle =
        new NotificationCompat.InboxStyle();

    inboxStyle.setBigContentTitle(title);

    for (String line : content) {
      inboxStyle.addLine(line);
    }

    mBuilder.setStyle(inboxStyle);

    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
    stackBuilder.addNextIntent(resultIntent);
    PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(pIntentCode,
        PendingIntent.FLAG_UPDATE_CURRENT);
    mBuilder.setContentIntent(resultPendingIntent);
    return mBuilder;
  }
}
