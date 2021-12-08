package com.example.muse;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class NetMusicService extends Service {
    private MediaPlayer player;
    private Timer timer;
    public static boolean isShuffle = false;
    private static String result;
    private static ArrayList<Song_Net> song_nets;


    public static Handler Nmshandler = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(@NonNull Message msg){
            super.handleMessage(msg);
            if(msg.what==1){
                song_nets= (ArrayList<Song_Net>) msg.obj;
                Log.e("handler","接收到全新集合");
            }
        }
    };//播放音乐的handler
    @Override
    public IBinder onBind(Intent intent){
        song_nets=NetList_Activity.song_nets;
        Log.e("ibinder","绑定成功");
        return new NetMusicService.MusicControl();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        return START_STICKY;
    }
    @Override
    public void onCreate(){
        super.onCreate();
        player=new MediaPlayer();

//        Log.e("song_nets_init",song_nets.get(0).getTitle());


    }
    public void addTimer(){ //添加计时器用于设置音乐播放器中的播放进度条
        if(timer==null){
            timer=new Timer();//创建计时器对象
            TimerTask task=new TimerTask() {
                @Override
                public void run() {
                    if (player==null) return;//获取歌曲总时长
                    int currentPosition=player.getCurrentPosition();//获取播放进度
                    Message msg=NetMusic_Activity.handler.obtainMessage();//创建消息对象
                    //将音乐的总时长和播放进度封装至消息对象中
                    Bundle bundle=new Bundle();
                    bundle.putInt("currentPosition",currentPosition);
                    msg.setData(bundle);
                    //将消息发送到主线程的消息队列
                    NetMusic_Activity.handler.sendMessage(msg);
                }
            };
            //开始计时任务后的5毫秒，第一次执行task任务，以后每500毫秒执行一次
            timer.schedule(task,5,500);
        }
    }




    class MusicControl extends Binder {//Binder是一种跨进程的通信方式
        public void play(int songNum){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.e("song_nets_start",song_nets.get(0).getTitle());
                        Uri uri = Uri.parse(song_nets.get(songNum).getUrl());
                        player.reset();
                        player.setDataSource(NetMusic_Activity.iv_music.getContext(),uri);
                        player.prepare();
                        player.start();
                        addTimer();
                        Log.e("duration",String.valueOf(song_nets.get(songNum).getDuration()));
                        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer) {
                                next(songNum);
                            }
                        });
                        Message msg = new Message();
                        msg.what=1;
                        msg.obj=songNum;
                        NetMusic_Activity.Nmhandler.sendMessage(msg);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        public void pausePlay(){
            player.pause();
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        public void porc() throws IOException {
            if (player.isPlaying()){
                player.pause();
                NetMusic_Activity.animator.pause();
                NetMusic_Activity.btn_porc.setImageDrawable(getDrawable(R.drawable.btn_pause));
            }else{
                if(!player.isPlaying()){
                    player.start();
                    NetMusic_Activity.btn_porc.setImageDrawable(getDrawable(R.drawable.btn_play));
                    Log.e("player","start");
                    if(NetMusic_Activity.animator.isPaused()){
                        Log.e("animator","ispaused");
                        NetMusic_Activity.animator.resume();
                    }else{
                        Log.e("animator","restart");
                        NetMusic_Activity.animator.start();
                    }
                }
            }
        }
        public void seekTo(int progress){
            player.seekTo(progress);
        }

        public void shuffle(){
            if(!isShuffle){
                isShuffle=true;
                NetMusic_Activity.btn_shuf.setImageDrawable(getDrawable(R.drawable.btn_shuffle_on));
                Toast.makeText(NetMusic_Activity.btn_shuf.getContext(), "开启随机播放",Toast.LENGTH_SHORT).show();
            }else{
                isShuffle=false;
                NetMusic_Activity.btn_shuf.setImageDrawable(getDrawable(R.drawable.btn_shuf));
                Toast.makeText(NetMusic_Activity.btn_loop.getContext(), "关闭随机播放",Toast.LENGTH_SHORT).show();
            }
        }

        public void setLooping(){
            if(player.isLooping()){
                player.setLooping(false);
                Toast.makeText(NetMusic_Activity.btn_loop.getContext(), "已关闭循环",Toast.LENGTH_SHORT).show();
                NetMusic_Activity.btn_loop.setImageDrawable(getDrawable(R.drawable.btn_loop));
            }else{
                player.setLooping(true);
                NetMusic_Activity.btn_loop.setImageDrawable(getDrawable(R.drawable.btn_loop_on));
                Toast.makeText(NetMusic_Activity.btn_loop.getContext(), "已开启循环",Toast.LENGTH_SHORT).show();
                player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        NetMusic_Activity.animator.start();
                    }
                });
            }
        }

        public void next(int songNum){
            NetMusic_Activity.btn_porc.setImageDrawable(getDrawable(R.drawable.btn_play));
            if(isShuffle){
                Random random = new Random();
                int r= random.nextInt(song_nets.size());
                while(r==songNum){
                    r= random.nextInt(song_nets.size());
                }
                songNum=r;
                Toast.makeText(Music_Activity.btn_loop.getContext(), "随机播放下一首",Toast.LENGTH_SHORT).show();
            }else{
                songNum = songNum == song_nets.size() - 1 ? 0 : songNum + 1;
            }
            play(songNum);
        }
        public void prev(int songNum){
            NetMusic_Activity.btn_porc.setImageDrawable(getDrawable(R.drawable.btn_play));
            if(isShuffle){
                Random random = new Random();
                int r= random.nextInt(song_nets.size());
                while(r==songNum){
                    r= random.nextInt(song_nets.size());
                }
                songNum=r;
                Toast.makeText(Music_Activity.btn_loop.getContext(), "随机播放上一首",Toast.LENGTH_SHORT).show();
            }else{
                songNum = songNum == 0 ? song_nets.size() - 1 : songNum - 1;
            }
            play(songNum);
        }
    }

    public static String connect(String myurl, String r_type){
        HttpURLConnection con;
        BufferedReader buffer;
        StringBuffer resultBuffer;
        Log.e("runnable","已开启线程");
        try {
            URL url = new URL(myurl);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod(r_type);
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
        }
        return result;
    }



    @Override
    public void onDestroy(){
        super.onDestroy();
        if(player==null) return;
        if(player.isPlaying()) player.stop();//停止播放音乐
        player.reset();
        player.release();//释放占用的资源
        player=null;//将player置为空
        Log.e("服务","已断开");
    }
}