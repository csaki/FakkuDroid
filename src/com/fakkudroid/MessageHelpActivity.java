package com.fakkudroid;

import com.fakkudroid.bean.UserBean;
import com.fakkudroid.core.DataBaseHandler;
import com.fakkudroid.core.FakkuDroidApplication;

import android.os.Bundle;
import android.webkit.WebView;
import android.app.Activity;

public class MessageHelpActivity extends Activity {

	FakkuDroidApplication app;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_message_help);

		app = (FakkuDroidApplication) getApplication();
		
		UserBean userBean = app.getSettingBean();
		userBean.setMessageHelp(true);
		
		DataBaseHandler db = new DataBaseHandler(this);
		db.updateSetting(userBean);
		
		WebView wvHelp = (WebView) findViewById(R.id.wbHelp);
		wvHelp.loadDataWithBaseURL(null, getResources().getString(R.string.help_to_fakkudroid),"text/html", "utf-8",null);	
	}
	
	
}
