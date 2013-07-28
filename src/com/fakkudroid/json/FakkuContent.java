package com.fakkudroid.json;

import com.fakkudroid.bean.URLBean;
import com.fakkudroid.util.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cesar on 01/07/13.
 */
public class FakkuContent {

    private long content_id;
    private boolean content_active;
    private String name;
    private String clean_name;
    private String description;
    private long filesize;
    private int pages;
    private long date;
    private int favorites;
    private int comments;
    private String cover;
    private String sample;
    private String thumbnails;
    private String username;
    private String user_displayname;
    private String attribute;
    private String category;
    private String language;
    private String directory;
    private String url;
    private ArrayLink artists;
    private ArrayLink series;
    private ArrayLink tags;
    private ArrayLink translators;

    public class ArrayLink{

        private List<String> names;
        private List<String> links;
        private List<String> ids;

        public List<String> getNames() {
            return names;
        }

        public void setNames(List<String> names) {
            this.names = names;
        }

        public List<String> getLinks() {
            return links;
        }

        public void setLinks(List<String> links) {
            this.links = links;
        }

        public List<String> getIds() {
            return ids;
        }

        public void setIds(List<String> ids) {
            this.ids = ids;
        }

        public List<URLBean> parseListURLBean(){
            List<URLBean> result = new ArrayList<URLBean>();
            if(ids!=null&&!ids.isEmpty()){
                for (int i = 0; i<ids.size(); i++){
                    URLBean urlBean = new URLBean();
                    urlBean.setDescription(names.get(i));
                    urlBean.setUrl(Constants.SITEROOT + extractUrl(links.get(i)));
                    result.add(urlBean);
                }
            }
            return result;
        }

        public URLBean parseURLBean(){
            URLBean urlBean = new URLBean();
            if(ids!=null&&!ids.isEmpty()){
                urlBean.setDescription(names.get(0));
                urlBean.setUrl(Constants.SITEROOT + extractUrl(links.get(0)));
            }
            return urlBean;
        }

        private String extractUrl(String link){
            String result = null;
            if(link!=null){
                int idx = link.indexOf("\"") + 1;
                int idxEnd = link.indexOf("\"", idx);
                result = link.substring(idx, idxEnd);
            }else{
                result = "";
            }
            return  result;
        }
    }

    public long getContent_id() {
        return content_id;
    }

    public void setContent_id(long content_id) {
        this.content_id = content_id;
    }

    public boolean isContent_active() {
        return content_active;
    }

    public void setContent_active(boolean content_active) {
        this.content_active = content_active;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClean_name() {
        return clean_name;
    }

    public void setClean_name(String clean_name) {
        this.clean_name = clean_name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getFilesize() {
        return filesize;
    }

    public void setFilesize(long filesize) {
        this.filesize = filesize;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getFavorites() {
        return favorites;
    }

    public void setFavorites(int favorites) {
        this.favorites = favorites;
    }

    public int getComments() {
        return comments;
    }

    public void setComments(int comments) {
        this.comments = comments;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getSample() {
        return sample;
    }

    public void setSample(String sample) {
        this.sample = sample;
    }

    public String getThumbnails() {
        return thumbnails;
    }

    public void setThumbnails(String thumbnails) {
        this.thumbnails = thumbnails;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUser_displayname() {
        return user_displayname;
    }

    public void setUser_displayname(String user_displayname) {
        this.user_displayname = user_displayname;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public ArrayLink getArtists() {
        return artists;
    }

    public void setArtists(ArrayLink artists) {
        this.artists = artists;
    }

    public ArrayLink getSeries() {
        return series;
    }

    public void setSeries(ArrayLink series) {
        this.series = series;
    }

    public ArrayLink getTags() {
        return tags;
    }

    public void setTags(ArrayLink tags) {
        this.tags = tags;
    }

    public ArrayLink getTranslators() {
        return translators;
    }

    public void setTranslators(ArrayLink translators) {
        this.translators = translators;
    }

    public URLBean parseLanguage(){
        URLBean result = new URLBean();
        result.setDescription(getLanguage());
        if(getUrl().startsWith(Constants.SITEMANGA))
            result.setUrl(Constants.SITEMANGA + "/" + getLanguage());
        else
            result.setUrl(Constants.SITEDOUJINSHI + "/" +getLanguage());

        return result;
    }

    public URLBean parseUploader(){
        URLBean result = new URLBean();
        result.setDescription(getUser_displayname());
        return result;
    }

    public String imageServer(){
        return Constants.IMAGESERVER + getDirectory() + "images/";
    }
}
