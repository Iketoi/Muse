package com.example.muse;


import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MusicService extends Service {
    private MediaPlayer player;
    private Timer timer;
    public static ArrayList<Song> songList;
    public static boolean isShuffle = false;

    @Override
    public  IBinder onBind(Intent intent){
        return new MusicControl();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        return START_STICKY;
    }
    @Override
    public void onCreate(){
        super.onCreate();
        player=new MediaPlayer();
        songList = MainActivity.songList;

    }
    public void addTimer(){ //添加计时器用于设置音乐播放器中的播放进度条
        if(timer==null){
            timer=new Timer();//创建计时器对象
            TimerTask task=new TimerTask() {
                @Override
                public void run() {
                    if (player==null) return;
                    int duration=player.getDuration();//获取歌曲总时长
                    int currentPosition=player.getCurrentPosition();//获取播放进度
                    Message msg=Music_Activity.handler.obtainMessage();//创建消息对象
                    //将音乐的总时长和播放进度封装至消息对象中
                    Bundle bundle=new Bundle();
                    bundle.putInt("duration",duration);
                    bundle.putInt("currentPosition",currentPosition);
                    msg.setData(bundle);
                    //将消息发送到主线程的消息队列
                    Music_Activity.handler.sendMessage(msg);
                }
            };
            //开始计时任务后的5毫秒，第一次执行task任务，以后每500毫秒执行一次
            timer.schedule(task,5,500);
        }
    }


    class MusicControl extends Binder{//Binder是一种跨进程的通信方式
        public void play(int songNum){//String path
            try{
                play_st(songNum);
                String dataSource = songList.get(songNum).getDatasource();
                player.reset();
                player.setDataSource(dataSource);
                player.prepare();
                player.start();
                Music_Activity.animator.start();
                addTimer();
                player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        next(songNum);

                    }
                });
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        public void pausePlay(){
            player.pause();
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        public void porc() throws IOException {
            if (player.isPlaying()){
                player.pause();
                Music_Activity.animator.pause();
//                MainActivity.animator.pause();

            }else{
                if(!player.isPlaying()){
//                    Music_Activity.animator.isPaused()
                    player.start();
                    Log.e("player","start");
                    if(Music_Activity.animator.isPaused()){
                        Log.e("animator","ispaused");
                        Music_Activity.animator.resume();
                    }else{
                        Log.e("animator","restart");
                        Music_Activity.animator.start();

                    }

//                    MainActivity.animator.resume();
                }
            }
        }
        public void seekTo(int progress){
            player.seekTo(progress);
        }

        public void shuffle(){
            if(!isShuffle){
                isShuffle=true;
                Toast.makeText(Music_Activity.btn_loop.getContext(), "开启随机播放",Toast.LENGTH_SHORT).show();
            }else{
                isShuffle=false;
                Toast.makeText(Music_Activity.btn_loop.getContext(), "关闭随机播放",Toast.LENGTH_SHORT).show();
            }
        }

        public void setLooping(){
            if(player.isLooping()){
                player.setLooping(false);
                Toast.makeText(Music_Activity.btn_loop.getContext(), "已关闭循环",Toast.LENGTH_SHORT).show();
            }else{
                player.setLooping(true);
                Toast.makeText(Music_Activity.btn_loop.getContext(), "已开启循环",Toast.LENGTH_SHORT).show();
            }
        }

        public void next(int songNum){
            if(isShuffle){
                Random random = new Random();
                int r= random.nextInt(songList.size());
                while(r==songNum){
                    r= random.nextInt(songList.size());
                }
                songNum=r;
                Toast.makeText(Music_Activity.btn_loop.getContext(), "随机播放下一首",Toast.LENGTH_SHORT).show();
            }else{
                songNum = songNum == songList.size() - 1 ? 0 : songNum + 1;
            }
            play(songNum);
        }
        public void prev(int songNum){
            if(isShuffle){
                Random random = new Random();
                int r= random.nextInt(songList.size());
                while(r==songNum){
                    r= random.nextInt(songList.size());
                }
                songNum=r;
                Toast.makeText(Music_Activity.btn_loop.getContext(), "随机播放上一首",Toast.LENGTH_SHORT).show();
            }else{
                songNum = songNum == 0 ? songList.size() - 1 : songNum - 1;
            }
            play(songNum);
        }
    }

    public void play_st(int songNum){
        Music_Activity.song_name.setText(songList.get(songNum).getTitle());
        Music_Activity.iv_music.setImageBitmap(songList.get(songNum).getCover());
        MainActivity.song_name.setText(songList.get(songNum).getTitle());
        MainActivity.iv_music.setImageBitmap(songList.get(songNum).getCover());
        Music_Activity.singer_name.setText(songList.get(songNum).getSinger());
        Music_Activity.count.setText(String.valueOf(songNum));
        MainActivity.count.setText(String.valueOf(songNum));
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