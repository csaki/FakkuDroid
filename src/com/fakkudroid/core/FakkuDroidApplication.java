package com.fakkudroid.core;


import android.app.Application;

import com.fakkudroid.bean.DoujinBean;
import com.fakkudroid.bean.SettingBean;
import com.fakkudroid.util.Constants;
import com.fakkudroid.util.Util;

public class FakkuDroidApplication extends Application {

	private DoujinBean current = null;
	private SettingBean settingBean = null;
	
	public SettingBean getSettingBean() {
		DataBaseHandler db = new DataBaseHandler(this.getApplicationContext());
		settingBean = db.getSetting();
		if(settingBean==null)
			settingBean = db.addSetting();
		
		return settingBean;
	}

	public void setSettingBean(SettingBean settingBean) {
		this.settingBean = settingBean;
	}

	public DoujinBean getCurrent() {
		return current;
	}

	public void setCurrent(DoujinBean current) {
		this.current = current;
	}
	
	public String getTitle(int nroPage, String title){
		return title + " #" + nroPage;
	}
	public String getUrl(int nroPage, String url) {
		if (nroPage > 1) {
			return url + Constants.PAGE + nroPage;
		}
		return url;
	}
	public String getUrlFavorite(int nroPage, String user) {
		return Util.escapeURL(Constants.SITEFAVORITE.replace("usr", user) + ((nroPage-1)*15));
	}
}
