package com.example.muse;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class frag1 extends Fragment {
    public static ArrayList<Song> songList;
    private View view;

    private Handler f1handler = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(@NonNull Message msg){
            super.handleMessage(msg);
            if(msg.what==1){
                songList = (ArrayList<Song>) msg.obj;
                ListView listView= view.findViewById(R.id.lv);
                MyBaseAdapter myBaseAdapter = new MyBaseAdapter();
                listView.setAdapter(myBaseAdapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Intent intent=new Intent(frag1.this.getContext(),Music_Activity.class);
                        intent.putExtra("position",String.valueOf(i));
                        Log.e("发送",String.valueOf(i));
                        MainActivity.p = 1;
                        if(MainActivity.playmode){
                            MainActivity.playmode=false;
                            Log.e("playmode",String.valueOf(MainActivity.playmode));
                        }
                        if(MainActivity.conn_net!=null){
                            MainActivity.musicControl_net.unbind();
                        }
                        startActivity(intent);

                    }
                } );
                Log.e("handler","接收到消息");
            }
        }
    };//处理子线程中的数据
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        view = inflater.inflate(R.layout.music_list, null);
//
        new Thread(new Runnable() {
            @Override
            public void run() {
                songList = new ArrayList<>();
                try {
                    songList=SongInfo.getAllSongs(frag1.this.getActivity().getApplicationContext());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Message msg = new Message();
                msg.what = 1;
                msg.obj = songList;
                f1handler.sendMessage(msg);
            }
        }).start();
        return view;
    }

    class MyBaseAdapter extends BaseAdapter {
        @Override
        public int getCount(){return  songList.size();}
        @Override
        public Object getItem(int i){return songList.get(i).getFileName();}
        @Override
        public long getItemId(int i){return i;}

        @Override
        public View getView(int i ,View convertView, ViewGroup parent) {


            View view=View.inflate(frag1.this.getContext(),R.layout.item_song,null);
            ImageView iv_cover=view.findViewById(R.id.cover);
            TextView tv_songs=view.findViewById(R.id.song_name);
            TextView tv_singer=view.findViewById(R.id.singer);

            iv_cover.setImageBitmap(songList.get(i).getCover());
            tv_songs.setText(songList.get(i).getTitle());
            tv_singer.setText(songList.get(i).getSinger());

            return view;
        }
    }//继承适配器渲染列表

}

