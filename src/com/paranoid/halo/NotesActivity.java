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

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

import com.paranoid.halo.utils.Notes;

import static com.paranoid.halo.utils.Notes.NOTES;

public class NotesActivity
        extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceScreen preferenceScreen = getPreferenceManager().createPreferenceScreen(this);
        setPreferenceScreen(preferenceScreen);

        for(int i = 0; i<NOTES.length; i++) {
            EditTextPreference pref = new EditTextPreference(this);

            // Load values inside preference
            pref.setKey(NOTES[i]);
            pref.setTitle(getString(R.string.note) + " " + (i + 1));
            pref.setDialogTitle(pref.getTitle());
            setPreferenceSummary(NOTES[i], pref);
            preferenceScreen.addPreference(pref);
        }

        SharedPreferences prefs = preferenceScreen.getSharedPreferences();
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    @SuppressWarnings("deprecation")
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Notes.setNoteByKey(this, key);
        setPreferenceSummary(key,
                (EditTextPreference) getPreferenceManager().findPreference(key));
    }

    public void setPreferenceSummary(String key, EditTextPreference preference) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String note = prefs.getString(key, null);
        preference.setSummary(note == null || note.isEmpty() ? getString(R.string.empty) : note);
    }
}
