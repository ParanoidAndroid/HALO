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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class Utils {

    public static int getStringHash(String str){
        int sum = 0;
        for(int i = 0; i<str.length(); i++){
            sum += str.charAt(i);
        }
        return sum;
    }
    
    public static Bitmap getApplicationIcon(String packageName, Context context){
        Bitmap result = null;
        try {
            Drawable icon = context.getPackageManager().getApplicationIcon(packageName);
            Bitmap tmp = ((BitmapDrawable)icon).getBitmap();
            int iconSize = context.getResources().getDimensionPixelSize(android.R.dimen.notification_large_icon_width);
            int appIconSize = context.getResources().getDimensionPixelSize(android.R.dimen.app_icon_size);
            Bitmap bmpWithBorder = Bitmap.createBitmap(iconSize, iconSize, tmp.getConfig());
            Canvas canvas = new Canvas(bmpWithBorder);
            canvas.drawColor(Color.TRANSPARENT);
            int margin = (iconSize - appIconSize) / 2;
            canvas.drawBitmap(tmp, null, new Rect(margin, margin, appIconSize+margin, appIconSize+margin), null);
            result = bmpWithBorder;
        } catch (NameNotFoundException e) {
            // Ups!
        }
        return result;
    }
    
    public static Drawable getApplicationIconDrawable(String packageName, Context context){
        Drawable appIcon = null;
        try {
            appIcon = context.getPackageManager().getApplicationIcon(packageName);
        } catch (NameNotFoundException e) {
            // Ups!
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

    public static boolean saveArray(String[] array, String arrayName, Context context) {
        SharedPreferences prefs = context.getSharedPreferences("loaded_apps", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.putInt(arrayName +"_size", array.length);
        for(int i = 0; i<array.length; i++) {
            editor.putString(arrayName + "_" + i, array[i]);
        }
        return editor.commit();
    }
    
    public static String[] loadArray(String arrayName, Context context) {
        SharedPreferences prefs = context.getSharedPreferences("loaded_apps", 0);
        int size = prefs.getInt(arrayName + "_size", 0);
        String[] array = null;
        if(size != 0) {
            array = new String[size];
            for(int i = 0; i<size; i++){
                array[i] = prefs.getString(arrayName + "_" + i, null);
            }
        }
        return array;
    }

    public static void createNotification(Context context, NotificationManager notificationManager, String packageName){
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
    }
}
