package com.example.muse;

import android.util.Log;

import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Hitokoto_get {
    private static String result;
    public static Hitokoto getHitokoto() throws Exception{
        Hitokoto hitokoto = new Hitokoto();
        String ori_content = connect();
        Log.e("content",ori_content);
        JSONObject ori_json = new JSONObject(ori_content);
        hitokoto.setHitokoto(ori_json.getString("hitokoto"));
        hitokoto.setOrigin(ori_json.getString("from"));
        hitokoto.setCreator(ori_json.getString("creator"));
        hitokoto.setId(ori_json.getString("uuid"));
        hitokoto.setType(ori_json.getString("type"));
        return hitokoto;
    }

    public static String connect(){
        HttpURLConnection con;
        StringBuffer resultBuffer;
        try {
            String hitokoto = "https://v1.hitokoto.cn/";
            URL url = new URL(hitokoto);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setDoOutput(true);
            Log.e("fasongqingbqiu","fasong");
            int responseCode = con.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK){
                InputStream inputStream = con.getInputStream();
                resultBuffer = new StringBuffer();
                byte[] bytes=new byte[1024];
                int len=0;
                while ((len=inputStream.read(bytes))!=-1){
                    resultBuffer.append(new String(bytes,0,len));
                }
                result= resultBuffer.toString();//获得原始字符串
            }
        }catch(Exception e) {
            e.printStackTrace();
        }return result;

    }


}
