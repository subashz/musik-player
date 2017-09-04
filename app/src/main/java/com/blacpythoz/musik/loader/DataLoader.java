package com.blacpythoz.musik.loader;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.blacpythoz.musik.models.AlbumModel;
import com.blacpythoz.musik.models.ArtistModel;
import com.blacpythoz.musik.models.SongModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by deadsec on 9/4/17.
 */

public class DataLoader {

    public static ArrayList<SongModel> getSongs(Context context) {

        ArrayList<SongModel> songs = new ArrayList<>();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cursor = context.getContentResolver().query(uri, null, selection, null, sortOrder);
        Uri albumArtUri = Uri.parse("content://media/external/audio/albumart");

        cursor.moveToFirst();
        do {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE_KEY));
            String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            String artistName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            String composer = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            String albumName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
            String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            int trackNumber = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.TRACK));
            int year = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.YEAR));
            long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
            long dateModified = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED));
            long dateAdded = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED));
            int albumId = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
            int artistId = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID));
            long bookmark = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.BOOKMARK));

            Uri albumArt = ContentUris.withAppendedId(albumArtUri, albumId);

            songs.add(new SongModel(id,title,artistName,composer,albumName,albumArt.toString(),
                         data,trackNumber,year,duration,dateModified,dateAdded,albumId,artistId,
                         bookmark));

        } while (cursor.moveToNext());
        cursor.close();

        return songs;
    }

    // incomplete get artist context
    public static ArrayList<AlbumModel>  getAlbums(Context context) {
        ArrayList<SongModel> songs = getSongs(context);

        // Organize song as: {albumId=>{song,song,song},albumId=>{song,song}}
        Map<Integer,ArrayList<SongModel>> albumMap=new HashMap<>();
        ArrayList<AlbumModel> allAlbums = new ArrayList<>();
        for(SongModel song: songs) {
            if (albumMap.get(song.getAlbumId()) == null) {
                albumMap.put(song.getAlbumId(), new ArrayList<SongModel>());
                albumMap.get(song.getAlbumId()).add(song);
            } else {
                albumMap.get(song.getAlbumId()).add(song);
            }
        }

        // Extracting and mapping the songlist
        for (Map.Entry<Integer, ArrayList<SongModel>> entry : albumMap.entrySet()) {
            ArrayList<SongModel> albumSpecificSongs = new ArrayList<>();
            for(SongModel song: entry.getValue()) {
                albumSpecificSongs.add(song);
            }
            allAlbums.add(new AlbumModel(albumSpecificSongs));
        }

//          Debugging Code
//        for(AlbumModel k:allAlbums) {
//            for(SongModel song: k.getAlbumSongs()) {
//                Log.i("Test","Album Title: "+song.getAlbumName()+"Album ID: "+song.getAlbumId()+" Song Name: "+song.getTitle());
//            }
//        }
//        Log.i("All Albums list: ",allAlbums.size()+"");

        return allAlbums;
    }

}
