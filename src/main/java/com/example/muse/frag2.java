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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class frag2 extends Fragment {
    public ArrayList<List_Net> list_nets;
    private static View view;
    public static String text;
    private static ListView listView;
    private static TextView progress,total,loading;
    private final String songlist_url = "http://139.196.76.67:3000/top/playlist?limit=30";
    private static String result;
    private static SharedPreferences savedcontent;

    private Handler f2handler = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(@NonNull Message msg){
            super.handleMessage(msg);
            if(msg.what==0){
                list_nets = (ArrayList<List_Net>) msg.obj;
                ImageButton btn_s = view.findViewById(R.id.btn_search);
                ImageButton btn_f = view.findViewById(R.id.btn_fresh);
                EditText search = view.findViewById(R.id.item_search);
                MyBaseAdapter myBaseAdapter = new MyBaseAdapter();
                listView= view.findViewById(R.id.lv);
                listView.setAdapter(myBaseAdapter);
                loading.setText("");
                progress.setText("");
                total.setText("");
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Intent intent=new Intent(frag2.listView.getContext(),NetList_Activity.class);
                        intent.putExtra("id",list_nets.get(i).getId());
                        MainActivity.p = 1;
                        startActivity(intent);
                    }
                } );
                btn_s.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        MainActivity.playmode=true;
                        MainActivity.p=1;
                        text = search.getText().toString();
                        MainActivity.netid.setText(text);
                        Intent i = new Intent(frag2.this.getContext(),NetList_Activity.class);
                        i.putExtra("keyword",text);
                        startActivity(i);

                    }
                });
                btn_f.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                String ori_content = connect(songlist_url);
                                try {
                                    list_nets = new ArrayList<>();
                                    list_nets=List_NetInfo.getAllLists(ori_content);
                                    Message msg = new Message();
                                    msg.what=0;
                                    msg.obj = list_nets;
                                    f2handler.sendMessage(msg);
                                    Log.e("position","here");
                                    Log.e("listinfo_mainthread", String.valueOf(list_nets));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                });
                Log.e("handler","接收到消息");
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

    public static Handler sharedhandler = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(@NonNull Message msg){
            super.handleMessage(msg);
            if(msg.what==1){
                String content = (String) msg.obj;
                savedcontent.edit().putString("savedcontent",content).commit();
            }
        }
    };

    public View onCreateView(final LayoutInflater inflater1, ViewGroup container, Bundle savedInstanceState) {
        view = inflater1.inflate(R.layout.frag2_layout, null);
        progress = view.findViewById(R.id.progress);
        total = view.findViewById(R.id.total);
        loading = view.findViewById(R.id.loading);
        savedcontent= getActivity().getSharedPreferences("file", Context.MODE_PRIVATE);
        if(savedcontent.contains("savedcontent")){
            String ori_content = savedcontent.getString("savedcontent",null);
            Log.e("调用",ori_content);
            list_nets = new ArrayList<>();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        list_nets=List_NetInfo.getAllLists_saved(ori_content);
                        Message msg = new Message();
                        msg.what=0;
                        msg.obj = list_nets;
                        f2handler.sendMessage(msg);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }else{
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String ori_content = connect(songlist_url);
                    try {
                        list_nets = new ArrayList<>();
                        list_nets=List_NetInfo.getAllLists(ori_content);
                        Message msg = new Message();
                        msg.what=0;
                        msg.obj = list_nets;
                        f2handler.sendMessage(msg);
                        Log.e("position","here");
                        Log.e("listinfo_mainthread", String.valueOf(list_nets));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }


        return view;
    }

    public static String connect(String songlist_url){
        HttpURLConnection con;
        StringBuffer resultBuffer;
        try {
            URL url = new URL(songlist_url);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            int responseCode = con.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK){
                InputStream inputStream = con.getInputStream();
                resultBuffer = new StringBuffer();
                byte[] bytes=new byte[1024];
                int len=0;
                while ((len=inputStream.read(bytes))!=-1){
                    resultBuffer.append(new String(bytes,0,len));
                }
                result= resultBuffer.toString();//获得原始字符串
            }
        }catch(Exception e) {
            e.printStackTrace();
        }return result;

    }

    class MyBaseAdapter extends BaseAdapter{
        @Override
        public int getCount(){return list_nets.size();}
        @Override
        public Object getItem(int i){return list_nets.get(i).getTitle();}
        @Override
        public long getItemId(int i){return i;}

        @Override
        public View getView(int i ,View convertView, ViewGroup parent) {

            View view=View.inflate(frag2.this.getContext(),R.layout.item_song,null);
            ImageView iv_cover=view.findViewById(R.id.cover);
            TextView tv_songs=view.findViewById(R.id.song_name);
            TextView tv_singer=view.findViewById(R.id.singer);

            iv_cover.setImageBitmap(list_nets.get(i).getCover());
            tv_songs.setText(list_nets.get(i).getTitle());
            tv_singer.setText(list_nets.get(i).getCreator());
            return view;
        }
    }
}
