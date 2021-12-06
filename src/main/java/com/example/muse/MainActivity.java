package com.example.muse;



import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.IBinder;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private FragmentManager fm;
    private FragmentTransaction ft;
    public static ImageView iv_music;
    public static TextView song_name,count,prog,netid;
    public static ConstraintLayout touch_bar;
    public static ObjectAnimator animator;
    public static MusicService.MusicControl musicControl;
    public static NetMusicService.MusicControl musicControl_net;
    private final boolean isUnbind =false;
    public static MyServiceConn conn;
    public static MyServiceConn_Net conn_net;
    public static AudioManager mAm;
    public static ArrayList<Song> songList;
    public static int p=-1;
    public static boolean playmode=false;
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
        setStatusBar();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

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
        netid = findViewById(R.id.net_id);
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
                if(!playmode){//本地播放模式
                    if(!mAm.isMusicActive()){
                        if(p==-1){//p值为-1时，即刚进入软件，点击touchbar随机播放
                            Intent intent=new Intent(MainActivity.this.getApplicationContext(),Music_Activity.class);
                            song_name.setText(songList.get(r).getTitle());
                            intent.putExtra("position",String.valueOf(r));
                            Log.e("发送",String.valueOf(r));
                            p=1;
                            startActivity(intent);
                        }else{//p值为1时，可返回本地音乐播放的activity
                            Intent i = new Intent(MainActivity.this.getApplicationContext(),Music_Activity.class);
                            i.putExtra("error","isconnected");
                            int position = Integer.parseInt(count.getText().toString());
                            i.putExtra("position",String.valueOf(position));
                            i.putExtra("stopAni","stop");
                            Log.e("设置图片1111",String.valueOf(position));
                            startActivity(i);
                        }
                    }else{//此时音乐正在播放，返回对应的activity
                        Intent i = new Intent(MainActivity.this.getApplicationContext(),Music_Activity.class);
                        i.putExtra("error","isconnected");
                        int position = Integer.parseInt(count.getText().toString());
                        i.putExtra("position",String.valueOf(position));
                        Log.e("设置图片",String.valueOf(position));
                        startActivity(i);
                    }
                }else{//在线播放模式
                    if(!mAm.isMusicActive()){//返回在线音乐播放的activity
                        Intent i = new Intent(MainActivity.this.getApplicationContext(),NetMusic_Activity.class);
                        i.putExtra("error","isconnected");
                        i.putExtra("id",netid.getText().toString());
                        Log.e("id",netid.getText().toString());
                        i.putExtra("stopAni","stop");
                        startActivity(i);
                    }else{//此时音乐正在播放，返回对应的activity
                        Intent i = new Intent(MainActivity.this.getApplicationContext(),NetMusic_Activity.class);
                        i.putExtra("error","isconnected");
                        i.putExtra("id",netid.getText().toString());
                        Log.e("id",netid.getText().toString());
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
        if(conn_net==null){
            conn_net = new MyServiceConn_Net();
        }
        Intent intent=new Intent(MainActivity.this,MusicService.class);
        intent.setType("main");
        bindService(intent,conn,BIND_AUTO_CREATE);

        Intent intent_net=new Intent(MainActivity.this,NetMusicService.class);
        intent.setType("main");
        bindService(intent_net,conn_net,BIND_AUTO_CREATE);



        ImageView porc = findViewById(R.id.btn_porc);
        porc.setOnClickListener(this);
    }

    public ArrayList<Song> getSongInfo(){
        songList = new ArrayList<>();
        songList=SongInfo.getAllSongs(this.getApplicationContext());
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

    class MyServiceConn_Net implements ServiceConnection {//用于实现连接服务
        @Override
        public void onServiceConnected(ComponentName name, IBinder service){
            musicControl_net=(NetMusicService.MusicControl) service;
            animator.start();
        }
        @Override
        public void onServiceDisconnected(ComponentName name){
            unbind_net(isUnbind);
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

    private void unbind_net(boolean isUnbind){
        if(!isUnbind){//判断服务是否被解绑
            musicControl_net.pausePlay();//暂停播放音乐
            unbindService(conn_net);//解绑服务
        }
    }

    protected boolean useThemestatusBarColor = false;
    //是否使用状态栏文字和图标为暗色，如果状态栏采用了白色系，则需要使状态栏和图标为暗色，android6.0以上可以设置
    protected boolean useStatusBarColor = true;
    protected void setStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//5.0及以上
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            //根据上面设置是否对状态栏单独设置颜色
            if (useThemestatusBarColor) {
                getWindow().setStatusBarColor(getResources().getColor(R.color.white));//设置状态栏背景色
            } else {
                getWindow().setStatusBarColor(Color.TRANSPARENT);//透明
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && useStatusBarColor) {//android6.0以后可以对状态栏文字颜色和图标进行修改
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
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
