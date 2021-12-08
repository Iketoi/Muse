package com.example.muse;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

public class List_NetInfo {
    public static ArrayList<List_Net> getAllLists(String ori_content) throws Exception {
        ArrayList<List_Net> list_nets = new ArrayList<>();
        JSONObject ori_json = new JSONObject(ori_content);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message msg = Message.obtain();
                msg.what=1;
                msg.obj = ori_content;
                frag2.sharedhandler.sendMessage(msg);
            }
        }).start();
        JSONArray playlists = ori_json.getJSONArray("playlists");
        for(int k= 0;k<playlists.length();k++){
            int progress = k;
            int total = playlists.length();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Message msg = Message.obtain();
                    msg.what = 0;
                    Bundle bundle = new Bundle();
                    bundle.putInt("progress",progress);
                    bundle.putInt("total",total);
                    Log.e("progress",String.valueOf(progress));
                    Log.e("total",String.valueOf(total));
                    msg.obj=bundle;
                    frag2.progresshandler.sendMessage(msg);
                }
            }).start();
            List_Net list_net= new List_Net();
            JSONObject realdata = playlists.getJSONObject(k);
            list_net.setTitle(realdata.getString("name"));
            list_net.setId(realdata.getString("id"));
            JSONObject creator = realdata.getJSONObject("creator");
            list_net.setCreator(creator.getString("nickname"));
            list_net.setCover(getCover(realdata.getString("coverImgUrl")));
            list_nets.add(list_net);
        }

        return list_nets;

    }

    public static ArrayList<List_Net> getAllLists_saved(String ori_content) throws Exception {
        ArrayList<List_Net> list_nets = new ArrayList<>();
        JSONObject ori_json = new JSONObject(ori_content);
        JSONArray playlists = ori_json.getJSONArray("playlists");
        for(int k= 0;k<playlists.length();k++){
            List_Net list_net= new List_Net();
            JSONObject realdata = playlists.getJSONObject(k);
            list_net.setTitle(realdata.getString("name"));
            list_net.setId(realdata.getString("id"));
            JSONObject creator = realdata.getJSONObject("creator");
            list_net.setCreator(creator.getString("nickname"));
            list_net.setCover(getCover(realdata.getString("coverImgUrl")));
            list_nets.add(list_net);
        }

        return list_nets;

    }
    public static Bitmap getCover(String picurl) throws Exception{
        URL url = new URL(picurl);
        Bitmap cover=null;
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setConnectTimeout(5000);
        con.setRequestMethod("GET");
        if (con.getResponseCode() == 200) {
            InputStream inputStream = con.getInputStream();
            cover = BitmapFactory.decodeStream(inputStream);
        }
        return cover;
    }
}
