package com.fakkudroid;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.androidexplorer.AndroidExplorerActivity;
import com.larswerkman.colorpicker.ColorPickerActivity;

public class PreferencesActivity extends SherlockPreferenceActivity {

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        Preference folderDialogPreference = (Preference) getPreferenceScreen().findPreference("folder_directory");
        folderDialogPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Intent itExplorer = new Intent(PreferencesActivity.this,
                        AndroidExplorerActivity.class);
                startActivity(itExplorer);
                return true;
            }
        });

        Preference colorDialogPreference = (Preference) getPreferenceScreen().findPreference("background_color");
        colorDialogPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Intent it = new Intent(PreferencesActivity.this,
                        ColorPickerActivity.class);
                startActivity(it);
                return true;
            }
        });
    }
	
}
