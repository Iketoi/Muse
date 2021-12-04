package com.example.muse;



import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private FragmentManager fm;
    private FragmentTransaction ft;
    public static ImageView iv_music;
    public static TextView song_name,count,prog;
    public static LinearLayout touch_bar;
    public static ObjectAnimator animator;
    public static MusicService.MusicControl musicControl;
    private boolean isUnbind =false;
    public static MyServiceConn conn;
    public static AudioManager mAm;
    public static ArrayList<Song> songList;
    public static int p=-1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (getIntent().hasExtra("bundle") && savedInstanceState==null){
            savedInstanceState = getIntent().getExtras().getBundle("bundle");
            int theme = savedInstanceState.getInt("theme");
            if(theme==1){
                setTheme(R.style.Theme_Pink);
                String count1 = count.getText().toString();
                Log.e("count",count1);
                String songname = getIntent().getStringExtra("songname");
//                byte[] b_cover = getIntent().getByteArrayExtra("cover");
//                Bitmap cover = BitmapFactory.decodeByteArray(b_cover, 0, b_cover.length);
//                iv_music.setImageBitmap(cover);
                song_name.setText(songname);
                Log.e("songname",songList.get(Integer.parseInt(count1)).getTitle());
            }else{
                setTheme((R.style.Theme_Muse));
            }
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        songList = getSongInfo();
        Log.e("songlist",String.valueOf(songList.size()));
        FrameLayout content = findViewById(R.id.content);

        TextView tv1 = findViewById(R.id.menu1);
        TextView tv2 = findViewById(R.id.menu2);
        TextView tv3 = findViewById(R.id.menu3);
        touch_bar = findViewById(R.id.touch_bar);
        prog = findViewById(R.id.prog);
        count = findViewById(R.id.count);
        Random random = new Random();
        int r = random.nextInt(songList.size());
        MainActivity.count.setText(String.valueOf(r));
        Log.e("maincount",String.valueOf(r));


        mAm = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

        tv1.setOnClickListener(this);
        tv2.setOnClickListener(this);
        tv3.setOnClickListener(this);

        touch_bar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(conn!=null){
                    if(!mAm.isMusicActive()){
                        if(p==-1){
                            Log.e("prog",String.valueOf(p));
                            Intent intent=new Intent(MainActivity.this.getApplicationContext(),Music_Activity.class);
//                            intent.putExtra("name",songList.get(r).getTitle());
                            song_name.setText(songList.get(r).getTitle());
                            intent.putExtra("position",String.valueOf(r));
                            Log.e("发送",String.valueOf(r));
                            p=1;
                            startActivity(intent);
                        }else{
                            Intent i = new Intent(MainActivity.this.getApplicationContext(),Music_Activity.class);
                            i.putExtra("error","isconnected");
                            int position = Integer.parseInt(count.getText().toString());
                            i.putExtra("position",String.valueOf(position));
                            i.putExtra("stopAni","stop");
                            Log.e("设置图片1111",String.valueOf(position));
                            startActivity(i);
                        }
                    }else{
                        Intent i = new Intent(MainActivity.this.getApplicationContext(),Music_Activity.class);
                        i.putExtra("error","isconnected");
                        int position = Integer.parseInt(count.getText().toString());
                        i.putExtra("position",String.valueOf(position));
                        Log.e("设置图片",String.valueOf(position));
                        startActivity(i);
                    }
                }

            }
        });

        fm=getSupportFragmentManager();
        ft=fm.beginTransaction();
        ft.replace(R.id.content,new frag1());
        ft.commit();

        animator=ObjectAnimator.ofFloat(iv_music,"rotation",0f,360.0f);
        animator.setDuration(10000);
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatCount(-1);

        iv_music = findViewById(R.id.iv_music);
        song_name = findViewById(R.id.song_name);


        if(conn==null){
            conn=new MyServiceConn();
            Log.e("maina","已经建立连接");
        }
        Intent intent=new Intent(MainActivity.this,MusicService.class);
        intent.setType("main");
        bindService(intent,conn,BIND_AUTO_CREATE);



        Button porc = findViewById(R.id.btn_porc);
        porc.setOnClickListener(this);
    }

    public ArrayList<Song> getSongInfo(){
        songList = new ArrayList<>();
        songList=SongInfo.getAllSongs(this.getApplicationContext());
//        if(frag1.this.getContext()==null){
//            Log.e("haha","cuole");
//        }if(frag1.this.getContext()!=null){
//            Log.e("correct","duileya");
//        }
//        Random random = new Random();
//        int r = random.nextInt(frag1.songList.size());
//        MainActivity.count.setText(String.valueOf(r));
//        Log.e("r", String.valueOf(r));
        return songList;
    }

    class MyServiceConn implements ServiceConnection {//用于实现连接服务
        @Override
        public void onServiceConnected(ComponentName name, IBinder service){
            musicControl=(MusicService.MusicControl) service;
            animator.start();
        }
        @Override
        public void onServiceDisconnected(ComponentName name){
            unbind(isUnbind);
//            stopService(intent2);
            Log.e("服务","已断开");
        }
    }
    private void unbind(boolean isUnbind){
        if(!isUnbind){//判断服务是否被解绑
            musicControl.pausePlay();//暂停播放音乐
            unbindService(conn);//解绑服务
        }
    }


    @Override
    public void onClick(View v){
        ft=fm.beginTransaction();
        int position = Integer.parseInt(count.getText().toString());
        switch (v.getId()){
            case R.id.menu1:
                ft.replace(R.id.content,new frag1());
                break;
            case R.id.menu2:
                ft.replace(R.id.content,new frag2());
                break;
            case R.id.menu3:
                frag3 frag3_m =frag3.getInstance(position);
                ft.replace(R.id.content,frag3_m);
                break;
            case R.id.btn_porc:
                musicControl.next(position);
                Log.e("position",String.valueOf(position));
                animator.start();
            default:
                break;
        }
        ft.commit();
    }
}
