package com.paranoid.halo;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.graphics.BitmapFactory;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import com.paranoid.halo.ApplicationsDialog.AppAdapter;
import com.paranoid.halo.ApplicationsDialog.AppItem;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class MainActivity extends PreferenceActivity {
    
    private static final int MENU_ADD = 0;
    private static final int MENU_ACTION = 1;
    
    private NotificationManager mNotificationManager;
    private Context mContext;
    private boolean mShowing;
    private PreferenceScreen mRoot;
    private List<ResolveInfo> mInstalledApps;
    private AppAdapter mAppAdapter;
    private OnPreferenceClickListener mOnItemClickListener = new OnPreferenceClickListener(){
            @Override
            public boolean onPreferenceClick(Preference arg0) {
                if(mShowing){
                    Toast.makeText(mContext, R.string.stop_to_remove, Toast.LENGTH_SHORT).show();
                } else {
                    mRoot.removePreference(arg0);
                    savePreferenceItems(false);
                    invalidateOptionsMenu();
                }
                return false;
            }
        };

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar bar = getActionBar();
        BitmapDrawable background = new BitmapDrawable(BitmapFactory.decodeResource(getResources(), R.drawable.ab_background));
        background.setTileModeX(Shader.TileMode.CLAMP);
        bar.setBackgroundDrawable(background);
        bar.setDisplayShowTitleEnabled(false);

        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mContext = this;
        mShowing = Utils.getStatus(mContext);
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mInstalledApps = mContext.getPackageManager().queryIntentActivities(mainIntent, 0);
        ApplicationsDialog appDialog = new ApplicationsDialog();
        mAppAdapter = appDialog.createAppAdapter(mContext, mInstalledApps);
        mAppAdapter.update();
        setPreferenceScreen(getPreferenceManager().createPreferenceScreen(this));
        mRoot = getPreferenceScreen();
        loadPreferenceItems();
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.getItem(1);
        item.setVisible(mRoot.getPreferenceCount() > 0);
        item.setIcon(mShowing ?
                R.drawable.ic_stop : R.drawable.ic_start);
        item.setTitle(mShowing ?
                R.string.stop : R.string.start);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, MENU_ADD, 0, R.string.add)
            .setIcon(R.drawable.ic_add)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        menu.add(Menu.NONE, MENU_ACTION, 0, R.string.start)
            .setIcon(R.drawable.ic_start)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ADD:
                final ListView list = new ListView(mContext);
                list.setAdapter(mAppAdapter);

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(R.string.choose_app);
                builder.setView(list);
                final Dialog dialog = builder.create();

                list.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1,
                            int arg2, long arg3) {
                        AppItem info = (AppItem) arg0.getItemAtPosition(arg2);
                        for(int i = 0; i<mRoot.getPreferenceCount(); i++){
                            if(mRoot.getPreference(i).getSummary()
                                    .equals(info.packageName)){
                                return;
                            }
                        }
                        Preference item = new Preference(mContext);
                        item.setTitle(info.title);
                        item.setSummary(info.packageName);
                        item.setIcon(info.icon);
                        item.setOnPreferenceClickListener(mOnItemClickListener);
                        mRoot.addPreference(item);
                        savePreferenceItems(true);
                        invalidateOptionsMenu();
                        dialog.cancel();
                    }
                });
                dialog.show();
                break;
            case MENU_ACTION:
                if(mShowing) {
                    mShowing = false;
                    for(int i = 0; i<mRoot.getPreferenceCount(); i++){
                        int hash = Utils.getStringHash(mRoot.getPreference(i)
                                .getSummary().toString());
                        mNotificationManager.cancel(hash);
                    }
                } else {
                    mShowing = true;
                    savePreferenceItems(true);
                }
                Utils.saveStatus(mShowing, mContext);
                invalidateOptionsMenu();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    public void savePreferenceItems(boolean create){
        ArrayList<String> items = new ArrayList<String>();
        for(int i = 0; i<mRoot.getPreferenceCount(); i++){
            String packageName = mRoot.getPreference(i)
                    .getSummary().toString();
            items.add(packageName);
            if(create && mShowing) Utils.createNotification(mContext, mNotificationManager, packageName);
        }
        Utils.saveArray(items.toArray(new String[items.size()]), "items", mContext);
    }
    
    public void loadPreferenceItems(){
        String[] packages = Utils.loadArray("items", mContext);
        if(packages == null) return;
        for(String packageName : packages){
            Preference app = new Preference(mContext);
            app.setTitle(Utils.getApplicationName(packageName, mContext));
            app.setSummary(packageName);
            app.setIcon(Utils.getApplicationIconDrawable(packageName, mContext));
            app.setOnPreferenceClickListener(mOnItemClickListener);
            mRoot.addPreference(app);
        }
    }
}
