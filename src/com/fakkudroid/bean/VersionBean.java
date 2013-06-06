package com.fakkudroid.bean;

public class VersionBean {

	private int id;
	private String version_code, version_url, new_changes, updated_at, created_at;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getVersion_code() {
		return version_code;
	}
	public void setVersion_code(String version_code) {
		this.version_code = version_code;
	}
	public String getVersion_url() {
		return version_url;
	}
	public void setVersion_url(String version_url) {
		this.version_url = version_url;
	}
	public String getNew_changes() {
		return new_changes;
	}
	public void setNew_changes(String new_changes) {
		this.new_changes = new_changes;
	}
	public String getUpdated_at() {
		return updated_at;
	}
	public void setUpdated_at(String updated_at) {
		this.updated_at = updated_at;
	}
	public String getCreated_at() {
		return created_at;
	}
	public void setCreated_at(String created_at) {
		this.created_at = created_at;
	}
}
