package com.blacpythoz.musik.database;

import android.database.Cursor;
import android.database.CursorWrapper;
import android.provider.MediaStore;

import com.blacpythoz.musik.models.SongModel;

/**
 * Created by deadsec on 10/14/17.
 */

public class SongCursorWrapper extends CursorWrapper {
    public SongCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public SongModel getSong() {
        int id = getInt(getColumnIndex(MediaStore.Audio.Media._ID));
        String title = getString(getColumnIndex(MediaStore.Audio.Media.TITLE));
        String artistName = getString(getColumnIndex(MediaStore.Audio.Media.ARTIST));
        String composer = getString(getColumnIndex(MediaStore.Audio.Media.COMPOSER));
        String albumName = getString(getColumnIndex(MediaStore.Audio.Media.ALBUM));
        String data = getString(getColumnIndex(MediaStore.Audio.Media.DATA));
        int trackNumber = getInt(getColumnIndex(MediaStore.Audio.Media.TRACK));
        int year = getInt(getColumnIndex(MediaStore.Audio.Media.YEAR));
        long duration = getLong(getColumnIndex(MediaStore.Audio.Media.DURATION));
        long dateModified = getLong(getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED));
        long dateAdded = getLong(getColumnIndex(MediaStore.Audio.Media.DATE_ADDED));
        int albumId = getInt(getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
        int artistId = getInt(getColumnIndex(MediaStore.Audio.Media.ARTIST_ID));
        long bookmark = getLong(getColumnIndex(MediaStore.Audio.Media.BOOKMARK));
        return new SongModel(id,title,artistName,composer,albumName,"",data,trackNumber,year,duration, dateModified,dateAdded,albumId,artistId,bookmark);
    };
}
