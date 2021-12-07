package com.example.muse;

import android.graphics.Bitmap;

public class Song_Net {
    private String title;
    private String singer;
    private String id;
    private Bitmap cover;
    private String url;
    private int duration;


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public Bitmap getCover() {
        return cover;
    }

    public void setCover(Bitmap cover) {
        this.cover = cover;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getDuration() {
        return duration;
    }

    public void setDurationl(int duration) {
        this.duration = duration;
    }

    public Song_Net() {
        super();
    }



    public Song_Net(String title,  String singer, Bitmap cover, String id, String url, int duration) {
        super();
        this.title = title;
        this.singer = singer;
        this.cover = cover;
        this.id = id;
        this.url = url;
        this.duration = duration;
    }
}
