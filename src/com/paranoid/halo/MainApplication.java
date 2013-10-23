/*
 * Copyright 2013 ParanoidAndroid Project
 *
 * This file is part of HALO.
 *
 * HALO is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * HALO is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with HALO.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.paranoid.halo;

import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;

import com.paranoid.halo.utils.Notes;
import com.paranoid.halo.utils.Utils;

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        if (Utils.getStatus(this)) {
            String[] packages = Utils.loadArray(this);
            if (packages != null) {
                NotificationManager notificationManager =
                        (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
                for (String packageName : packages) {
                    Utils.createNotification(this, notificationManager, packageName);
                }
            }
        }

        Notes.setAllNotes(this);
    }
}
