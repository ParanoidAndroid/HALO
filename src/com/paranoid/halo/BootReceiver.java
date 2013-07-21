package com.paranoid.halo;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Map;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Utils.getStatus(context)) {
            Map<String, ?> packages = Utils.loadPackageNames(context);
            if(packages != null) {
                NotificationManager notificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                for(Map.Entry<String,?> entry : packages.entrySet()){
                    Utils.createNotification(context, notificationManager, entry);
                }
            }
        }
    }
}