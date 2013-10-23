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

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.paranoid.halo.ApplicationsDialog.AppAdapter;
import com.paranoid.halo.ApplicationsDialog.AppItem;
import com.paranoid.halo.showcase.ShowcaseUtils;
import com.paranoid.halo.showcase.ShowcaseView;
import com.paranoid.halo.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends PreferenceActivity {

    private static final String FIRST_RUN = "app_first_run";

    private static final int CUSTOM_USER_ICON = 0;

    private static final int MENU_ADD = 0;
    private static final int MENU_ACTION = 1;
    private static final int MENU_NOTES = 2;

    private NotificationManager mNotificationManager;
    private Context mContext;
    private boolean mShowing;
    private PreferenceScreen mRoot;
    private List<ResolveInfo> mInstalledApps;
    private AppAdapter mAppAdapter;
    private Preference mPreference;
    private File mImageTmp;
    private OnPreferenceClickListener mOnItemClickListener = new OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference arg0) {
            if (mShowing) {
                Toast.makeText(mContext, R.string.stop_to_remove, Toast.LENGTH_SHORT).show();
            } else {
                mRoot.removePreference(arg0);
                Utils.removeCustomApplicationIcon(arg0.getSummary().toString(), mContext);
                savePreferenceItems(false);
                invalidateOptionsMenu();
            }
            return false;
        }
    };

    private ShowcaseView mShowcaseView;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;

        ActionBar bar = getActionBar();
        BitmapDrawable background = new BitmapDrawable(BitmapFactory
                .decodeResource(getResources(), R.drawable.ab_background));
        background.setTileModeX(Shader.TileMode.CLAMP);
        bar.setBackgroundDrawable(background);
        bar.setDisplayShowTitleEnabled(false);

        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mImageTmp = new File(mContext.getCacheDir() + File.separator + "target.tmp");

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
        showFirstRunDialog();

        invalidateOptionsMenu();

        if (ShowcaseUtils.needsShowcase(this, ShowcaseUtils.SHOWCASE_PIN_APPLICATION)) {
            mShowcaseView = ShowcaseView.insertShowcaseViewWithType(ShowcaseView.ITEM_ACTION_ITEM,
                    MENU_ADD, this, R.string.pin_applications,
                            R.string.pin_applications_summary, null);
            mShowcaseView.setShowcaseIndicatorScale(0.5f);
            ShowcaseUtils.setShowcased(this, ShowcaseUtils.SHOWCASE_PIN_APPLICATION);
        }
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
        menu.add(Menu.NONE, MENU_NOTES, 0, R.string.notes)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
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
                        final AppItem info = (AppItem) arg0.getItemAtPosition(arg2);
                        final String packageName = info.packageName;
                        for (int i = 0; i < mRoot.getPreferenceCount(); i++) {
                            if (mRoot.getPreference(i).getSummary()
                                    .equals(packageName)) {
                                return;
                            }
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setTitle(R.string.icon_picker_type)
                                .setItems(R.array.icon_types, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog2, int which) {
                                        mPreference = new Preference(mContext);
                                        mPreference.setOnPreferenceClickListener(mOnItemClickListener);
                                        mPreference.setTitle(info.title);
                                        mPreference.setSummary(packageName);
                                        mPreference.setIcon(info.icon);
                                        mRoot.addPreference(mPreference);
                                        invalidateOptionsMenu();
                                        dialog.cancel();
                                        switch (which) {
                                            case 0: // Default
                                                savePreferenceItems(true);
                                                break;
                                            case 1: // Custom user icon
                                                Intent intent = new Intent(Intent.ACTION_GET_CONTENT
                                                        , null);
                                                intent.setType("image/*");
                                                intent.putExtra("crop", "true");
                                                intent.putExtra("scale", true);
                                                intent.putExtra("scaleUpIfNeeded", false);
                                                intent.putExtra("outputFormat",
                                                        Bitmap.CompressFormat.PNG.toString());
                                                intent.putExtra("aspectX", 1);
                                                intent.putExtra("aspectY", 1);
                                                intent.putExtra("outputX", 162);
                                                intent.putExtra("outputY", 162);
                                                try {
                                                    mImageTmp.createNewFile();
                                                    mImageTmp.setWritable(true, false);
                                                    intent.putExtra(MediaStore.EXTRA_OUTPUT,
                                                            Uri.fromFile(mImageTmp));
                                                    intent.putExtra("return-data", false);
                                                    startActivityForResult(intent, CUSTOM_USER_ICON);
                                                    dialog.cancel();
                                                } catch (IOException e) {
                                                    // We could not write temp file
                                                    e.printStackTrace();
                                                } catch (ActivityNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                                break;
                                        }
                                    }
                                }
                                );
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                });
                dialog.show();
                break;
            case MENU_ACTION:
                if (mShowing) {
                    mShowing = false;
                    for (int i = 0; i < mRoot.getPreferenceCount(); i++) {
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
            case MENU_NOTES:
                Intent intent = new Intent(this, NotesActivity.class);
                this.startActivity(intent);
                break;
        }
        if(mShowcaseView != null) {
            mShowcaseView.hide();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) return;
        switch (requestCode) {
            case CUSTOM_USER_ICON:
                if (resultCode == Activity.RESULT_OK) {
                    File image = new File(mContext.getFilesDir() + File.separator
                            + "icon_" + System.currentTimeMillis() + "."
                            + Bitmap.CompressFormat.PNG.toString().toLowerCase());
                    String path = image.getAbsolutePath();
                    if (mImageTmp.exists()) {
                        mImageTmp.renameTo(image);
                    }
                    image.setReadOnly();
                    String packageName = mPreference.getSummary().toString();
                    Utils.setCustomApplicationIcon(packageName, path, mContext);
                    Drawable d = new BitmapDrawable(getResources(), Utils.getCustomApplicationIcon(
                            packageName, mContext));
                    mPreference.setIcon(d);
                    savePreferenceItems(true);
                } else {
                    if (mImageTmp.exists()) {
                        mImageTmp.delete();
                    }
                }
                break;
        }
    }

    public void savePreferenceItems(boolean create) {
        if (ShowcaseUtils.needsShowcase(this, ShowcaseUtils.SHOWCASE_START_SERVICE)) {
            mShowcaseView = ShowcaseView.insertShowcaseViewWithType(ShowcaseView.ITEM_ACTION_ITEM,
                    MENU_ACTION, this, R.string.start_service, R.string.start_service_summary, null);
            mShowcaseView.setShowcaseIndicatorScale(0.5f);
            ShowcaseUtils.setShowcased(this, ShowcaseUtils.SHOWCASE_START_SERVICE);
        }

        ArrayList<String> items = new ArrayList<String>();
        for (int i = 0; i < mRoot.getPreferenceCount(); i++) {
            String packageName = mRoot.getPreference(i)
                    .getSummary().toString();
            items.add(packageName);
            if (create && mShowing) {
                Utils.createNotification(mContext, mNotificationManager, packageName);
            }
        }
        Utils.saveArray(items.toArray(new String[items.size()]), mContext);
    }

    public void loadPreferenceItems() {
        String[] packages = Utils.loadArray(mContext);
        if (packages == null) return;
        for (String packageName : packages) {
            Preference app = new Preference(mContext);
            app.setTitle(Utils.getApplicationName(packageName, mContext));
            app.setSummary(packageName);
            app.setIcon(Utils.getApplicationIconDrawable(packageName, mContext));
            app.setOnPreferenceClickListener(mOnItemClickListener);
            mRoot.addPreference(app);
        }
    }

    public void showFirstRunDialog() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean firstRun = prefs.getBoolean(FIRST_RUN, true);

        if (firstRun) {
            if (!Utils.isParanoidRom()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setMessage(R.string.no_pa_rom_content)
                        .setTitle(R.string.no_pa_rom_title)
                        .setPositiveButton(R.string.no_pa_rom_ok, null);

                AlertDialog dialog = builder.create();
                dialog.show();
            }
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(FIRST_RUN, false).commit();
        }
    }

}
