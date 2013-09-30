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
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

public class ExtensionsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener{
	
	public static final String KEY_EXT_NOTE_1 = "ext_note1";
	public static final String KEY_EXT_NOTE_2 = "ext_note2";
	public static final String KEY_EXT_NOTE_3 = "ext_note3";
	public static final String KEY_EXT_NOTE_4 = "ext_note4";
	
	@SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.extensions);
        
    	Preference pref_note1 = findPreference("ext_note1");
        Preference pref_note2 = findPreference("ext_note2");
        Preference pref_note3 = findPreference("ext_note3");
        Preference pref_note4 = findPreference("ext_note4");
        
    	SharedPreferences ext_prefs = PreferenceManager.getDefaultSharedPreferences(this);
        
    	String note1 = ext_prefs.getString("ext_note1", null);
    	String note2 = ext_prefs.getString("ext_note2", null);
    	String note3 = ext_prefs.getString("ext_note3", null);
    	String note4 = ext_prefs.getString("ext_note4", null);
        
        pref_note1.setSummary(note1);
        pref_note2.setSummary(note2);
        pref_note3.setSummary(note3);
        pref_note4.setSummary(note4);
        
    }

    @SuppressWarnings("deprecation")
	@Override
    protected void onResume(){
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }
    
	public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key){
		Preference pref_note1 = findPreference("ext_note1");
	    Preference pref_note2 = findPreference("ext_note2");
	    Preference pref_note3 = findPreference("ext_note3");
	    Preference pref_note4 = findPreference("ext_note4");
	    
		SharedPreferences ext_prefs = PreferenceManager.getDefaultSharedPreferences(this);
	    
		String note1 = ext_prefs.getString("ext_note1", null);
		String note2 = ext_prefs.getString("ext_note2", null);
		String note3 = ext_prefs.getString("ext_note3", null);
		String note4 = ext_prefs.getString("ext_note4", null);		
		
		NotificationManager ext_notification_manager =
				(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		
		Intent intent = new Intent(this, ExtensionsActivity.class);
	    PendingIntent ext_intent = PendingIntent.getActivity(this, 0, intent, 0);
	
	    if (key.equals(KEY_EXT_NOTE_1)) {
    		NotificationCompat.Builder ext_builder =
			        new NotificationCompat.Builder(this)
					.setSmallIcon(R.drawable.ic_add)
					.setContentIntent(ext_intent)
			        .setContentTitle(note1);
				
				 Notification notif = ext_builder.build();
		            notif.flags |= Notification.FLAG_ONGOING_EVENT;
		            notif.priority = Notification.PRIORITY_MIN;
		            notif.tickerText = note1;
				ext_notification_manager.notify(1, notif);
				
				pref_note1.setSummary(note1);
    	}
    	
    	if(note1.length() == 0){
			ext_notification_manager.cancel(1);
			pref_note1.setSummary(note1);
		}
    	
    	if (key.equals(KEY_EXT_NOTE_2)) {
    		NotificationCompat.Builder ext_builder =
			        new NotificationCompat.Builder(this)
					.setSmallIcon(R.drawable.ic_add)
					.setContentIntent(ext_intent)
			        .setContentTitle(note2);
				
				 Notification notif = ext_builder.build();
		            notif.flags |= Notification.FLAG_ONGOING_EVENT;
		            notif.priority = Notification.PRIORITY_MIN;
		            notif.tickerText = note2;
				ext_notification_manager.notify(2, notif);
				
				pref_note2.setSummary(note2);
    	}
    	
    	if(note1.length() == 0){
			ext_notification_manager.cancel(2);
			pref_note2.setSummary(note2);
		}
    	
    	if (key.equals(KEY_EXT_NOTE_3)) {
    		NotificationCompat.Builder ext_builder =
			        new NotificationCompat.Builder(this)
					.setSmallIcon(R.drawable.ic_add)
					.setContentIntent(ext_intent)
			        .setContentTitle(note3);
				
				 Notification notif = ext_builder.build();
		            notif.flags |= Notification.FLAG_ONGOING_EVENT;
		            notif.priority = Notification.PRIORITY_MIN;
		            notif.tickerText = note3;
				ext_notification_manager.notify(3, notif);
				
				pref_note3.setSummary(note3);
    	}
    	
    	if(note1.length() == 0){
			ext_notification_manager.cancel(3);
			pref_note3.setSummary(note3);
		}
    	
    	if (key.equals(KEY_EXT_NOTE_4)) {
    		NotificationCompat.Builder ext_builder =
			        new NotificationCompat.Builder(this)
					.setSmallIcon(R.drawable.ic_add)
					.setContentIntent(ext_intent)
			        .setContentTitle(note4);
				
				 Notification notif = ext_builder.build();
		            notif.flags |= Notification.FLAG_ONGOING_EVENT;
		            notif.priority = Notification.PRIORITY_MIN;
		            notif.tickerText = note4;
				ext_notification_manager.notify(4, notif);
				
				pref_note4.setSummary(note4);
    	}
    	
    	if(note1.length() == 0){
			ext_notification_manager.cancel(4);
			pref_note4.setSummary(note4);
		}
    	
    }
    
}