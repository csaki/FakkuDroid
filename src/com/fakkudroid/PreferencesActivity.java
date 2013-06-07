package com.fakkudroid;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

public class PreferencesActivity extends SherlockPreferenceActivity {

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }
	
}
