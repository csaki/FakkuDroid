package com.fakkudroid;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.androidexplorer.AndroidExplorerActivity;
import com.fakkudroid.bean.DoujinBean;
import com.fakkudroid.core.DataBaseHandler;
import com.fakkudroid.util.Helper;
import com.larswerkman.colorpicker.ColorPickerActivity;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class PreferencesActivity extends SherlockPreferenceActivity {

    private ProgressDialog dialog;

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

        Preference scanFolderDialogPreference = (Preference) getPreferenceScreen().findPreference("scan_folder");
        scanFolderDialogPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                dialog = ProgressDialog.show(PreferencesActivity.this, getResources().getString(R.string.pref_title_scan_folder), getResources().getString(R.string.loading), false, true);
                dialog.setIcon(R.drawable.ic_launcher);
                dialog.setCancelable(false);
                new Thread() {
                    public void run() {
                        try {
                            File downloadDir = Helper.getDir("", PreferencesActivity.this);
                            File[] lst = downloadDir.listFiles();
                            for(File f:lst){
                                try{
                                    File json = new File(f, "data.json");
                                    if(json.exists()){
                                        DoujinBean doujinBean = Helper.readJsonDoujin(json);
                                        DataBaseHandler db = new DataBaseHandler(
                                                PreferencesActivity.this);
                                        db.deleteDoujin(doujinBean.getId());
                                        db.addDoujin(doujinBean);
                                    }
                                }catch (Exception e){}
                            }
                        }catch (Exception e) {}
                        dialog.dismiss();
                    }
                }.start();
                return true;
            }
        });

        CheckBoxPreference logFilePreference = (CheckBoxPreference)getPreferenceScreen().findPreference("log_file_checkbox");
        logFilePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                Helper.writeLogFile = (Boolean)o;
                return true;
            }
        });
    }
}
