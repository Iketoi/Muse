package com.example.muse;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class NetList_Activity extends AppCompatActivity{
    private static String id,keyword;
    private static String listurl = "http://139.196.76.67:3000/playlist/track/all?id=";
    private static String result;
    private static String searchurl="http://139.196.76.67:3000/search?keywords=";
    private static Bitmap cover;
    private static ListView listview;
    private static TextView loading,total,progress;
    public static ArrayList<Song_Net> song_nets;

    private Handler Nlhandler = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(@NonNull Message msg){
            super.handleMessage(msg);
            if(msg.what==0){
                song_nets = (ArrayList<Song_Net>) msg.obj;
                listview= findViewById(R.id.lv);
                listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Intent intent=new Intent(NetList_Activity.this.getApplicationContext(),NetMusic_Activity.class);
                        intent.putExtra("id",song_nets.get(i).getId());
                        intent.putExtra("position",String.valueOf(i));
                        MainActivity.netid.setText(song_nets.get(i).getId());
                        if(MainActivity.conn_net!=null){
//                            MainActivity.musicControl.pausePlay();
                        }
                        MainActivity.count.setText(String.valueOf(i));
                        if(!MainActivity.playmode){
                            MainActivity.playmode=true;
                            Log.e("playmode",String.valueOf(MainActivity.playmode));
                        }
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Message msg = new Message();
                                msg.what=1;
                                msg.obj = song_nets;
                                NetMusicService.Nmshandler.sendMessage(msg);
                            }
                        }).start();
                        startActivity(intent);
                    }
                } );
                Log.e("handler","接收到消息");
            }
            if(msg.what==1){
                loading = findViewById(R.id.loading);
                song_nets = (ArrayList<Song_Net>) msg.obj;
                listview= findViewById(R.id.lv);
                listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Intent intent=new Intent(NetList_Activity.this.getApplicationContext(),NetMusic_Activity.class);
                        intent.putExtra("id",song_nets.get(i).getId());
                        intent.putExtra("position",String.valueOf(i));
                        MainActivity.netid.setText(song_nets.get(i).getId());
                        if(MainActivity.conn_net!=null){
//                            MainActivity.musicControl.pausePlay();
                        }
                        MainActivity.count.setText(String.valueOf(i));
                        if(!MainActivity.playmode){
                            MainActivity.playmode=true;
                            Log.e("playmode",String.valueOf(MainActivity.playmode));
                        }

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Message msg = new Message();
                                msg.what=1;
                                msg.obj = song_nets;
                                NetMusicService.Nmshandler.sendMessage(msg);
                            }
                        }).start();
                        startActivity(intent);
                    }
                } );
            }
        }
    };

    public static Handler imageviewhandler = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(@NonNull Message msg){
            super.handleMessage(msg);
            if(msg.what==1){
                cover = (Bitmap) msg.obj;
                Bundle bundle = msg.getData();
                int pro = bundle.getInt("progress");
                song_nets.get(pro).setCover(cover);
//                MyBaseAdapter_iv myBaseAdapter_iv = new MyBaseAdapter_iv();
                MyBaseAdapter myBaseAdapter = new MyBaseAdapter();
                listview.setAdapter(myBaseAdapter);
                loading.setText("");
                total.setText("");
                progress.setText("");
//                listview.setAdapter(myBaseAdapter_iv);
            }
        }
    };

    public static Handler progresshandler = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(@NonNull Message msg){
            super.handleMessage(msg);
            if(msg.what==0){
                Bundle bundle = (Bundle) msg.obj;
                progress.setText(String.valueOf(bundle.getInt("progress")+1));
                total.setText(String.valueOf(bundle.getInt("total")));
                int pro = bundle.getInt("progress")+1;
                if(pro ==bundle.getInt("total")){
                    loading.setText("加 载 完 毕");
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_netlist);
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void init() throws Exception{
        Intent intent = getIntent();
        total = findViewById(R.id.total);
        progress = findViewById(R.id.progress);
        loading = findViewById(R.id.loading);
        if(intent.hasExtra("id")){
            id = intent.getStringExtra("id");
            listurl = listurl+id+"&limit=50";
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String ori_content = connect(listurl);
                    listurl = "http://139.196.76.67:3000/playlist/track/all?id=";
                    try {
                        song_nets = new ArrayList<>();
                        song_nets=Song_NetInfo.getAllsongs_click(ori_content);
                        Message msg = new Message();
                        msg.what=0;
                        msg.obj = song_nets;
                        Nlhandler.sendMessage(msg);
                        Log.e("position","here");
                        Log.e("listinfo_mainthread", String.valueOf(song_nets));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        if(intent.hasExtra("keyword")){
            keyword = intent.getStringExtra("keyword");
            searchurl = searchurl+keyword+"&limit=5";
            Log.e("searchurl",searchurl);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String ori_content = connect(searchurl);
                    searchurl="http://139.196.76.67:3000/search?keywords=";
                    try {
                        song_nets = new ArrayList<>();
                        song_nets=Song_NetInfo.getAllsongs_search(ori_content);
                        Message msg = new Message();
                        msg.what=1;
                        msg.obj = song_nets;
                        Nlhandler.sendMessage(msg);
                        Log.e("position","here");
                        Log.e("listinfo_mainthread", String.valueOf(song_nets));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

    }

    public static String connect(String list_url){
        HttpURLConnection con;
        BufferedReader buffer;
        StringBuffer resultBuffer;
        Log.e("runnable","已开启线程");
        try {
            URL url = new URL(list_url);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
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
                Log.e("netease",resultBuffer.toString());
                result= resultBuffer.toString();
            }
        }catch(Exception e) {
            e.printStackTrace();
        }return result;

    }

    public static class MyBaseAdapter extends BaseAdapter {
        @Override
        public int getCount(){return song_nets.size();}
        @Override
        public Object getItem(int i){return song_nets.get(i).getTitle();}
        @Override
        public long getItemId(int i){return i;}

        @Override
        public View getView(int i ,View convertView, ViewGroup parent) {

            View view=View.inflate(NetList_Activity.listview.getContext(),R.layout.item_song,null);
            ImageView iv_cover=view.findViewById(R.id.cover);
            TextView tv_songs=view.findViewById(R.id.song_name);
            TextView tv_singer=view.findViewById(R.id.singer);


            iv_cover.setImageBitmap(song_nets.get(i).getCover());
            tv_songs.setText(song_nets.get(i).getTitle());
            tv_singer.setText(song_nets.get(i).getSinger());
            return view;
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }
}
