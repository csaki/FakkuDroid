package com.fakkudroid.bean;

import java.util.Calendar;
import java.util.Date;

public class UserBean {

	private String user;
	private String password;
	private boolean checked;
	private Date dateShowMessageHelp;
	private boolean messageHelp;
	
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
	public Date getDateShowMessageHelp() {
		return dateShowMessageHelp;
	}
	public void setDateShowMessageHelp(Date dateShowMessageHelp) {
		this.dateShowMessageHelp = dateShowMessageHelp;
	}
	public boolean isMessageHelp() {
		return messageHelp;
	}
	public void setMessageHelp(boolean messageHelp) {
		this.messageHelp = messageHelp;
	}
	
	public boolean isShowMessage(){
		Calendar c = Calendar.getInstance(); 
		return !messageHelp&&c.getTime().after(dateShowMessageHelp);
	}
}
