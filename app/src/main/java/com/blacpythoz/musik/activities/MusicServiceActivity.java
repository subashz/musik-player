package com.blacpythoz.musik.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.blacpythoz.musik.services.MusicService;

/**
 * Created by deadsec on 9/20/17.
 */

public abstract class MusicServiceActivity extends PermitActivity {
    public static final String TAG = MusicServiceActivity.class.getSimpleName();
    public static final String PLAY_STATE = "play_state";
    public static final String SONG_ID = "song_id";
    public static final String DURATION = "duration";
    ServiceConnection serviceConnection;
    boolean isPlaying = false;
    public static MusicService musicService;
    boolean boundService = false;
    Intent playIntent;
    MusicService.MusicBinder binder;

    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate of "+TAG);
        if (savedInstanceState != null) {
            isPlaying = savedInstanceState.getBoolean(PLAY_STATE);
        }
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                binder = (MusicService.MusicBinder) iBinder;
                musicService = binder.getService();
                MusicServiceActivity.this.onServiceConnected();

                if(isPlaying) {
                    musicService.play(savedInstanceState.getLong(SONG_ID));
                    musicService.seekTo(savedInstanceState.getInt(DURATION));
                    musicService.toBackground();
                }
                boundService = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };
    }

    // this acts like a callback.
    // mug. yo function gets called after service connected
    // so it must be override
    abstract public void onServiceConnected();

    @Override
    protected void onStart() {
        super.onStart();
        playIntent = new Intent(this, MusicService.class);
        playIntent.setAction("");
        bindService(playIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        startService(playIntent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStopCalled() of "+TAG);
        runServiceIfSongIsPlaying();
    }

    void runServiceIfSongIsPlaying() {
        Log.d(TAG,"runServiceIfSUngIsPlaying");
        if (boundService) {
            Log.d(TAG,"boundService");
            if (musicService.isPlaying() || isChangingConfigurations()) {
                Log.d(TAG,"toForeground");
                musicService.toForeground();
            } else {
                    stopService(playIntent);
            }
            unbindService(serviceConnection);
            //tesing
            binder = null;
            boundService = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onDestroy of "+TAG);
        runServiceIfSongIsPlaying();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(PLAY_STATE, musicService.isPlaying());
        outState.putLong(SONG_ID,musicService.getCurrentSong().getId());
        outState.putInt(DURATION,musicService.getCurrentStreamPosition());
    }
}
