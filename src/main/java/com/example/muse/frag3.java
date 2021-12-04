package com.example.muse;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.fragment.app.Fragment;
import android.content.Intent;

import java.io.ByteArrayOutputStream;

public class frag3 extends Fragment {
    private View settings;
    public static frag3 getInstance(int count){
        frag3 rightFragment = new frag3();
        Bundle bundle = new Bundle();
        //将需要传递的字符串以键值对的形式传入bundle
        bundle.putString("count",String.valueOf(count));
        rightFragment.setArguments(bundle);
        return rightFragment;
    }

    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        settings = inflater.inflate(R.layout.setting_layout, null);
        Button theme1 = settings.findViewById(R.id.btn_theme1);
        Button theme2 = settings.findViewById(R.id.btn_theme2);

        int count;

        Bundle bundle = this.getArguments();
        String frag3_count = bundle.getString("count");
        count = Integer.parseInt(frag3_count);
        Log.e("frag3_count",String.valueOf(count));




        theme1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendIntent(0,count);
            }
        });
        theme2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendIntent(1,count);
            }
        });

        return settings;

    }

    public void sendIntent(int theme, int count){
        Intent intent = new Intent(frag3.this.getContext(),MainActivity.class);Bundle bd = new Bundle();
        bd.putInt("theme",theme);
        bd.putString("songname",MainActivity.songList.get(count).getTitle());
        Bitmap cover = MainActivity.songList.get(count).getCover();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        cover.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] coverlist = new byte[MainActivity.songList.get(count).getCover().getByteCount()];
        coverlist = baos.toByteArray();
        bd.putByteArray("cover",coverlist);
        intent.putExtra("bundle",bd);
        startActivity(intent);
    }


}
