package com.fakkudroid.json;

import java.io.Serializable;

/**
 * Created by neko on 07/09/2014.
 */
public class ContentImages implements Serializable{

    private String cover;
    private String sample;

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
}
