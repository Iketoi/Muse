package com.example.muse;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class Song_NetInfo {
    private static String myurl = "http://139.196.76.67:3000/song/url?id=";
    private static String infourl = "http://139.196.76.67:3000/song/detail?ids=";
    private static String result;
    public static ArrayList<Song_Net> getAllsongs_click(String ori_content) throws Exception {
        ArrayList<Song_Net> song_nets = new ArrayList<>();
        JSONObject ori_json = new JSONObject(ori_content);
        JSONArray playlists = ori_json.getJSONArray("songs");
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
                    NetList_Activity.progresshandler.sendMessage(msg);
                }
            }).start();
            Song_Net song_net= new Song_Net();
            JSONObject realdata = playlists.getJSONObject(k);
            song_net.setTitle(realdata.getString("name"));
            song_net.setId(realdata.getString("id"));
            String music = connect(myurl+realdata.getString("id"),"POST");
            song_net.setUrl(parseJson_songurl(music));
            song_net.setDurationl(Integer.parseInt(realdata.getString("dt")));
            JSONArray art = realdata.getJSONArray("ar");
            JSONObject arti = art.getJSONObject(0);
            song_net.setSinger(arti.getString("name"));
            JSONObject all = realdata.getJSONObject("al");
            song_net.setCover(getCover(all.getString("picUrl")));
            song_nets.add(song_net);
        }
        return song_nets;

    }
    public static ArrayList<Song_Net> getAllsongs_search(String ori_content) throws Exception {
        ArrayList<Song_Net> song_nets = new ArrayList<>();
        JSONObject ori_json = new JSONObject(ori_content);
        JSONObject lists = ori_json.getJSONObject("result");
        JSONArray playlists = lists.getJSONArray("songs");
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
                    NetList_Activity.progresshandler.sendMessage(msg);
                }
            }).start();
            Song_Net song_net= new Song_Net();
            JSONObject realdata = playlists.getJSONObject(k);
            song_net.setTitle(realdata.getString("name"));
            song_net.setId(realdata.getString("id"));
            song_net.setDurationl(Integer.parseInt(realdata.getString("duration")));
            String music = connect(myurl+realdata.getString("id"),"POST");
            song_net.setUrl(parseJson_songurl(music));
            JSONArray art = realdata.getJSONArray("artists");
            JSONObject arti = art.getJSONObject(0);
            song_net.setSinger(arti.getString("name"));
            song_net.setCover(getCover_search(realdata.getString("id")));
            song_nets.add(song_net);
        }
        return song_nets;

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

    public static String parseJson_songurl(String ori_content) throws Exception {
        JSONObject ori_json = new JSONObject(ori_content);
        JSONArray data = ori_json.getJSONArray("data");
        JSONObject realdata = data.getJSONObject(0);
        String url = realdata.getString("url");
        return url;
    }

    public static String connect(String myurl, String r_type){
        HttpURLConnection con;
        BufferedReader buffer;
        StringBuffer resultBuffer;
        Log.e("runnable","已开启线程");
        try {
            URL url = new URL(myurl);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod(r_type);
            con.setDoOutput(true);
            int responseCode = con.getResponseCode();
            Log.e("respondcode",String.valueOf(responseCode));

            if(responseCode == HttpURLConnection.HTTP_OK){
                InputStream inputStream = con.getInputStream();
                resultBuffer = new StringBuffer();
                byte[] bytes=new byte[1024];
                int len=0;
                while ((len=inputStream.read(bytes))!=-1){
                    resultBuffer.append(new String(bytes,0,len));
                }
                result= resultBuffer.toString();
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static Bitmap getCover_search(String id) throws Exception {
        Bitmap cover=null;
        String ori_content = connect(infourl+id,"POST");
        JSONObject ori_json = new JSONObject(ori_content);
        JSONArray data = ori_json.getJSONArray("songs");
        JSONObject realdata = data.getJSONObject(0);
        JSONObject all = realdata.getJSONObject("al");
        URL url = new URL(all.getString("picUrl"));
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setConnectTimeout(5000);
        con.setRequestMethod("GET");
        if (con.getResponseCode() == 200) {
            InputStream inputStream = con.getInputStream();
            cover=BitmapFactory.decodeStream(inputStream);
        }
        return cover;
    }
}
