package com.example.muse;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private FragmentManager fm;
    private FragmentTransaction ft;
    public static ImageView iv_music;
    public static TextView song_name,count,prog,netid;
    public static ImageView btn_next;
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
    private static SharedPreferences loop_mode,shuf_mode;

    private Handler handler = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(@NonNull Message msg){
            super.handleMessage(msg);
            if(msg.what==1){
                songList = (ArrayList<Song>) msg.obj;
                iv_music.setImageDrawable(getDrawable(R.drawable.muse1));
                btn_next.setImageDrawable(getDrawable(R.drawable.btn_next));
                mAm = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                touch_bar = findViewById(R.id.touch_bar);

                touch_bar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(!playmode){//本地播放模式
                            if(!mAm.isMusicActive()){
                                if(p==-1){//p值为-1时，即刚进入软件，点击touchbar随机播放
                                    if(songList!=null){
                                        Random random = new Random();
                                        int r = random.nextInt(songList.size());
                                        MainActivity.count.setText(String.valueOf(r));
                                        Intent intent=new Intent(MainActivity.this.getApplicationContext(),Music_Activity.class);
                                        song_name.setText(songList.get(r).getTitle());
                                        intent.putExtra("position",String.valueOf(r));
                                        Log.e("发送",String.valueOf(r));
                                        p=1;
                                        startActivity(intent);
                                    }
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
                        }if(playmode){//在线播放模式
                            if(!mAm.isMusicActive()){//返回在线音乐播放的activity
                                Intent i = new Intent(MainActivity.this.getApplicationContext(),NetMusic_Activity.class);
                                i.putExtra("error","isconnected");
                                int position = Integer.parseInt(count.getText().toString());
                                i.putExtra("position",String.valueOf(position));
                                i.putExtra("id",netid.toString());
                                i.putExtra("stopAni","stop");
                                startActivity(i);
                            }else{//此时音乐正在播放，返回对应的activity
                                Intent i = new Intent(MainActivity.this.getApplicationContext(),NetMusic_Activity.class);
                                i.putExtra("error","isconnected");
                                int position = Integer.parseInt(count.getText().toString());
                                i.putExtra("id",netid.toString());
                                i.putExtra("position",String.valueOf(position));
                                startActivity(i);
                            }
                        }
                    }
                });
                if(conn==null){
                    conn=new MyServiceConn();
                    Log.e("maina","已经建立连接");
                }
                Intent intent=new Intent(MainActivity.this,MusicService.class);
                intent.setType("main");
                bindService(intent,conn,BIND_AUTO_CREATE);

            }
        }
    };//处理子线程中的数据


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBar();

//        Uri uri1 = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata");
//        Intent intent1 = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
//        intent1.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
//                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
//                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
//                | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
//        intent1.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri1);
//        startActivityForResult(intent1, 11);
//        if (!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager())) {
//            //表明已经有这个权限了
//            Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
//            startActivity(intent);
//        }
        setContentView(R.layout.activity_main);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    songList = getSongInfo();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Message msg = Message.obtain();
                msg.obj = songList;
                msg.what=1;
                handler.sendMessage(msg);
            }
        }).start();

        loop_mode  = getSharedPreferences("loop_mode", Context.MODE_PRIVATE);
        shuf_mode  = getSharedPreferences("shuf_mode", Context.MODE_PRIVATE);



        prog = findViewById(R.id.prog);
        count = findViewById(R.id.count);
        netid = findViewById(R.id.net_id);



        ImageButton tv1 = findViewById(R.id.menu1);
        ImageButton tv2 = findViewById(R.id.menu2);
        ImageButton tv3 = findViewById(R.id.menu3);
        tv1.setOnClickListener(this);
        tv2.setOnClickListener(this);
        tv3.setOnClickListener(this);

        fm=getSupportFragmentManager();
        ft=fm.beginTransaction();
        ft.replace(R.id.content,new frag1());
        ft.commit();

        iv_music = findViewById(R.id.iv_music);
        song_name = findViewById(R.id.song_name);

        btn_next = findViewById(R.id.btn_next);
        btn_next.setOnClickListener(this);
    }

    public ArrayList<Song> getSongInfo() throws Exception {
        songList = new ArrayList<>();
        songList=SongInfo.getAllSongs(MainActivity.this.getApplicationContext());
        return songList;
    }

    class MyServiceConn implements ServiceConnection {//用于实现连接服务
        @Override
        public void onServiceConnected(ComponentName name, IBinder service){
            musicControl=(MusicService.MusicControl) service;
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
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(conn_net==null){
                            conn_net = new MyServiceConn_Net();
                        }
                        Intent intent_net=new Intent(MainActivity.this,NetMusicService.class);
                        intent_net.setType("main");
                        bindService(intent_net,conn_net,BIND_AUTO_CREATE);
                    }
                }).start();
                ft.replace(R.id.content,new frag2());
                break;
            case R.id.menu3:
                ft.replace(R.id.content,new frag3());
                break;
            case R.id.btn_next:
                if(playmode){
                    musicControl_net.next(position);
                }else{
                    musicControl.next(position);
                }
            default:
                break;
        }
        ft.commit();
    }

    @Override
    protected void onDestroy(){
        loop_mode.edit().putBoolean("loop_mode",false).commit();
        shuf_mode.edit().putBoolean("shuf_mode",false).commit();
        super.onDestroy();

    }
}
