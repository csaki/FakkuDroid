package com.fakkudroid.bean;

public class URLBean {

	private String url;
	private String description;
	private int icon;

	public URLBean(){}
	
	public URLBean(String description){
		this.description = description;
	}
	
	public URLBean(String url, String description){
		this.url = url;
		this.description = description;
	}
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "URLBean [url=" + url + ", description=" + description + "]";
	}

	public int getIcon() {
		return icon;
	}

	public void setIcon(int icon) {
		this.icon = icon;
	}
}
