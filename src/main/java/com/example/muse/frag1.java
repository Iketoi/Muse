package com.example.muse;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Objects;

public class frag1 extends Fragment {
    public static ArrayList<Song> songList;
    private View view;
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        view = inflater.inflate(R.layout.music_list, null);
        ListView listView= view.findViewById(R.id.lv);
        songList = getSongInfo();
        MyBaseAdapter myBaseAdapter = new MyBaseAdapter();
        listView.setAdapter(myBaseAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                Intent intent=new Intent(frag1.this.getContext(),Music_Activity.class);
                Intent intent=new Intent(frag1.this.getContext(),Music_Activity.class);
                intent.putExtra("name",songList.get(i).getFileName());
                intent.putExtra("position",String.valueOf(i));
                Log.e("发送",String.valueOf(i));
                MainActivity.p = 1;
                MainActivity.playmode=false;
                MainActivity.musicControl_net.pausePlay();
                startActivity(intent);

            }
        } );
        return view;
    }

    public ArrayList<Song> getSongInfo(){
        songList = new ArrayList<>();
        songList=SongInfo.getAllSongs(Objects.requireNonNull(frag1.this.getActivity()));
        return songList;
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
    }
}

