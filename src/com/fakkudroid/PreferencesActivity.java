package com.fakkudroid;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.androidexplorer.AndroidExplorerActivity;
import com.fakkudroid.core.DataBaseHandler;
import com.fakkudroid.util.Helper;
import com.larswerkman.colorpicker.ColorPickerActivity;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class PreferencesActivity extends SherlockPreferenceActivity {

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        Preference folderDialogPreference = (Preference) getPreferenceScreen().findPreference("folder_directory");
        folderDialogPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                //Intent itExplorer = new Intent(PreferencesActivity.this,
                //        AndroidExplorerActivity.class);
                //startActivity(itExplorer);
                Toast.makeText(PreferencesActivity.this, R.string.soon, Toast.LENGTH_SHORT).show();
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

        Preference deleteCoversPreference = (Preference) getPreferenceScreen().findPreference("delete_covers");
        deleteCoversPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                File dir = Helper.getCacheDir(PreferencesActivity.this);
                try {
                    FileUtils.deleteDirectory(dir);
                } catch (IOException e) {
                    Helper.logError(this, e.getMessage(), e);
                }

                Toast.makeText(
                        PreferencesActivity.this,
                        R.string.covers_images_cache_deleted,
                        Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }
	
}
