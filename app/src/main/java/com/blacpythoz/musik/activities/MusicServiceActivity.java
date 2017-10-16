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
    ServiceConnection serviceConnection;
    public static MusicService musicService;
    boolean boundService = false;
    Intent playIntent;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                MusicService.MusicBinder binder = (MusicService.MusicBinder) iBinder;
                musicService = binder.getService();
                MusicServiceActivity.this.onServiceConnected();
                binder.getService().toBackground();
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
        Log.i(TAG, "onStopCalled()");
        runServiceIfSongIsPlaying();
    }

    void runServiceIfSongIsPlaying() {
        if (boundService) {
            if (musicService.isPlaying()) {
                musicService.toForeground();
            } else {
                stopService(playIntent);
            }
            unbindService(serviceConnection);
            boundService = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroyCalled");
    }
}
