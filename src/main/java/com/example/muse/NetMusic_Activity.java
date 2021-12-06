package com.example.muse;

import static java.lang.Integer.parseInt;

import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class NetMusic_Activity  extends AppCompatActivity implements View.OnClickListener{
    private static SeekBar bar;
    private static TextView bar_progress,bar_total;
    public static TextView song_name,singer_name;
    public static ImageView iv_music;
    public static ImageButton btn_loop;
    public static ObjectAnimator animator;
    public static NetMusicService.MusicControl musicControl;
    private static String result;
    private String myurl = "http://139.196.76.67:3000/song/url?id=";
    private String infourl = "http://139.196.76.67:3000/song/detail?ids=";
    private final String r_type = "POST";
    private String musicurl;
    private String[] songInfo;
    private static Bitmap cover;
    String name;
    Intent intent1,intent2;
    NetMusic_Activity.MyServiceConn conn;
    NetMusic_Activity.MyServiceConn_fa conn1;
    NetMusic_Activity.MyServiceConn_stopAni conn2;
    private boolean isUnbind =false;//记录服务是否被解绑
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_netmusic);
        intent1=getIntent();
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void init() throws Exception{
        bar_progress=findViewById(R.id.bar_progress);
        bar_total=findViewById(R.id.bar_total);
        bar=findViewById(R.id.bar);
        song_name=findViewById(R.id.song_name);
        singer_name=findViewById(R.id.singer_name);
        iv_music=findViewById(R.id.iv_music);
        btn_loop = findViewById(R.id.btn_loop);
        findViewById(R.id.btn_exit).setOnClickListener(this);
//        findViewById(R.id.btn_prev).setOnClickListener(this);
//        findViewById(R.id.btn_next).setOnClickListener(this);
        findViewById(R.id.btn_loop).setOnClickListener(this);
        findViewById(R.id.btn_porc).setOnClickListener(this);
//        findViewById(R.id.btn_shuf).setOnClickListener(this);

        animator=ObjectAnimator.ofFloat(iv_music,"rotation",0f,360.0f);
        animator.setDuration(10000);//动画旋转一周的时间为10秒
        animator.setInterpolator(new LinearInterpolator());//匀速
        animator.setRepeatCount(-1);//-1表示设置动画无限循环

        Intent intent = getIntent();
        String id = intent.getStringExtra("id");
        try {
            myurl = myurl+id;
            infourl = infourl+id;
            Log.e("id",id);
            Log.e("myurl",myurl);
            Log.e("infourl",infourl);
            String music = connect(myurl,r_type);
            String songinfo = connect(infourl,r_type);
            musicurl= parseJson_songurl(music);
            songInfo = parseJson_songinfo(songinfo);
            URL url = new URL(songInfo[2]);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(5000);
            con.setRequestMethod("GET");
            if (con.getResponseCode() == 200) {
                InputStream inputStream = con.getInputStream();
                cover = BitmapFactory.decodeStream(inputStream);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        if(conn==null){
            if(getIntent().hasExtra("error")){//从主页点击重新连接服务
                Log.e("主页点击","已恢复连接");
                iv_music.setImageBitmap(cover);
                MainActivity.iv_music.setImageBitmap(cover);
                song_name.setText(songInfo[0]);
                singer_name.setText(songInfo[1]);
                intent2=new Intent(this,NetMusicService.class);
                if(getIntent().hasExtra("stopAni")){//歌曲暂停时从主页点击，重新连接服务并重新设置animator
                    conn2 = new NetMusic_Activity.MyServiceConn_stopAni();
                    bindService(intent2,conn2,BIND_AUTO_CREATE);
                    Log.e("conn2","已连接服务");
                }else{
                    conn1=new NetMusic_Activity.MyServiceConn_fa();
                    bindService(intent2,conn1,BIND_AUTO_CREATE);
                }
            }
            else{
                song_name.setText(songInfo[0]);
                MainActivity.song_name.setText(songInfo[0]);
                singer_name.setText(songInfo[1]);
                iv_music.setImageBitmap(cover);
                MainActivity.iv_music.setImageBitmap(cover);

                intent2=new Intent(this,NetMusicService.class);
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

    public static String parseJson_songurl(String ori_content) throws Exception {
        JSONObject ori_json = new JSONObject(ori_content);
        JSONArray data = ori_json.getJSONArray("data");
        JSONObject realdata = data.getJSONObject(0);
        String url = realdata.getString("url");
        return url;
    }

    public static String[] parseJson_songinfo(String ori_content) throws Exception {
        String[] songInfo = new String[3];
        JSONObject ori_json = new JSONObject(ori_content);
        JSONArray data = ori_json.getJSONArray("songs");
        JSONObject realdata = data.getJSONObject(0);
        songInfo[0]= realdata.getString("name");
        JSONArray art = realdata.getJSONArray("ar");
        JSONObject artist = art.getJSONObject(0);
        songInfo[1]= artist.getString("name");
        JSONObject all = realdata.getJSONObject("al");
        songInfo[2]= all.getString("picUrl");
        return songInfo;
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
            musicControl.play(musicurl);
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

    public static Handler handler=new Handler(){//创建消息处理器对象
        //在主线程中处理从子线程发送过来的消息
        @Override
        public void handleMessage(Message msg){
            Bundle bundle=msg.getData();//获取从子线程发送过来的音乐播放进度
            int duration=bundle.getInt("duration");
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
    };


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

        switch (v.getId()) {
            case R.id.btn_porc:
                try {
                    musicControl.porc();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_exit:
                isUnbind = true;
                unbind(isUnbind);
                finish();
                break;
//            case R.id.btn_prev:
//                musicControl.prev(position);
//                Log.e("position",String.valueOf(position));
//                animator.start();
//                MainActivity.animator.start();
//                break;
//            case R.id.btn_next:
//                musicControl.next(position);
//                Log.e("position",String.valueOf(position));
//                animator.start();
//                MainActivity.animator.start();
//                break;
            case R.id.btn_loop:
                musicControl.setLooping();
                break;
//            case R.id.btn_shuf:
//                musicControl.shuffle();
//                break;
        }
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
//        unbind(isUnbind);//解绑服务
    }

}
