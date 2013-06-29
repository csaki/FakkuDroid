package com.fakkudroid;

import android.os.Bundle;
import android.preference.Preference;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

public class PreferencesActivity extends SherlockPreferenceActivity {

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        Preference folderDialogPreference = (Preference) getPreferenceScreen().findPreference("folder_directory");
        folderDialogPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                // dialog code here
                return true;
            }
        });

        Preference colorDialogPreference = (Preference) getPreferenceScreen().findPreference("background_color");
        colorDialogPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                // dialog code here
                return true;
            }
        });
    }
	
}
