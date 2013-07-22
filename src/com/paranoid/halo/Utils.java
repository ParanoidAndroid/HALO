package com.paranoid.halo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
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
import java.util.Map;

public class Utils {

    public static final String MAIN = "main";

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

    public static String[] getActivities(String packageName, Context context) {
        ArrayList<String> activities = new ArrayList<String>();
        activities.clear();
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);

            for (ActivityInfo a : info.activities) {
                activities.add(a.name);
            }
        } catch (NameNotFoundException e) {
            // Package does not exist
        }
        return activities.toArray(new String[activities.size()]);
    }

    public static Bitmap getCustomApplicationIcon(String componentName, Context context) {
        Bitmap result = null;
        SharedPreferences prefs = context.getSharedPreferences("custom_icons", 0);
        String path = prefs.getString(componentName, null);
        result = BitmapFactory.decodeFile(path);
        return result;
    }

    public static Bitmap getApplicationIcon(String componentName, Context context){
        Bitmap appIcon = getCustomApplicationIcon(componentName, context);
        if(appIcon == null) {
            try {
                boolean isPackage = packageExists(context, componentName);
                Drawable icon = context.getPackageManager().getApplicationIcon(isPackage
                        ? componentName : getPackageName(componentName, context));
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
    
    public static Drawable getApplicationIconDrawable(String componentName, Context context){
        Bitmap icon = getCustomApplicationIcon(componentName, context);
        Drawable appIcon = new BitmapDrawable(context.getResources(), icon);
        if(icon == null) {
            try {
                boolean isPackage = packageExists(context, componentName);
                appIcon = context.getPackageManager().getApplicationIcon(isPackage
                        ? componentName : getPackageName(componentName, context));
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

    public static boolean savePackageName(String packageName, Context context) {
        SharedPreferences prefs = context.getSharedPreferences("loaded_apps", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(packageName, MAIN);
        return editor.commit();
    }

    public static boolean saveActivity(String packageName, String activity, Context context) {
        SharedPreferences prefs = context.getSharedPreferences("loaded_apps", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(activity, packageName);
        return editor.commit();
    }

    public static String getPackageName(String key, Context context) {
        SharedPreferences prefs = context.getSharedPreferences("loaded_apps", 0);
        return prefs.getString(key, null);
    }
    
    public static Map<String, ?> loadPackageNames(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("loaded_apps", 0);
        return prefs.getAll();
    }

    public static boolean removeApplicationOrActivity(String input, Context context) {
        SharedPreferences prefs = context.getSharedPreferences("loaded_apps", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(input);
        return editor.commit();
    }

    public static void createNotification(Context context, NotificationManager notificationManager,
                                          Map.Entry<String,?> entry){
        try {
            String key = entry.getKey();
            String value = entry.getValue().toString();
            boolean isPackage = value.equals(MAIN);

            // If entry is an activity, we swap the package position
            // to allow multiple activities attached
            String packageName = isPackage ? key : value;
            String appName = getApplicationName(packageName, context);
            Notification.Builder mBuilder =
                    new Notification.Builder(context)
                            .setSmallIcon(R.drawable.ic_status)
                            .setAutoCancel(false)
                            .setLargeIcon(getApplicationIcon(key, context))
                            .setContentTitle(appName)
                            .setContentText(context.getString(R.string.tap_to_launch));
            Intent intent;
            if(isPackage) {
                intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
            } else {
                intent = new Intent(Intent.ACTION_MAIN);
                ComponentName activity = new ComponentName(packageName, key);
                String shortString = activity.getShortClassName();
                appName += " " + shortString.substring(shortString.lastIndexOf(".")
                        + 1, shortString.length());
                intent.setComponent(activity);
            }
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                    intent, 0);
            mBuilder.setContentIntent(contentIntent);
            Notification notif = mBuilder.build();
            notif.flags |= Notification.FLAG_ONGOING_EVENT;
            notif.priority = Notification.PRIORITY_MIN;
            notif.tickerText = appName;
            notificationManager.notify(Utils.getStringHash(key), notif);
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
