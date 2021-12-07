package com.example.muse;

import android.graphics.Bitmap;

public class List_Net {
    private String title;
    private String creator;
    private Bitmap cover;
    private String id;


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Bitmap getCover() {
        return cover;
    }

    public void setCover(Bitmap cover) {
        this.cover = cover;
    }

    public String getId() { return id; }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreator() { return creator; }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public List_Net() {
        super();
    }

    public List_Net( String title, Bitmap cover, String creator,String id) {
        super();
        this.title = title;
        this.cover = cover;
        this.creator = creator;
        this.id = id;

    }
}
