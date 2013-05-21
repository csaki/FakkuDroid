package com.fakkudroid;

import java.io.File;
import java.io.IOException;

import com.fakkudroid.core.FakkuDroidApplication;
import com.fakkudroid.util.Constants;

import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;

public class PasswordActivity extends Activity {

	private FakkuDroidApplication app;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_password);

		app = (FakkuDroidApplication) getApplication();

		createFolders();
		
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		if (prefs.getString("password_text", "").equals("")) {
			unlock();
		}
	}
	
	private void createFolders(){
		File file = null;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String settingDir = prefs.getString("dir_download", "0");
		
		if(settingDir.equals(Constants.EXTERNAL_STORAGE + "")){
			String state = Environment.getExternalStorageState();
			if(Environment.MEDIA_MOUNTED.equals(state)){
				file = new File(Environment.getExternalStorageDirectory() + Constants.DIRECTORY);
				boolean success = true;
				if(!file.exists()){
					success = file.mkdirs();
				}
				
				if(!success)
					file = null;
			}
		}
		if(file == null)
			file = new File(Environment.getRootDirectory() + Constants.DIRECTORY);
		
		if(!file.exists()){
			file.mkdirs();
		}
		
		File nomedia = new File(file, ".nomedia");
		try {
			nomedia.createNewFile();
		} catch (IOException e) {
			Log.e(PasswordActivity.class.getName(), "Error creating .nomedia file.", e);
		}
	}
	
	private void unlock(){
		Intent it = new Intent(this, MainActivity.class);
		this.startActivity(it);

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		
		if (prefs.getBoolean(Constants.SHOW_MESSAGE_HELP, true)) {
			it = new Intent(this, MessageHelpActivity.class);
			this.startActivity(it);
		}
		finish();
	}
	
	public void close(View view) {
		finish();
	}

	public void checkPassword(View view) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		EditText etPassword = (EditText) findViewById(R.id.etPassword);
		if (!prefs.getString("password_text", "").equals(
				etPassword.getText().toString())) {
			Toast.makeText(this,
					getResources().getString(R.string.password_incorrect),
					Toast.LENGTH_SHORT).show();
		} else {
			unlock();
		}
	}
}
