package com.fakkudroid.json;

import java.io.Serializable;
import java.util.List;

/**
 * Created by neko on 07/09/2014.
 */
public class JSONListResult implements Serializable{

    private List<Content> content;
    private List<Content> index;
    private List<Content> related;
    private int total;
    private int page;
    private int pages;

    public List<Content> getRelated() {
        return related;
    }

    public void setRelated(List<Content> related) {
        this.related = related;
    }

    public List<Content> getIndex() {
        return index;
    }

    public void setIndex(List<Content> index) {
        this.index = index;
    }

    public List<Content> getContent() {
        return content;
    }

    public void setContent(List<Content> content) {
        this.content = content;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }
}
