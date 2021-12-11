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
//                    con.setRequestMethod("POST");
//                    con.setDoOutput(true);
                    Log.e("respond",String.valueOf(con.getResponseCode()));
                    if (con.getResponseCode() == 200) {
                        InputStream is = con.getInputStream();//获取输入流
                        FileOutputStream fileOutputStream;//文件输出流
                        if (is != null) {
                            Log.e("下载中","无问题");
//                            ContextWrapper cw = new ContextWrapper(context);
//                            File directory = cw.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
//                            FileUtils fileUtils = new FileUtils();
//                            fileOutputStream = new FileOutputStream(fileUtils.createFile(FileName+".mp3"));
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
//                            Mp3File mp3file = new Mp3File(file.getAbsolutePath());
//                            Bitmap cover = Song_NetInfo.getCover_search(id);
//                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                            cover.compress(Bitmap.CompressFormat.PNG, 100, baos);
//                            ID3v2 id3v2Tag;
//                            if (mp3file.hasId3v2Tag()) {
//                                id3v2Tag = mp3file.getId3v2Tag();
//                            } else {
//                                // mp3 does not have an ID3v2 tag, let's create one..
//                                id3v2Tag = new ID3v24Tag();
//                                mp3file.setId3v2Tag(id3v2Tag);
//                            }
//                            id3v2Tag.setAlbumImage(baos.toByteArray(),"image/jpeg");
//                            mp3file.save(FileName+".wav");





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


//    public static class FileUtils {
//        private String path = Environment.getExternalStorageDirectory().toString() + "/MuseDownload";
//
//        public FileUtils() {
//            File file = new File(path);
//            /**
//             *如果文件夹不存在就创建
//             */
//            if (!file.exists()) {
//                file.mkdirs();
//            }
//        }
//
//        /**
//         * 创建一个文件
//         * @param FileName 文件名
//         * @return
//         */
//        public File createFile(String FileName) {
//            return new File(path, FileName);
//        }
//    }


}
