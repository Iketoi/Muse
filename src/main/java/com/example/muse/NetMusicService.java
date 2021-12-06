package com.example.muse;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

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
    private String myurl = "http://139.196.76.67:3000/song/url?id=";
    private final String r_type = "POST";

    @Override
    public IBinder onBind(Intent intent){
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
                    Message msg=NetMusic_Activity.handler.obtainMessage();//创建消息对象
                    //将音乐的总时长和播放进度封装至消息对象中
                    Bundle bundle=new Bundle();
                    bundle.putInt("duration",duration);
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
        public void play(String song_url){
            try {
                Uri uri = Uri.parse(song_url);
                player.reset();
                player.setDataSource(NetMusic_Activity.iv_music.getContext(),uri);
                player.prepare();
                player.start();
                addTimer();
                NetMusic_Activity.animator.start();
//                player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                    @Override
//                    public void onCompletion(MediaPlayer mediaPlayer) {
//                        next();
//                    }
//                });

            } catch (Exception e) {
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
                NetMusic_Activity.animator.pause();
            }else{
                if(!player.isPlaying()){
//                    Music_Activity.animator.isPaused()
                    player.start();
                    Log.e("player","start");
                    if(NetMusic_Activity.animator.isPaused()){
                        Log.e("animator","ispaused");
                        NetMusic_Activity.animator.resume();
                    }else{
                        Log.e("animator","restart");
                        NetMusic_Activity.animator.start();

                    }

//                    MainActivity.animator.resume();
                }
            }
        }
        public void seekTo(int progress){
            player.seekTo(progress);
        }

//        public void shuffle(){
//            if(!isShuffle){
//                isShuffle=true;
//                Toast.makeText(NetMusic_Activity.btn_loop.getContext(), "开启随机播放",Toast.LENGTH_SHORT).show();
//            }else{
//                isShuffle=false;
//                Toast.makeText(NetMusic_Activity.btn_loop.getContext(), "关闭随机播放",Toast.LENGTH_SHORT).show();
//            }
//        }

        public void setLooping(){
            if(player.isLooping()){
                player.setLooping(false);
                Toast.makeText(NetMusic_Activity.btn_loop.getContext(), "已关闭循环",Toast.LENGTH_SHORT).show();
            }else{
                player.setLooping(true);
                Toast.makeText(NetMusic_Activity.btn_loop.getContext(), "已开启循环",Toast.LENGTH_SHORT).show();
                player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        NetMusic_Activity.animator.start();
                    }
                });
            }
        }

//        public void next(int songNum){
//            if(isShuffle){
//                Random random = new Random();
//                int r= random.nextInt(songList.size());
//                while(r==songNum){
//                    r= random.nextInt(songList.size());
//                }
//                songNum=r;
//                Toast.makeText(Music_Activity.btn_loop.getContext(), "随机播放下一首",Toast.LENGTH_SHORT).show();
//            }else{
//                songNum = songNum == songList.size() - 1 ? 0 : songNum + 1;
//            }
//            play(songNum);
//        }
//        public void prev(int songNum){
//            if(isShuffle){
//                Random random = new Random();
//                int r= random.nextInt(songList.size());
//                while(r==songNum){
//                    r= random.nextInt(songList.size());
//                }
//                songNum=r;
//                Toast.makeText(Music_Activity.btn_loop.getContext(), "随机播放上一首",Toast.LENGTH_SHORT).show();
//            }else{
//                songNum = songNum == 0 ? songList.size() - 1 : songNum - 1;
//            }
//            play(songNum);
//        }
    }

//    public void play_st(int songNum){
//        Music_Activity.song_name.setText(songList.get(songNum).getTitle());
//        Music_Activity.iv_music.setImageBitmap(songList.get(songNum).getCover());
//        MainActivity.song_name.setText(songList.get(songNum).getTitle());
//        MainActivity.iv_music.setImageBitmap(songList.get(songNum).getCover());
//        Music_Activity.singer_name.setText(songList.get(songNum).getSinger());
//        Music_Activity.count.setText(String.valueOf(songNum));
//        MainActivity.count.setText(String.valueOf(songNum));
//    }


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

    public static String parseJson(String ori_content) throws Exception {
        JSONObject ori_json = new JSONObject(ori_content);
        JSONArray data = ori_json.getJSONArray("data");
        JSONObject realdata = data.getJSONObject(0);
        String url = realdata.getString("url");
        Log.e("realurl",url);
        return url;
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