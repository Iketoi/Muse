package com.example.muse;

import static java.lang.Integer.parseInt;

import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;

public class NetMusic_Activity  extends AppCompatActivity implements View.OnClickListener{
    private static SeekBar bar;
    private static TextView bar_progress,bar_total;
    public static TextView song_name,singer_name,count;
    public static ImageView iv_music,background;
    public static ImageButton btn_loop,btn_shuf,btn_porc,btn_download;
    public static ObjectAnimator animator;
    public static NetMusicService.MusicControl musicControl;
    private static ArrayList<Song_Net> song_nets;
    private static SharedPreferences loop_mode,shuf_mode;

    private static SharedPreferences theme_settings;
    Intent intent1,intent2;
    NetMusic_Activity.MyServiceConn conn;
    NetMusic_Activity.MyServiceConn_fa conn1;
    NetMusic_Activity.MyServiceConn_stopAni conn2;
    private boolean isUnbind =false;//记录服务是否被解绑

    public static Handler Nmhandler = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(@NonNull Message msg){
            super.handleMessage(msg);
            if(msg.what==1){
                int songNum = (int) msg.obj;
                play_st(songNum);
                animator.start();
                Log.e("handler","接收到消息");
            }
        }
    };//播放音乐的handler

    public static void play_st(int songNum){
        NetMusic_Activity.song_name.setText(song_nets.get(songNum).getTitle());
        NetMusic_Activity.iv_music.setImageBitmap(song_nets.get(songNum).getCover());
        MainActivity.song_name.setText(song_nets.get(songNum).getTitle());
        MainActivity.iv_music.setImageBitmap(song_nets.get(songNum).getCover());
        NetMusic_Activity.singer_name.setText(song_nets.get(songNum).getSinger());
        NetMusic_Activity.count.setText(String.valueOf(songNum));
        MainActivity.count.setText(String.valueOf(songNum));
        MainActivity.netid.setText(song_nets.get(songNum).getId());
    }//初始化播放页面

    public static Handler handler=new Handler(Looper.myLooper()){//创建消息处理器对象
        //在主线程中处理从子线程发送过来的消息
        @Override
        public void handleMessage(Message msg){
            Bundle bundle=msg.getData();//获取从子线程发送过来的音乐播放进度
            int duration = song_nets.get(Integer.parseInt(count.getText().toString())).getDuration();
            int currentPosition=bundle.getInt("currentPosition");
            bar.setMax(duration);
            bar.setProgress(currentPosition);

            //歌曲总时长
            int minute=duration/1000/60;
            int second=duration/1000%60;
            String strMinute;
            String strSecond;
            if(minute<10){//如果歌曲的时间中的分钟小于10
                strMinute="0"+minute;//在分钟的前面加一个0
            }else{
                strMinute=minute+"";
            }
            if (second<10){//如果歌曲中的秒钟小于10
                strSecond="0"+second;//在秒钟前面加一个0
            }else{
                strSecond=second+"";
            }
            String total = strMinute+":"+strSecond;
            bar_total.setText(total);
            //歌曲当前播放时长
            minute=currentPosition/1000/60;
            second=currentPosition/1000%60;
            if(minute<10){//如果歌曲的时间中的分钟小于10
                strMinute="0"+minute;//在分钟的前面加一个0
            }else{
                strMinute=minute+" ";
            }
            if (second<10){//如果歌曲中的秒钟小于10
                strSecond="0"+second;//在秒钟前面加一个0
            }else{
                strSecond=second+" ";
            }
            String progress = strMinute+":"+strSecond;
            bar_progress.setText(progress);
        }
    };//处理进度条的handler

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_netmusic);
        bar=findViewById(R.id.bar);
        theme_settings = getSharedPreferences("theme", Context.MODE_PRIVATE);
        int theme = theme_settings.getInt("theme",1);
        switch (theme){
            case 1:
                bar.setProgressDrawableTiled(getDrawable(R.drawable.st_seekbar));
                break;
            case 2:
                bar.setProgressDrawableTiled(getDrawable(R.drawable.seekbar_blue));
                break;
            case 3:
                bar.setProgressDrawableTiled(getDrawable(R.drawable.seekbar_green));
                break;
            case 4:
                bar.setProgressDrawableTiled(getDrawable(R.drawable.seekbar_pink));
                break;

        }
        intent1=getIntent();
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//onCreate周期

    private void init() throws Exception{
        song_nets = NetList_Activity.song_nets;

        background = findViewById(R.id.background);
        bar_progress=findViewById(R.id.bar_progress);
        bar_total=findViewById(R.id.bar_total);

        song_name=findViewById(R.id.song_name);
        singer_name=findViewById(R.id.singer_name);
        iv_music=findViewById(R.id.iv_music);
        btn_loop = findViewById(R.id.btn_loop);
        btn_shuf = findViewById(R.id.btn_shuf);
        btn_porc = findViewById(R.id.btn_porc);
        btn_download = findViewById(R.id.btn_download);
        count = findViewById(R.id.count);

        loop_mode  = getSharedPreferences("loop_mode", Context.MODE_PRIVATE);
        boolean loop_modes = loop_mode.getBoolean("loop_mode",false);
        if(loop_modes){
            btn_loop.setImageDrawable(getDrawable(R.drawable.btn_loop_on));

            Log.e("loop","on");
        }else{
            btn_loop.setImageDrawable(getDrawable(R.drawable.btn_loop));
            Log.e("loop","off");
        }
        shuf_mode  = getSharedPreferences("shuf_mode", Context.MODE_PRIVATE);
        boolean shuf_modes = shuf_mode.getBoolean("shuf_mode",false);
        if(shuf_modes){
            btn_shuf.setImageDrawable(getDrawable(R.drawable.btn_shuffle_on));
            Log.e("shuf","on");
        }else{
            btn_shuf.setImageDrawable(getDrawable(R.drawable.btn_shuf));
            Log.e("shuf","on");
        }


        findViewById(R.id.btn_prev).setOnClickListener(this);
        findViewById(R.id.btn_next).setOnClickListener(this);
        findViewById(R.id.btn_loop).setOnClickListener(this);
        findViewById(R.id.btn_porc).setOnClickListener(this);
        findViewById(R.id.btn_shuf).setOnClickListener(this);


        animator=ObjectAnimator.ofFloat(iv_music,"rotation",0f,360.0f);
        animator.setDuration(10000);//动画旋转一周的时间为10秒
        animator.setInterpolator(new LinearInterpolator());//匀速
        animator.setRepeatCount(-1);//-1表示设置动画无限循环
        if(conn==null){
            if(getIntent().hasExtra("error")){//从主页点击重新连接服务
                Log.e("主页点击","已恢复连接");
                Intent intent = getIntent();
                String position = intent.getStringExtra("position");
                count.setText(position);
                MainActivity.count.setText(position);
                int count = parseInt(position);
                iv_music.setImageBitmap(song_nets.get(count).getCover());
//                        background.setImageBitmap(cover);
                MainActivity.iv_music.setImageBitmap(song_nets.get(count).getCover());
                song_name.setText(song_nets.get(count).getTitle());
                singer_name.setText(song_nets.get(count).getSinger());
                intent2=new Intent(NetMusic_Activity.this,NetMusicService.class);
                if(getIntent().hasExtra("stopAni")){//歌曲暂停时从主页点击，重新连接服务并重新设置animator
                    conn2 = new NetMusic_Activity.MyServiceConn_stopAni();
                    btn_porc.setImageDrawable(getDrawable(R.drawable.btn_pause));
                    bindService(intent2,conn2,BIND_AUTO_CREATE);
                    Log.e("conn2","已连接服务");
                }else{
                    conn1=new NetMusic_Activity.MyServiceConn_fa();
                    bindService(intent2,conn1,BIND_AUTO_CREATE);
                }
            }
            else{
                String position= intent1.getStringExtra("position");
                count.setText(position);
                MainActivity.count.setText(position);
                int i=parseInt(position);
                song_name.setText(song_nets.get(i).getTitle());
                MainActivity.song_name.setText(song_nets.get(i).getTitle());
                singer_name.setText(song_nets.get(i).getSinger());
                iv_music.setImageBitmap(song_nets.get(i).getCover());
//                        background.setImageBitmap(blur.doBlur(cover,2,true));
                MainActivity.iv_music.setImageBitmap(song_nets.get(i).getCover());

                intent2=new Intent(NetMusic_Activity.this,NetMusicService.class);
                conn=new NetMusic_Activity.MyServiceConn();//创建服务连接对象
                bindService(intent2,conn,BIND_AUTO_CREATE);//绑定服务
            }
        }

        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //进度条改变时，会调用此方法
                if (progress==seekBar.getMax()){//当滑动条到末端时，结束动画
                    animator.pause();//停止播放动画
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {//滑动条开始滑动时调用
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {//滑动条停止滑动时调用
                //根据拖动的进度改变音乐播放进度
                int progress=seekBar.getProgress();//获取seekBar的进度
                musicControl.seekTo(progress);//改变播放进度
            }
        });

        btn_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("position",count.getText().toString());
                int position = parseInt(count.getText().toString());
                String url = song_nets.get(position).getUrl();
                String name = song_nets.get(position).getSinger()+song_nets.get(position).getTitle();
                String id = song_nets.get(position).getId();
                DownloadUtil.downLoad(NetMusic_Activity.this.getApplicationContext(),url,name,id);

            }
        });
    }

    class MyServiceConn_fa implements ServiceConnection {//用于实现连接服务
        @Override
        public void onServiceConnected(ComponentName name, IBinder service){
            musicControl=(NetMusicService.MusicControl) service;
            animator.start();
            Log.e("frommainact","reconnect");
        }
        @Override
        public void onServiceDisconnected(ComponentName name){
            unbind(isUnbind);
            Log.e("服务","已断开");
        }
    }

    class MyServiceConn_stopAni implements ServiceConnection {//用于实现连接服务
        @Override
        public void onServiceConnected(ComponentName name, IBinder service){
            musicControl=(NetMusicService.MusicControl) service;
            animator.pause();
        }
        @Override
        public void onServiceDisconnected(ComponentName name){
            unbind(isUnbind);
            Log.e("服务","已断开");
        }
    }

    class MyServiceConn implements ServiceConnection{//用于实现连接服务
        @Override
        public void onServiceConnected(ComponentName name, IBinder service){
            musicControl=(NetMusicService.MusicControl) service;
            intent1=getIntent();
            String position= intent1.getStringExtra("position");
            int i=parseInt(position);
            musicControl.play(i);
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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onClick(View v) {
        int position = Integer.parseInt(count.getText().toString());
        switch (v.getId()) {
            case R.id.btn_porc:
                try {
                    musicControl.porc();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_prev:
                musicControl.prev(position);
                Log.e("position",String.valueOf(position));
                animator.start();
                break;
            case R.id.btn_next:
                musicControl.next(position);
                animator.start();
                break;
            case R.id.btn_loop:
                musicControl.setLooping();
                break;
            case R.id.btn_shuf:
                musicControl.shuffle();
                break;
        }
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
//        unbind(isUnbind);//解绑服务
    }

}
