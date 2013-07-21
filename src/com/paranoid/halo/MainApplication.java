package com.paranoid.halo;

import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;

import java.util.Map;

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (Utils.getStatus(this)) {
            Map<String, ?> packages = Utils.loadPackageNames(this);
            if(packages != null) {
                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                for(Map.Entry<String,?> entry : packages.entrySet()){
                    Utils.createNotification(this, notificationManager, entry);
                }
            }
        }
    }
}
