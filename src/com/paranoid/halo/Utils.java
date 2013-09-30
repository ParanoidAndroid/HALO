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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Utils {

    private static final String ITEMS = "items";

    public static int getStringHash(String str){
        int sum = 0;
        for(int i = 0; i<str.length(); i++){
            sum += str.charAt(i);
        }
        return sum;
    }

    public static boolean packageExists(Context context, String targetPackage){
        List<ApplicationInfo> packages;
        PackageManager pm = context.getPackageManager();
        packages = pm.getInstalledApplications(0);
        for (ApplicationInfo packageInfo : packages) {
            if(packageInfo.packageName.equals(targetPackage)) return true;
        }
        return false;
    }

    public static Bitmap getCustomApplicationIcon(String packageName, Context context) {
        Bitmap result = null;
        SharedPreferences prefs = context.getSharedPreferences("custom_icons", 0);
        String path = prefs.getString(packageName, null);
        result = BitmapFactory.decodeFile(path);
        return result;
    }
    
    public static Bitmap getApplicationIcon(String packageName, Context context){
        Bitmap appIcon = getCustomApplicationIcon(packageName, context);
        if(appIcon == null) {
            try {
                Drawable icon = context.getPackageManager().getApplicationIcon(packageName);
                Bitmap tmp = ((BitmapDrawable)icon).getBitmap();
                int iconSize = context.getResources()
                        .getDimensionPixelSize(android.R.dimen.notification_large_icon_width);
                int appIconSize = context.getResources()
                        .getDimensionPixelSize(android.R.dimen.app_icon_size);
                Bitmap bmpWithBorder = Bitmap.createBitmap(iconSize, iconSize, tmp.getConfig());
                Canvas canvas = new Canvas(bmpWithBorder);
                canvas.drawColor(Color.TRANSPARENT);
                int margin = (iconSize - appIconSize) / 2;
                canvas.drawBitmap(tmp, null, new Rect(margin, margin,
                        appIconSize + margin, appIconSize + margin), null);
                appIcon = bmpWithBorder;
            } catch (NameNotFoundException e) {
                // Ups!
            }
        }
        return appIcon;
    }
    
    public static Drawable getApplicationIconDrawable(String packageName, Context context){
        Bitmap icon = getCustomApplicationIcon(packageName, context);
        Drawable appIcon = new BitmapDrawable(context.getResources(), icon);
        if(icon == null) {
            try {
                appIcon = context.getPackageManager().getApplicationIcon(packageName);
            } catch (NameNotFoundException e) {
                // Ups!
            }
        }
        return appIcon;
    }

    public static String getApplicationName(String packageName, Context context){
        PackageManager pm = context.getPackageManager();
        ApplicationInfo ai;
        try {
            ai = pm.getApplicationInfo(packageName, 0);
        } catch (final NameNotFoundException e) {
            ai = null;
        }
        final String applicationName = (String) (ai != null ?
                pm.getApplicationLabel(ai) : context.getString(R.string.unknown));
        return applicationName;
    }

    public static void removeCustomApplicationIcon(String packageName, Context context) {
        SharedPreferences prefs = context.getSharedPreferences("custom_icons", 0);
        SharedPreferences.Editor editor = prefs.edit();
        String path = prefs.getString(packageName, null);
        if(path != null) {
            File f = new File(path);
            f.delete();
        }
        editor.remove(packageName);
        editor.commit();
    }

    public static boolean setCustomApplicationIcon(String packageName, String path, Context context) {
        SharedPreferences prefs = context.getSharedPreferences("custom_icons", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(packageName, path);
        return editor.commit();
    }
    
    public static boolean saveStatus(boolean showing, Context context) {
        SharedPreferences prefs = context.getSharedPreferences("status", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("status", showing);
        return editor.commit();  
    }
    
    public static boolean getStatus(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("status", 0);
        return prefs.getBoolean("status", false);
    }

    public static boolean saveArray(String[] array, Context context) {
        SharedPreferences prefs = context.getSharedPreferences("loaded_apps", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.putInt(ITEMS +"_size", array.length);
        for(int i = 0; i<array.length; i++) {
            editor.putString(ITEMS + "_" + i, array[i]);
        }
        return editor.commit();
    }
    
    public static String[] loadArray(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("loaded_apps", 0);
        int size = prefs.getInt(ITEMS + "_size", 0);
        ArrayList<String> packages = new ArrayList<String>();
        if(size != 0) {
            for(int i = 0; i<size; i++){
                String packageName = prefs.getString(ITEMS + "_" + i, null);
                if(packageExists(context, packageName)) {
                    packages.add(packageName);
                }
            }
        }
        String[] array = packages.toArray(new String[packages.size()]);
        return array;
    }

    public static void createNotification(Context context, NotificationManager notificationManager,
                                          String packageName){
        try {
            String appName = Utils.getApplicationName(packageName, context);
            Notification.Builder mBuilder =
                    new Notification.Builder(context)
                            .setSmallIcon(R.drawable.ic_status)
                            .setAutoCancel(false)
                            .setLargeIcon(Utils.getApplicationIcon(packageName, context))
                            .setContentTitle(appName)
                            .setContentText(context.getString(R.string.tap_to_launch));
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                    intent, 0);
            mBuilder.setContentIntent(contentIntent);
            Notification notif = mBuilder.build();
            notif.flags |= Notification.FLAG_ONGOING_EVENT;
            notif.priority = Notification.PRIORITY_MIN;
            notif.tickerText = appName;
            notificationManager.notify(Utils.getStringHash(packageName), notif);
        } catch(NullPointerException npe) {
            // Skip this application
        }
    }
    
    // Check if the current user has installed Paranoid Android ROM
    // Using Yamil's method of Paranoid OTA
    
    public static String getProp(String prop) {
        try {
            Process process = Runtime.getRuntime().exec("getprop " + prop);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                    process.getInputStream()));
            StringBuilder log = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                log.append(line);
            }
            return log.toString();
        } catch (IOException e) {
            // Runtime error
        }
        return null;
    }
}
