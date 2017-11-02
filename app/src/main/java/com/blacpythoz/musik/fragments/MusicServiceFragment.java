package com.blacpythoz.musik.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.blacpythoz.musik.services.MusicService;

/**
 * Created by deadsec on 9/20/17.
 * This class makes a service fragments, so that other fragments
 * can extend this, and gets the music service.
 * The onconnected is callback function ho. .
 */

public abstract class MusicServiceFragment extends Fragment {
    public static final String TAG = "MusicServiceFragment";
    ServiceConnection serviceConnection;
    public MusicService musicService;
    Intent playIntent;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                MusicService.MusicBinder binder = (MusicService.MusicBinder)iBinder;
                musicService = binder.getService();
                MusicServiceFragment.this.onServiceConnected(musicService);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                Log.d(TAG,"onServiceDisconnected");
                MusicServiceFragment.this.onServiceDisconnected();
            }
        };
    }

    // this acts like a callback.
    // mug. yo function gets called after service connected
    // so it must be override
    public abstract void onServiceConnected(MusicService musicService);
    public abstract void onServiceDisconnected();

    @Override
    public void onStart() {
        super.onStart();
        playIntent = new Intent(getActivity(), MusicService.class);
        playIntent.setAction("");
        getActivity().bindService(playIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        getActivity().startService(playIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onStop()");
        MusicServiceFragment.this.onServiceDisconnected();
        getActivity().stopService(playIntent);
        getActivity().unbindService(serviceConnection);

        }
    }
