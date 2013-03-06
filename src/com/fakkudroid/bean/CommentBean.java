package com.fakkudroid.bean;

public class CommentBean {

	private URLBean user;
	private String comment;
	private int level;
	private String id;
	private String date;
	private int rank;
	private int selectRank;
	private String urlLike;
	private String urlDislike;
	private int selectLike;
	
	public String getUrlLike() {
		return urlLike;
	}

	public void setUrlLike(String urlLike) {
		this.urlLike = urlLike;
	}

	public String getUrlDislike() {
		return urlDislike;
	}

	public void setUrlDislike(String urlDislike) {
		this.urlDislike = urlDislike;
	}

	public int getSelectRank() {
		return selectRank;
	}

	public void setSelectRank(int selectRank) {
		this.selectRank = selectRank;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public URLBean getUser() {
		return user;
	}

	public void setUser(URLBean user) {
		this.user = user;
	}
	
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}
	public String getStrRank(){
		String str = "";
		if(rank>0){
			str = "(+" + rank + ")";
		}else if(rank<0){
			str = "(" + rank + ")";
		}
		return str;
	}

	public int getSelectLike() {
		return selectLike;
	}

	public void setSelectLike(int selectLike) {
		this.selectLike = selectLike;
	}
}
