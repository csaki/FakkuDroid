package com.fakkudroid.bean;

public class SettingBean {

	private String user;
	private String password;
	private boolean checked;
	private int reading_mode;
	private String pin;
	
	public static final int JAPANESE_MODE = 0;
	public static final int OCCIDENTAL_MODE = 1;
	
	public String getPin() {
		return pin;
	}
	public void setPin(String pin) {
		this.pin = pin;
	}
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
	public int getReading_mode() {
		return reading_mode;
	}
	public void setReading_mode(int reading_mode) {
		this.reading_mode = reading_mode;
	}
	
	
}
