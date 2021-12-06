package com.example.muse;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetList_Activity extends AppCompatActivity{
    private static String id,keyword,musicurl;
    private static String listurl = "http://139.196.76.67:3000/playlist/track/all?id=";
    private static String result;
    private static String searchurl="http://139.196.76.67:3000/search?keywords=";
    private static String myurl = "http://139.196.76.67:3000/song/detail?ids=";
    private static String[][] songlist;
    private static ListView listview;


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
        listview= findViewById(R.id.lv);
        Intent intent = getIntent();
        if(intent.hasExtra("id")){
            id = intent.getStringExtra("id");
            listurl = listurl+id;
            String ori_content = connect(listurl);
            songlist = parseJson_songlist_click(ori_content);
            MyBaseAdapter myBaseAdapter = new MyBaseAdapter();
            listview.setAdapter(myBaseAdapter);
            listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Intent intent=new Intent(NetList_Activity.this.getApplicationContext(),NetMusic_Activity.class);
                    intent.putExtra("id",songlist[i][1]);
                    MainActivity.netid.setText(id);
                    startActivity(intent);
                }
            } );
        }if(intent.hasExtra("keyword")){
            keyword = intent.getStringExtra("keyword");
            searchurl = searchurl+keyword;
            Log.e("searchurl",searchurl);
            String ori_content = connect(searchurl);
            songlist = parseJson_songlist_search(ori_content);
            searchurl="http://139.196.76.67:3000/search?keywords=";
            MyBaseAdapter myBaseAdapter = new MyBaseAdapter();
            listview.setAdapter(myBaseAdapter);
            listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Intent intent=new Intent(NetList_Activity.this.getApplicationContext(),NetMusic_Activity.class);
                    intent.putExtra("id",songlist[i][1]);
                    MainActivity.netid.setText(songlist[i][1]);
                    MainActivity.musicControl_net.pausePlay();
                    startActivity(intent);
                }
            } );
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

    public static String[][] parseJson_songlist_click(String ori_content) throws Exception {
        JSONObject ori_json = new JSONObject(ori_content);
        JSONArray playlists = ori_json.getJSONArray("songs");
        String[][] list_song = new String[playlists.length()][4];
        for(int k= 0;k<playlists.length();k++){
            JSONObject realdata = playlists.getJSONObject(k);
            list_song[k][0] = realdata.getString("name");
            list_song[k][1] = realdata.getString("id");
            JSONObject all = realdata.getJSONObject("al");
            list_song[k][2] = all.getString("picUrl");
            JSONArray art = realdata.getJSONArray("ar");
            JSONObject arti = art.getJSONObject(0);
            list_song[k][3] = arti.getString("name");//歌手名称
        }
        return list_song;
    }


    public static String[][] parseJson_songlist_search(String ori_content) throws Exception {
        JSONObject ori_json = new JSONObject(ori_content);
        JSONObject lists = ori_json.getJSONObject("result");
        JSONArray playlists = lists.getJSONArray("songs");
        String[][] list_song = new String[playlists.length()][4];
        for(int k= 0;k<playlists.length();k++){
            JSONObject realdata = playlists.getJSONObject(k);
            list_song[k][0] = realdata.getString("name");
            list_song[k][1] = realdata.getString("id");
            JSONArray art = realdata.getJSONArray("artists");
            JSONObject arti = art.getJSONObject(0);
            list_song[k][2] = arti.getString("img1v1Url");
//            String id = realdata.getString("id");
//            myurl = myurl+id;
//            String music = connect(myurl);
//            Log.e("parsejsonmusic",music);
//            JSONObject ori_json_pic = new JSONObject(music);
//            if(ori_json_pic.getJSONArray("songs")!=null){
//                JSONArray data = ori_json_pic.getJSONArray("songs");
//                JSONObject realdata_pic = data.getJSONObject(0);
//                JSONObject all = realdata_pic.getJSONObject("al");
//                list_song[k][2]= all.getString("picUrl");
//            }else{
//                list_song[k][2]= null;
//            }
            list_song[k][3] = arti.getString("name");//歌手名称
        }
        return list_song;
    }

    class MyBaseAdapter extends BaseAdapter {
        @Override
        public int getCount(){Log.e("lengthhhhhh",String.valueOf(songlist.length));return songlist.length;}
        @Override
        public Object getItem(int i){return songlist[i][0];}
        @Override
        public long getItemId(int i){return i;}

        @Override
        public View getView(int i ,View convertView, ViewGroup parent) {

            View view=View.inflate(NetList_Activity.this.getApplicationContext(),R.layout.item_song,null);
            ImageView iv_cover=view.findViewById(R.id.cover);
            TextView tv_songs=view.findViewById(R.id.song_name);
            TextView tv_singer=view.findViewById(R.id.singer);


            try {
                iv_cover.setImageBitmap(frag2.getCover(songlist[i][2]));
            } catch (Exception e) {
                e.printStackTrace();
            };
            tv_songs.setText(songlist[i][0]);
            tv_singer.setText(songlist[i][3]);
            return view;
        }
    }
}
