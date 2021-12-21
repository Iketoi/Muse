package com.example.muse;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v24Tag;
import com.mpatric.mp3agic.Mp3File;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadUtil {
    private String path = Environment.getExternalStorageDirectory().toString() + "/MuseDownload";
    public static void downLoad(Context context, String url_s, String FileName, String id) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Looper.prepare();
                    Toast.makeText(context,"开始下载",Toast.LENGTH_SHORT).show();
                    URL url = new URL(url_s);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    Log.e("respond",String.valueOf(con.getResponseCode()));
                    if (con.getResponseCode() == 200) {
                        InputStream is = con.getInputStream();//获取输入流
                        FileOutputStream fileOutputStream;//文件输出流
                        if (is != null) {
                            Log.e("下载中","无问题");
                            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
                                    .toString();
                            File directory = new File(path,"MuseDownload");
                            if(!directory.exists()){
                                directory.mkdir();
                            }
                            Log.e("file",String.valueOf(directory));
                            File file = new File(directory, FileName+".mp3");
                            Log.e("下载中","创建文件");
                            fileOutputStream = new FileOutputStream(file);
                            byte[] buf = new byte[1024];
                            int len;
                            while ((len = is.read(buf)) != -1) {
                                fileOutputStream.write(buf, 0, len);//将获取到的流写入文件中
                            }
                            is.close();
                            fileOutputStream.flush();
                            fileOutputStream.close();
                            Toast.makeText(context,"下载完成",Toast.LENGTH_SHORT).show();
                        }
                    }
                    Looper.loop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
