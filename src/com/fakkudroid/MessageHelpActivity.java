package com.fakkudroid;

import com.fakkudroid.core.FakkuDroidApplication;
import com.fakkudroid.util.Constants;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.webkit.WebView;
import android.app.Activity;
import android.content.SharedPreferences;

public class MessageHelpActivity extends Activity {

	FakkuDroidApplication app;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_message_help);

		app = (FakkuDroidApplication) getApplication();


		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		prefs.edit().putBoolean(Constants.SHOW_MESSAGE_HELP, false).commit();
		
		WebView wvHelp = (WebView) findViewById(R.id.wbHelp);
		wvHelp.loadDataWithBaseURL(null, getResources().getString(R.string.help_to_fakkudroid),"text/html", "utf-8",null);	
	}
	
	public void close(View view) {
		finish();
	}
}
