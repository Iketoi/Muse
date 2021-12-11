package com.example.muse;

import android.graphics.Bitmap;

public class Hitokoto {
    private String hitokoto;
    private String type;
    private String origin;
    private String creator;
    private String id;


    public String getHitokoto() {
        return hitokoto;
    }

    public void setHitokoto(String hitokoto) {
        this.hitokoto = hitokoto;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Hitokoto() {
        super();
    }



    public Hitokoto(String hitokoto,  String type, String id, String origin, String creator) {
        super();
        this.hitokoto = hitokoto;
        this.type = type;
        this.id = id;
        this.origin = origin;
        this.creator = creator;
    }
}
