package com.example.muse;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import android.content.Intent;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class frag3 extends Fragment {
    private static View settings;
    private static SharedPreferences theme_setting;
    private static Button theme1, theme2, theme3, theme4;
    private static Hitokoto hitokoto;

    public static Handler sharedhandler = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(@NonNull Message msg){
            super.handleMessage(msg);
            if(msg.what==1){
                theme1 = settings.findViewById(R.id.btn_theme1);
                theme2 = settings.findViewById(R.id.btn_theme2);
                theme3 = settings.findViewById(R.id.btn_theme3);
                theme4 = settings.findViewById(R.id.btn_theme4);


                theme1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        theme_setting.edit().putInt("theme",1).commit();
                    }
                });
                theme2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        theme_setting.edit().putInt("theme",2).commit();
                    }
                });
                theme3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        theme_setting.edit().putInt("theme",3).commit();
                    }
                });
                theme4.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        theme_setting.edit().putInt("theme",4).commit();
                    }
                });
            }
        }
    };

    public static Handler hitokotohandler = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(@NonNull Message msg){
            super.handleMessage(msg);
            if(msg.what==1){
                hitokoto = (Hitokoto) msg.obj;
                TextView tvText = frag3.settings.findViewById(R.id.tvText);
                TextView tvAuthor = frag3.settings.findViewById(R.id.tvAuthor);
                TextView tvSource = frag3.settings.findViewById(R.id.tvSource);


                tvText.setText(hitokoto.getHitokoto());
                tvAuthor.setText(hitokoto.getCreator());
                tvSource.setText(hitokoto.getOrigin());
            }
        }
    };

    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        settings = inflater.inflate(R.layout.setting_layout, null);

        theme_setting = getActivity().getSharedPreferences("theme", Context.MODE_PRIVATE);

        new Thread(new Runnable() {
            @Override
            public void run() {
                Message msg = Message.obtain();
                msg.what=1;
                sharedhandler.sendMessage(msg);
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Message msg = Message.obtain();
                    msg.what=1;
                    hitokoto = Hitokoto_get.getHitokoto();
                    msg.obj = hitokoto;
                    Log.e("fasong","hitokoto");
                    hitokotohandler.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();

        CardView cv = frag3.settings.findViewById(R.id.hitokoto);
        cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Message msg = Message.obtain();
                            msg.what=1;
                            hitokoto = Hitokoto_get.getHitokoto();
                            msg.obj = hitokoto;
                            hitokotohandler.sendMessage(msg);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }).start();
            }
        });

        return settings;

    }




}
