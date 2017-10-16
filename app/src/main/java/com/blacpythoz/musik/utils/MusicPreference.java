package com.blacpythoz.musik.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by deadsec on 10/15/17.
 */

public class MusicPreference {

    Context mContext;
    SharedPreferences mSharedPreferences;
    public static MusicPreference sMusicPreference;

    private static final String LAST_PLAYED_SONG_ID = "last_played_song_id";
    private static final String LAST_PLAYED_SONG_DURATION = "last_played_song_duration";

    private MusicPreference(Context context) {
        mContext=context;
        mSharedPreferences=PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static MusicPreference get(Context context) {
        if(sMusicPreference==null) {
            sMusicPreference = new MusicPreference(context);
        }
        return sMusicPreference;
    }

    public void setCurrentSongStatus(long songId, long timePlayed) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putLong(LAST_PLAYED_SONG_ID, songId);
        editor.putLong(LAST_PLAYED_SONG_DURATION,timePlayed);
        editor.commit();
    }


    public long getLastPlayedSongId() {
        return mSharedPreferences.getLong(LAST_PLAYED_SONG_ID,0);
    }

    public long getLastPlayedSongDuration() {
        return mSharedPreferences.getLong(LAST_PLAYED_SONG_DURATION,0);
    }
}
