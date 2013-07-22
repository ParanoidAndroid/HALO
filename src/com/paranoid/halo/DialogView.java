package com.paranoid.halo;


import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.List;

public class DialogView extends ScrollView {

    private DialogCallback mCallback;

    public DialogView(final Context context, List<ResolveInfo> installedApps) {
        super(context);
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        final LayoutInflater layoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for(int i= 0; i<installedApps.size(); i++){
            final ResolveInfo app = installedApps.get(i);

            PackageManager pm = context.getPackageManager();
            final ItemInfo info = new ItemInfo();
            info.packageName = app.activityInfo.packageName;
            info.title = app.loadLabel(pm).toString();
            info.icon = app.loadIcon(pm);
            info.activities = Utils.getActivities(info.packageName, context);

            View appView = layoutInflater.inflate(R.layout.preference_icon, null, false);
            appView.setClickable(true);
            appView.setBackgroundResource(R.drawable.layout_background);
            TextView title = (TextView) appView.findViewById(android.R.id.title);
            ImageView icon = (ImageView) appView.findViewById(R.id.icon);

            final LinearLayout activityList = (LinearLayout) appView.findViewById(R.id.activity_list);
            final ImageView activities = (ImageView) appView.findViewById(R.id.activities);
            activities.setImageResource(R.drawable.expand);
            activities.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    boolean visible = activityList.getVisibility() == View.VISIBLE;
                    if(visible) {
                        activities.setImageResource(R.drawable.expand);
                        activityList.setVisibility(View.GONE);
                        activityList.removeAllViews();
                    } else {
                        for(String activity : info.activities) {
                            TextView t = new TextView(context);
                            t.setTextAppearance(context, android.R.attr.textAppearanceMedium);
                            t.setPadding(30, 0, 0, 0);
                            t.setSingleLine();
                            t.setEllipsize(TextUtils.TruncateAt.END);
                            t.setText(activity);
                            activityList.addView(t);
                        }
                        activities.setImageResource(R.drawable.collapse);
                        activityList.setVisibility(View.VISIBLE);
                    }
                }
            });

            appView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    boolean visible = activityList.getVisibility() == View.VISIBLE;
                    if(visible) {
                        mCallback.onActivityClicked(info);
                    } else {
                        mCallback.onApplicationClicked(info);
                    }
                }
            });

            title.setText(info.title);
            icon.setImageDrawable(info.icon);

            container.addView(appView);
        }

        addView(container);
    }

    public void addCallback(DialogCallback callback) {
        mCallback = callback;
    }

    public interface DialogCallback {
        public abstract void onApplicationClicked(ItemInfo info);
        public abstract void onActivityClicked(ItemInfo info);
    }

    public class ItemInfo {
        public String packageName;
        public String title;
        public Drawable icon;
        public String[] activities;
    }
}
