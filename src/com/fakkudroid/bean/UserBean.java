package com.fakkudroid.bean;

import java.util.Calendar;
import java.util.Date;

public class UserBean {

	private String user;
	private String password;
	private boolean checked;
    private String urlUser;
	
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public boolean isChecked() {
		return checked;
	}
	public void setChecked(boolean checked) {
		this.checked = checked;
	}
    public String getUrlUser() {
        return urlUser;
    }
    public void setUrlUser(String urlUser) {
        this.urlUser = urlUser;
    }
}
