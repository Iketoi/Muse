package com.example.muse;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import static java.lang.Integer.parseInt;

import java.io.IOException;
import java.util.ArrayList;

public class Music_Activity extends AppCompatActivity implements View.OnClickListener{
    private static SeekBar bar;
    private static TextView bar_progress,bar_total;
    public static TextView song_name,count,singer_name;
    public static ImageView iv_music;
    public static ImageButton btn_loop,btn_porc;
    public static ObjectAnimator animator;
    public static MusicService.MusicControl musicControl;
    public static ArrayList<Song> songList;
    protected boolean useThemestatusBarColor = false;
    protected boolean useStatusBarColor = true;
    Intent intent1,intent2;MyServiceConn conn;
    MyServiceConn_fa conn1;
    MyServiceConn_stopAni conn2;
    private boolean isUnbind =false;//记录服务是否被解绑

    public static Handler mhandler = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(@NonNull Message msg){
            super.handleMessage(msg);
            if(msg.what==1){
                animator=ObjectAnimator.ofFloat(iv_music,"rotation",0f,360.0f);
                animator.setDuration(10000);//动画旋转一周的时间为10秒
                animator.setInterpolator(new LinearInterpolator());//匀速
                animator.setRepeatCount(-1);//-1表示设置动画无限循环
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
                int songNum = (int) msg.obj;
                play_st(songNum);
                animator.start();
                Log.e("handler","接收到消息");
            }
        }
    };//播放音乐的handler

    public static void play_st(int songNum){
        Music_Activity.song_name.setText(songList.get(songNum).getTitle());
        Music_Activity.iv_music.setImageBitmap(songList.get(songNum).getCover());
        MainActivity.song_name.setText(songList.get(songNum).getTitle());
        MainActivity.iv_music.setImageBitmap(songList.get(songNum).getCover());
        Music_Activity.singer_name.setText(songList.get(songNum).getSinger());
        Music_Activity.count.setText(String.valueOf(songNum));
        MainActivity.count.setText(String.valueOf(songNum));
    }

    private Handler viewhandler = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(@NonNull Message msg){
            super.handleMessage(msg);
            if(msg.what==1){
                animator=ObjectAnimator.ofFloat(iv_music,"rotation",0f,360.0f);
                animator.setDuration(10000);//动画旋转一周的时间为10秒
                animator.setInterpolator(new LinearInterpolator());//匀速
                animator.setRepeatCount(-1);//-1表示设置动画无限循环
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
                if(conn==null){
                    if(getIntent().hasExtra("error")){//从主页点击重新连接服务
                        Log.e("主页点击","已恢复连接");
                        Intent intent = getIntent();
                        String position = intent.getStringExtra("position");
                        count.setText(position);
                        int count = parseInt(position);
                        iv_music.setImageBitmap(songList.get(count).getCover());
                        song_name.setText(songList.get(count).getTitle());
                        singer_name.setText(songList.get(count).getSinger());
                        intent2=new Intent(Music_Activity.iv_music.getContext(),MusicService.class);
                        if(getIntent().hasExtra("stopAni")){//歌曲暂停时从主页点击，重新连接服务并重新设置animator
                            conn2 = new MyServiceConn_stopAni();
                            btn_porc.setImageDrawable(getDrawable(R.drawable.btn_pause));
                            bindService(intent2,conn2,BIND_AUTO_CREATE);
                            Log.e("conn2","已连接服务");
                        }else{
                            conn1=new MyServiceConn_fa();
                            bindService(intent2,conn1,BIND_AUTO_CREATE);
                        }
                    }else{
                        String position= intent1.getStringExtra("position");
                        count.setText(position);
                        int i=parseInt(position);
                        song_name.setText(songList.get(i).getTitle());
                        MainActivity.song_name.setText(songList.get(i).getTitle());
                        iv_music.setImageBitmap(songList.get(i).getCover());
                        singer_name.setText(songList.get(i).getSinger());
                        MainActivity.iv_music.setImageBitmap(songList.get(i).getCover());
                        intent2=new Intent(Music_Activity.iv_music.getContext(),MusicService.class);
                        conn=new MyServiceConn();//创建服务连接对象
                        bindService(intent2,conn,BIND_AUTO_CREATE);//绑定服务
                    }
                }

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setStatusBar();
        setContentView(R.layout.activity_music);
        intent1=getIntent();
        init();
    }
    private void init(){
        songList=frag1.songList;

        bar_progress=findViewById(R.id.bar_progress);
        bar_total=findViewById(R.id.bar_total);
        bar=findViewById(R.id.bar);
        song_name=findViewById(R.id.song_name);
        singer_name=findViewById(R.id.singer_name);
        iv_music=findViewById(R.id.iv_music);
        btn_loop = findViewById(R.id.btn_loop);
        btn_porc = findViewById(R.id.btn_porc);
        count = findViewById(R.id.count);

        findViewById(R.id.btn_prev).setOnClickListener(this);
        findViewById(R.id.btn_next).setOnClickListener(this);
        findViewById(R.id.btn_loop).setOnClickListener(this);
        findViewById(R.id.btn_porc).setOnClickListener(this);
        findViewById(R.id.btn_shuf).setOnClickListener(this);

        new Thread(new Runnable() {
            @Override
            public void run() {
                Message msg = Message.obtain();
                msg.what=1;
                viewhandler.sendMessage(msg);
            }
        }).start();

    }

    public static Handler handler=new Handler(Looper.myLooper()){//创建消息处理器对象
        //在主线程中处理从子线程发送过来的消息
        @Override
        public void handleMessage(Message msg){
            Bundle bundle=msg.getData();//获取从子线程发送过来的音乐播放进度
//            Log.e("musicactivity",String.valueOf(songNum));
            int duration = songList.get(Integer.parseInt(count.getText().toString())).getDuration();
            int currentPosition=bundle.getInt("currentPosition");
//            Log.e("musicactivity_handler_timer",String.valueOf(currentPosition));
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

    class MyServiceConn_fa implements ServiceConnection {//用于实现连接服务
        @Override
        public void onServiceConnected(ComponentName name, IBinder service){
            musicControl=(MusicService.MusicControl) service;
            animator.start();
        }
        @Override
        public void onServiceDisconnected(ComponentName name){
            unbind(isUnbind);
            Log.e("服务","已断开");
        }
    }//从MainActivity点击重新连接

    class MyServiceConn_stopAni implements ServiceConnection {//用于实现连接服务
        @Override
        public void onServiceConnected(ComponentName name, IBinder service){
            musicControl=(MusicService.MusicControl) service;
            animator.pause();
        }
        @Override
        public void onServiceDisconnected(ComponentName name){
            unbind(isUnbind);
//            stopService(intent2);
            Log.e("服务","已断开");
        }
    }//暂停从MainActivity点击重新连接

    class MyServiceConn implements ServiceConnection{//用于实现连接服务
        @Override
        public void onServiceConnected(ComponentName name, IBinder service){
            musicControl=(MusicService.MusicControl) service;
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
    }//初始绑定服务

    private void unbind(boolean isUnbind){
        if(!isUnbind){//判断服务是否被解绑
            musicControl.pausePlay();//暂停播放音乐
            unbindService(conn);//解绑服务
        }
    }

    @Override
    protected void onStart(){
        super.onStart();
        if(MainActivity.mAm.isMusicActive()&&animator.isPaused()){
            animator.resume();
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
                Log.e("position",String.valueOf(position));
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
