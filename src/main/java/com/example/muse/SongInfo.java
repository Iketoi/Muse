package com.example.muse;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

public class SongInfo {

    public static ArrayList<Song> getAllSongs(Context context) throws Exception {


        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, null, null, MediaStore.Audio.AudioColumns.IS_MUSIC);
        ArrayList<Song> songs = new ArrayList<>();
        MediaMetadataRetriever mediaMetadataRetriever=new MediaMetadataRetriever();
        if (cursor != null && cursor.moveToFirst()) {
            Song song;
            do {
                String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                if(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE))!=null){
                    song = new Song();
                    song.setDatasource(path);
                    song.setFileName(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)));
                    song.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)));
                    song.setDuration(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)));
                    song.setSinger(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)));
                    song.setAlbum(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)));
                    if (song.getTitle().contains("-")) {
                        String[] str = song.getTitle().split("-");
                        song.setSinger(str[0]);
                        if(str[1].startsWith(" ")){
                            str[1]=str[1].trim();
                        }
                        song.setTitle(str[1]);
                    }
                    Uri uri = Uri.parse(path);
                    try{
                        mediaMetadataRetriever.setDataSource(context,uri);
                        byte[] picture = mediaMetadataRetriever.getEmbeddedPicture();
                        Bitmap cover= BitmapFactory.decodeByteArray(picture,0,picture.length);
                        song.setCover(cover);
                    }catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }


//
                    songs.add(song);
                }


            } while (cursor.moveToNext());
            mediaMetadataRetriever.close();
            cursor.close();
        }
        return songs;
    }
}
