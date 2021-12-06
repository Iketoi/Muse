package com.example.muse;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;


import org.json.JSONArray;
import org.json.JSONObject;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class frag2 extends Fragment {
    public ArrayList<Song_Net> song_nets;
    private View view;
    public static String id;
    private String myurl = "http://139.196.76.67:3000/song/url?id=";
    private final String r_type = "POST";
    private static String result;

    public View onCreateView(final LayoutInflater inflater1, ViewGroup container, Bundle savedInstanceState) {
        view = inflater1.inflate(R.layout.frag2_layout, null);
        Button btn_s = view.findViewById(R.id.btn_search);
        EditText search = view.findViewById(R.id.item_search);
        btn_s.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.playmode=true;
                MainActivity.p=1;
                id = search.getText().toString();
                MainActivity.netid.setText(id);
                MainActivity.musicControl.pausePlay();
                Intent i = new Intent(frag2.this.getContext(),NetMusic_Activity.class);
                i.putExtra("id",id);
                startActivity(i);

            }
        });
        return view;
    }
}
