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
import android.content.pm.PackageManager;
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
import android.preference.PreferenceScreen;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MainActivity extends PreferenceActivity implements DialogView.DialogCallback {

    private static final int CUSTOM_USER_ICON = 0;
    
    private static final int MENU_ADD = 0;
    private static final int MENU_ACTION = 1;
    private static final int MENU_EXTENSIONS = 2;
    
    private NotificationManager mNotificationManager;
    private Context mContext;
    private boolean mShowing;
    private PreferenceScreen mRoot;
    private List<ResolveInfo> mInstalledApps;
    private PackageManager mPackageManager;
    private Preference mPreference;
    private File mImageTmp;
    private Dialog mDialog;

    private OnPreferenceClickListener mOnItemClickListener = new OnPreferenceClickListener(){
            @Override
            public boolean onPreferenceClick(Preference arg0) {
                if(mShowing){
                    Toast.makeText(mContext, R.string.stop_to_remove, Toast.LENGTH_SHORT).show();
                } else {
                    mRoot.removePreference(arg0);
                    String componentName = arg0.getSummary().toString();
                    Utils.removeCustomApplicationIcon(componentName, mContext);
                    Utils.removeApplicationOrActivity(componentName, mContext);
                    invalidateOptionsMenu();
                }
                return false;
            }
        };

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
        mPackageManager =  mContext.getPackageManager();
        mInstalledApps = mPackageManager.queryIntentActivities(mainIntent, 0);
        Collections.sort(mInstalledApps, new ResolveInfo.DisplayNameComparator(mPackageManager));
        setPreferenceScreen(getPreferenceManager().createPreferenceScreen(this));
        mRoot = getPreferenceScreen();
        loadPreferenceItems();
        helperDialogs();        

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
        menu.add(Menu.NONE, MENU_EXTENSIONS, 0, R.string.extensions)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ADD:
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(R.string.choose_app);
                DialogView view = new DialogView(mContext, mInstalledApps);
                view.addCallback(this);
                builder.setView(view);
                mDialog = builder.create();
                mDialog.show();
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
            case MENU_EXTENSIONS:
                Intent intent = new Intent(this, ExtensionsActivity.class);
    	        this.startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onApplicationClicked(final DialogView.ItemInfo info) {
        String packageName = info.packageName;
        for(int i = 0; i<mRoot.getPreferenceCount(); i++){
            if(mRoot.getPreference(i).getSummary()
                    .equals(packageName)){
                mDialog.dismiss();
                return;
            }
        }
        iconPickerDialog(info, packageName);
    }

    @Override
    public void onActivityClicked(final DialogView.ItemInfo info){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.choose_activity);
        builder.setItems(info.activities, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                String activity = info.activities[item];
                for(int i = 0; i<mRoot.getPreferenceCount(); i++){
                    if(mRoot.getPreference(i).getSummary()
                            .equals(activity)){
                        return;
                    }
                }
                iconPickerDialog(info, activity);
                mDialog.dismiss();
            }
        });
        //Dialog alert = builder.create();
        builder.show();
        //alert.show();
    }

    public void iconPickerDialog(final DialogView.ItemInfo info, final String extraInfo){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.icon_picker_type)
                .setItems(R.array.icon_types, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog2, int which) {
                        mPreference = new Preference(mContext);
                        mPreference.setOnPreferenceClickListener(mOnItemClickListener);
                        mPreference.setTitle(info.title);
                        mPreference.setSummary(extraInfo);
                        mPreference.setIcon(info.icon);
                        mPreference.setKey(info.packageName);
                        mRoot.addPreference(mPreference);
                        invalidateOptionsMenu();
                        mDialog.cancel();
                        switch(which) {
                            case 0: // Default
                                savePreferenceItems(true);
                                break;
                            case 1: // Custom user icon
                                Intent intent = new Intent(Intent.ACTION_GET_CONTENT,
                                        null);
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
                                    mDialog.cancel();
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
        Dialog iconDialog = builder.create();
        iconDialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) return;
        switch (requestCode) {
            case CUSTOM_USER_ICON:
                if (resultCode == Activity.RESULT_OK) {
                    File image = new File(mContext.getFilesDir() + File.separator
                            + "icon_" + System.currentTimeMillis() + ".png");
                    String path = image.getAbsolutePath();
                    if (mImageTmp.exists()) {
                        mImageTmp.renameTo(image);
                    }
                    image.setReadOnly();
                    String componentName = mPreference.getSummary().toString();
                    Utils.setCustomApplicationIcon(componentName, path, mContext);
                    Drawable d = new BitmapDrawable(getResources(), Utils.getCustomApplicationIcon(
                            componentName, mContext));
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

    public void savePreferenceItems(boolean create){
        for(int i = 0; i<mRoot.getPreferenceCount(); i++) {
            final String activityName = mRoot.getPreference(i)
                    .getSummary().toString();
            final String packageName = mRoot.getPreference(i).getKey();
            final boolean isPackage = Utils.packageExists(mContext, activityName);
            if(isPackage) {
                Utils.savePackageName(activityName, mContext);
            } else {
                Utils.saveActivity(packageName, activityName, mContext);
            }
            Map.Entry<String,?> entry = new Map.Entry<String, Object>() {
                @Override
                public String getKey() {
                    return isPackage ? packageName : activityName;
                }

                @Override
                public Object getValue() {
                    return isPackage ? Utils.MAIN : packageName;
                }

                @Override
                public Object setValue(Object o) {
                    return null;
                }
            };

            if(create && mShowing) Utils.createNotification(mContext, mNotificationManager, entry);
        }
    }

    public void loadPreferenceItems(){
        Map<String, ?> packages = Utils.loadPackageNames(mContext);
        if(packages == null) return;
        for(Map.Entry<String,?> entry : packages.entrySet()){
            String key = entry.getKey();
            String value = entry.getValue().toString();
            boolean isPackage = value.equals(Utils.MAIN);
            String packageName = isPackage ? key : value;
            Preference pref = new Preference(mContext);
            pref.setIcon(Utils.getApplicationIconDrawable(key, mContext));
            pref.setTitle(Utils.getApplicationName(packageName, mContext));
            pref.setOnPreferenceClickListener(mOnItemClickListener);
            pref.setKey(packageName);
            if (isPackage) {
                if(Utils.packageExists(mContext, packageName)) {
                    pref.setSummary(packageName);
                }
            } else {
                pref.setSummary(key);
            }
            mRoot.addPreference(pref);
        }
    }
    
    public void helperDialogs(){
    	// On first run: Show a Dialog to explain the user the utility of Halo))).
        // We will store the firstrun as a SharedPreference.

        boolean firstrun = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("firstrun", true);

        if (firstrun) {
        	// Create HelperActivity as a dialog
        	Intent intent = new Intent(this, HelperActivity.class);
	        this.startActivity(intent);

        	// Save a shared Preference explaining to the app that it has been run previously
        	getSharedPreferences("PREFERENCE", MODE_PRIVATE)
        		.edit()
        		.putBoolean("firstrun", false)
        		.commit();        	
        }
        else{
        	//Try to check if the device has PA installed.
    		
    		String hasPa = Utils.getProp("ro.pa");
    		
    		if(hasPa.equals("true")){
    			// You're clever dude! No advice must be shown!
            }
    		else{
    			AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

    			builder.setMessage(R.string.nopa_content)
    			       .setTitle(R.string.nopa_title)
    			       .setPositiveButton(R.string.nopa_download, new DialogInterface.OnClickListener() {
    			           public void onClick(DialogInterface dialog, int id) {
    			        	   String url = "http://goo.im/devs/paranoidandroid/roms";
    			        	   Intent i = new Intent(Intent.ACTION_VIEW);
    			        	   i.setData(Uri.parse(url));
    			        	   startActivity(i);
    			           }
    			       })   
    			       .setNegativeButton(R.string.nopa_ok, new DialogInterface.OnClickListener() {
    			           public void onClick(DialogInterface dialog, int id) {
    			        	   dialog.dismiss();
    			           }
    			       });

    			AlertDialog nopa_dialog = builder.create();
    			nopa_dialog.show();
    		}
        }
    }
}
