package com.fakkudroid.json;

import java.io.Serializable;

/**
 * Created by neko on 07/09/2014.
 */
public class Attribute implements Serializable{

    private String attribute;
    private String attribute_link;

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public String getAttribute_link() {
        return attribute_link;
    }

    public void setAttribute_link(String attribute_link) {
        this.attribute_link = attribute_link;
    }
}
