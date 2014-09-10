package com.fakkudroid.bean;

import java.util.LinkedList;

/**
 * Created by neko on 09/09/2014.
 */
public class DoujinListBean {

    private LinkedList<DoujinBean> lstDoujin;
    private int pages;

    public LinkedList<DoujinBean> getLstDoujin() {
        return lstDoujin;
    }

    public void setLstDoujin(LinkedList<DoujinBean> lstDoujin) {
        this.lstDoujin = lstDoujin;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }
}
