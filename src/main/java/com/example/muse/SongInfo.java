package com.example.muse;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;

public class SongInfo {

    public static ArrayList<Song> getAllSongs(Context context) {
        MediaMetadataRetriever mediaMetadataRetriever=new MediaMetadataRetriever();

        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, null, null, MediaStore.Audio.AudioColumns.IS_MUSIC);
        ArrayList<Song> songs = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) {
            Song song;
            do {
                String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                mediaMetadataRetriever.setDataSource(path);
                byte[] picture = mediaMetadataRetriever.getEmbeddedPicture();
                Bitmap cover= BitmapFactory.decodeByteArray(picture,0,picture.length);
                song = new Song();
                song.setDatasource(path);
                song.setFileName(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)));
                song.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)));
                song.setDuration(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)));
                song.setSinger(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)));
                song.setAlbum(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)));
                song.setCover(cover);
                if (cursor.getString(6) != null) {
                    song.setYear(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)));
                } else {
                    song.setYear("未知");
                }
                // 歌曲格式
//                if ("audio/mpeg".equals(cursor.getString(7).trim())) {
//                    song.setType("mp3");
//                } else if ("audio/x-ms-wma".equals(cursor.getString(7).trim())) {
//                    song.setType("wma");
//                }
                // 文件大小
//                if (cursor.getString(8) != null) {
//                    float size = cursor.getInt(8) / 1024f / 1024f;
//                    song.setSize((size + "").substring(0, 4) + "M");
//                } else {
//                    song.setSize("未知");
//                }
                songs.add(song);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return songs;
    }
}
