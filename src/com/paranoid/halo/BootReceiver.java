package com.paranoid.halo;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Utils.getStatus(context)) {
            String[] packages = Utils.loadArray(context);
            if(packages != null) {
                NotificationManager notificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                for(String packageName : packages){
                    Utils.createNotification(context, notificationManager, packageName);
                }
            }
        }
    }
}