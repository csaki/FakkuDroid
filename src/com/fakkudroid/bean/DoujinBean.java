package com.fakkudroid.bean;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

import com.fakkudroid.util.Constants;

public class DoujinBean {

	private String title;
	private URLBean serie;
	private URLBean artist;
	private String description;
	private String urlImagePage;
	private String urlImageTitle;
	private String url;
	private int qtyPages;
	private int qtyFavorites;
	private URLBean language;
	private URLBean translator;
	private URLBean uploader;
	private String fecha;
	private List<URLBean> lstTags;
	private boolean addedInFavorite;
	private String timeAgo;
	
	public String getTimeAgo() {
		return timeAgo;
	}

	public void setTimeAgo(String timeAgo) {
		this.timeAgo = timeAgo;
	}

	public String getId() {
		int idxStart = url.lastIndexOf("/") + 1;		
		
		return url.substring(idxStart);
	}
	
	public String urlFavorite(String urlFavorite){
		int idxStart = url.lastIndexOf("/");
		idxStart = url.substring(0, idxStart).lastIndexOf("/") + 1;
		
		return urlFavorite + url.substring(idxStart);
	}
	
	public String urlComments(int page){
		int idxStart = url.lastIndexOf("/");
		idxStart = url.substring(0, idxStart).lastIndexOf("/") + 1;
		
		return Constants.SITECOMMENTS.replace("@id",url.substring(idxStart)).replace("@page", page*20+"");
	}
	
	public String urlRelated(int nroPage){
		int idxStart = url.lastIndexOf("/");
		idxStart = url.substring(0, idxStart).lastIndexOf("/") + 1;
		
		return Constants.SITERELATED + url.substring(idxStart) + "/"  + ((nroPage-1)*10);
	}
	
	public boolean isAddedInFavorite() {
		return addedInFavorite;
	}

	public void setAddedInFavorite(boolean addedInFavorite) {
		this.addedInFavorite = addedInFavorite;
	}

	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public URLBean getSerie() {
		return serie;
	}
	public void setSerie(URLBean serie) {
		this.serie = serie;
	}
	public URLBean getArtist() {
		return artist;
	}
	public void setArtist(URLBean artist) {
		this.artist = artist;
	}
	public String getUrlImagePage() {
		return urlImagePage;
	}
	public void setUrlImagePage(String urlImagePage) {
		this.urlImagePage = urlImagePage;
	}
	public String getUrlImageTitle() {
		return urlImageTitle;
	}
	public void setUrlImageTitle(String urlImageTitle) {
		this.urlImageTitle = urlImageTitle;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public int getQtyPages() {
		return qtyPages;
	}
	public void setQtyPages(int qtyPages) {
		this.qtyPages = qtyPages;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public int getQtyFavorites() {
		return qtyFavorites;
	}
	public void setQtyFavorites(int qtyFavorites) {
		this.qtyFavorites = qtyFavorites;
	}
	public URLBean getLanguage() {
		return language;
	}
	public void setLanguage(URLBean language) {
		this.language = language;
	}
	public URLBean getTranslator() {
		return translator;
	}
	public void setTranslator(URLBean translator) {
		this.translator = translator;
	}
	public URLBean getUploader() {
		return uploader;
	}
	public void setUploader(URLBean uploader) {
		this.uploader = uploader;
	}
	public String getFecha() {
		return fecha;
	}
	public void setFecha(String fecha) {
		this.fecha = fecha;
	}
	public List<URLBean> getLstTags() {
		return lstTags;
	}
	public void setLstTags(List<URLBean> lstTags) {
		this.lstTags = lstTags;
	}
	public List<String> getImages(){
		List<String> result = new ArrayList<String>();
		for (int i = 1; i <= qtyPages; i++) {
			Formatter fmt = new Formatter();
			result.add(getUrlImage() +  fmt.format("%03d",i) + ".jpg");
			fmt.close();
		}
		return result;
	}
	
	public String getUrlImage(){
		int idxStart = Constants.SITEROOT.length();
		int idxEnd = urlImageTitle.indexOf("thumbs",idxStart);
		
		if(idxEnd==-1)
			idxEnd = urlImageTitle.indexOf("cover",idxStart);
		
		String urlImage = urlImageTitle.substring(idxStart,idxEnd);
		urlImage = Constants.SITEIMAGE + urlImage + "images/";
		return urlImage;
	}
	
	public String getTags(){
		String result = "";
		
		for (URLBean url : lstTags) {
			result += url.getDescription() + ", ";
		}
		if(result.length()>0)
			result = result.substring(0, result.length() - ", ".length());
		return result;
	}
	
	@Override
	public String toString() {
		return "DoujinBean [title=" + title + ", serie=" + serie + ", artist="
				+ artist + ", description=" + description + ", urlImagePage="
				+ urlImagePage + ", urlImageTitle=" + urlImageTitle + ", url="
				+ url + ", qtyPages=" + qtyPages + ", qtyFavorites="
				+ qtyFavorites + ", language="
				+ language + ", translator=" + translator + ", uploader="
				+ uploader + ", fecha=" + fecha + ", lstTags=" + lstTags + "]";
	}
	
}
