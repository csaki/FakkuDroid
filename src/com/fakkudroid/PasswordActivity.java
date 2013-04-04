package com.fakkudroid;

import com.fakkudroid.core.FakkuDroidApplication;

import android.os.Bundle;
import android.preference.PreferenceManager;
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

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		if (prefs.getString("password_text", "").equals("")) {
			unlock();
		}
	}
	
	private void unlock(){
		Intent it = new Intent(this, DoujinListActivity.class);
		this.startActivity(it);

		if (app.getSettingBean().isShowMessage()) {
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
