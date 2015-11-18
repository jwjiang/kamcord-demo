package com.example.jwjiang.kamcordwatchview;

import android.graphics.Bitmap;

import java.util.HashMap;

/**
 * Created by jwjiang on 11/11/15.
 */
public class BitmapCache {

    private static BitmapCache bInstance = null;
    private static HashMap<String, Bitmap> cache;

    private BitmapCache(){}

    // singleton cache to save thumbnail URLs and their corresponding bitmaps to prevent redownloading
    // speeds up refreshes a lot
    public static BitmapCache getInstance() {
        if (bInstance == null) {
            bInstance = new BitmapCache();
            cache = new HashMap<String, Bitmap>();
        }
        return bInstance;
    }

    public Bitmap getBitmap(String name) {
        return cache.get(name);
    }

    public void putBitmap(String name, Bitmap bitmap) {
        cache.put(name, bitmap);
    }

}
