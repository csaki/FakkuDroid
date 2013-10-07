package com.fakkudroid.component;

import android.graphics.Bitmap;

import java.util.LinkedList;

/**
 * Created by neko on 28/09/13.
 */
public class ImageStore {

    private static LinkedList<Bitmap> qBitmap;
    private static final int MAX_STORED = 10;

    public static void reset(){
        qBitmap = new LinkedList<Bitmap>();
    }

    public static void add(Bitmap b){
        if(qBitmap.size()>MAX_STORED){
            qBitmap.removeFirst().recycle();
            System.gc();
        }
        qBitmap.add(b);
    }
}
