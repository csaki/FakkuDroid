package com.fakkudroid.util;

/**
 * Created by neko on 21/06/2014.
 */
public enum ImageQuality {
    LOW(50,70),MEDIUM(100,140),HIGH(200,280);

    int width;
    int height;

    ImageQuality(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
