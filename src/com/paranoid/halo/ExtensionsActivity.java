package com.paranoid.halo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class ExtensionsActivity extends PreferenceActivity {
    @SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.extensions);
        
        Preference pref_note1 = findPreference("ext_note1");
        Preference pref_note2 = findPreference("ext_note2");
        Preference pref_note3 = findPreference("ext_note3");
        Preference pref_note4 = findPreference("ext_note4");

    	SharedPreferences ext_prefs = PreferenceManager.getDefaultSharedPreferences(ExtensionsActivity.this);
        
        String note1 = ext_prefs.getString("ext_note1", null);
		String note2 = ext_prefs.getString("ext_note2", null);
		String note3 = ext_prefs.getString("ext_note3", null);
		String note4 = ext_prefs.getString("ext_note4", null);
        
		pref_note1.setSummary(note1);
		pref_note2.setSummary(note2);
		pref_note3.setSummary(note3);
		pref_note4.setSummary(note4);
        
    }
    
}