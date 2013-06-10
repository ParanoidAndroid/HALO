package com.paranoid.halo;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class ExtensionsActivity extends PreferenceActivity {
    @SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.extensions);
    }
}
