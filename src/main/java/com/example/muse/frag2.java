package com.example.muse;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class frag2 extends Fragment {
    public ArrayList<Song_Net> song_nets;
    private View view;
    public static String text;
    private String songlist_url = "http://139.196.76.67:3000/top/playlist?limit=20";
    private final String r_type = "POST";
    private static String result;
    private static String[][] listinfo;

    public View onCreateView(final LayoutInflater inflater1, ViewGroup container, Bundle savedInstanceState) {
        view = inflater1.inflate(R.layout.frag2_layout, null);
        ListView listView= view.findViewById(R.id.lv);
        String ori_content = connect(songlist_url);
        try {
            listinfo= parseJson_songlist(ori_content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Button btn_s = view.findViewById(R.id.btn_search);
        EditText search = view.findViewById(R.id.item_search);
        MyBaseAdapter myBaseAdapter = new MyBaseAdapter();
        listView.setAdapter(myBaseAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent=new Intent(frag2.this.getContext(),NetList_Activity.class);
                intent.putExtra("id",listinfo[i][1]);
                MainActivity.p = 1;
                MainActivity.playmode=true;
                MainActivity.musicControl.pausePlay();
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
                MainActivity.musicControl.pausePlay();
                Intent i = new Intent(frag2.this.getContext(),NetList_Activity.class);
                i.putExtra("keyword",text);
                startActivity(i);

            }
        });
        return view;
    }

    public static String connect(String songlist_url){
        HttpURLConnection con;
        BufferedReader buffer;
        StringBuffer resultBuffer;
        Log.e("runnable","已开启线程");
        try {
            URL url = new URL(songlist_url);
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


    public static String[][] parseJson_songlist(String ori_content) throws Exception {
        String[][] listinfo = new String[20][4];
        JSONObject ori_json = new JSONObject(ori_content);
        JSONArray playlists = ori_json.getJSONArray("playlists");
        for(int k= 0;k<20;k++){
            JSONObject realdata = playlists.getJSONObject(k);
            listinfo[k][0] = realdata.getString("name");
            listinfo[k][1] = realdata.getString("id");
            listinfo[k][2] = realdata.getString("coverImgUrl");
            JSONObject creator = realdata.getJSONObject("creator");
            listinfo[k][3] = creator.getString("nickname");
        }
        return listinfo;
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

    class MyBaseAdapter extends BaseAdapter{
        @Override
        public int getCount(){return listinfo.length;}
        @Override
        public Object getItem(int i){return listinfo[i][0];}
        @Override
        public long getItemId(int i){return i;}

        @Override
        public View getView(int i ,View convertView, ViewGroup parent) {

            View view=View.inflate(frag2.this.getContext(),R.layout.item_song,null);
            ImageView iv_cover=view.findViewById(R.id.cover);
            TextView tv_songs=view.findViewById(R.id.song_name);
            TextView tv_singer=view.findViewById(R.id.singer);


            try {
                iv_cover.setImageBitmap(getCover(listinfo[i][2]));
            } catch (Exception e) {
                e.printStackTrace();
            };
            tv_songs.setText(listinfo[i][0]);
            tv_singer.setText(listinfo[i][3]);
            return view;
        }
    }
}
