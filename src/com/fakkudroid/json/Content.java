package com.fakkudroid.json;

import java.io.Serializable;
import java.util.List;

/**
 * Created by neko on 07/09/2014.
 */
public class Content implements Serializable{

    private String content_name;
    private String content_url;
    private String content_description;
    private String content_language;
    private String content_category;
    private long content_date;
    private long content_filesize;
    private int content_favorites;
    private int content_comments;
    private int content_pages;
    private String content_poster;
    private String content_poster_url;
    private List<Attribute> content_tags;
    private List<Attribute> content_translators;
    private List<Attribute> content_series;
    private List<Attribute> content_artists;
    private ContentImages content_images;

    public String getContent_name() {
        return content_name;
    }

    public void setContent_name(String content_name) {
        this.content_name = content_name;
    }

    public String getContent_url() {
        return content_url;
    }

    public void setContent_url(String content_url) {
        this.content_url = content_url;
    }

    public String getContent_description() {
        return content_description;
    }

    public void setContent_description(String content_description) {
        this.content_description = content_description;
    }

    public String getContent_language() {
        return content_language;
    }

    public void setContent_language(String content_language) {
        this.content_language = content_language;
    }

    public String getContent_category() {
        return content_category;
    }

    public void setContent_category(String content_category) {
        this.content_category = content_category;
    }

    public long getContent_date() {
        return content_date;
    }

    public void setContent_date(long content_date) {
        this.content_date = content_date;
    }

    public long getContent_filesize() {
        return content_filesize;
    }

    public void setContent_filesize(long content_filesize) {
        this.content_filesize = content_filesize;
    }

    public int getContent_favorites() {
        return content_favorites;
    }

    public void setContent_favorites(int content_favorites) {
        this.content_favorites = content_favorites;
    }

    public int getContent_comments() {
        return content_comments;
    }

    public void setContent_comments(int content_comments) {
        this.content_comments = content_comments;
    }

    public int getContent_pages() {
        return content_pages;
    }

    public void setContent_pages(int content_pages) {
        this.content_pages = content_pages;
    }

    public String getContent_poster() {
        return content_poster;
    }

    public void setContent_poster(String content_poster) {
        this.content_poster = content_poster;
    }

    public String getContent_poster_url() {
        return content_poster_url;
    }

    public void setContent_poster_url(String content_poster_url) {
        this.content_poster_url = content_poster_url;
    }

    public List<Attribute> getContent_tags() {
        return content_tags;
    }

    public void setContent_tags(List<Attribute> content_tags) {
        this.content_tags = content_tags;
    }

    public List<Attribute> getContent_translators() {
        return content_translators;
    }

    public void setContent_translators(List<Attribute> content_translators) {
        this.content_translators = content_translators;
    }

    public List<Attribute> getContent_series() {
        return content_series;
    }

    public void setContent_series(List<Attribute> content_series) {
        this.content_series = content_series;
    }

    public List<Attribute> getContent_artists() {
        return content_artists;
    }

    public void setContent_artists(List<Attribute> content_artists) {
        this.content_artists = content_artists;
    }

    public ContentImages getContent_images() {
        return content_images;
    }

    public void setContent_images(ContentImages content_images) {
        this.content_images = content_images;
    }

    public boolean isContent(){
        return getContent_name()!=null;
    }
}
