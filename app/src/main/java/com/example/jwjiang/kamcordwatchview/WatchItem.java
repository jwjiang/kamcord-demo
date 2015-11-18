package com.example.jwjiang.kamcordwatchview;

/**
 * Created by jwjiang on 11/11/15.
 */
public class WatchItem {

    private String video_url;
    private String thumbnail_url;
    private String title;

    // model object for an item in the watch view
    public WatchItem(String vurl, String turl, String title) {
        video_url = vurl;
        thumbnail_url = turl;
        this.title = title;
    }

    public String getVidUrl() {
        return video_url;
    }

    public String getThumbUrl() {
        return thumbnail_url;
    }

    public String getTitle() {
        return title;
    }

}
