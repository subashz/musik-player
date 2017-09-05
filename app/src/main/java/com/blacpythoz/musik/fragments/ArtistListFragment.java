package com.blacpythoz.musik.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blacpythoz.musik.R;
import com.blacpythoz.musik.adapters.ArtistAdapter;
import com.blacpythoz.musik.models.ArtistModel;
import com.blacpythoz.musik.services.MusicService;

import java.util.ArrayList;

/**
 * Created by deadsec on 9/3/17.
 */

public class ArtistListFragment extends Fragment {

    // to be continue
    Intent playIntent;
    MusicService musicService;
    boolean serviceBound = false;
    RecyclerView recyclerView;
    ArtistAdapter artistAdapter;
    ArrayList<ArtistModel> artists;
    ServiceConnection serviceConnection;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                MusicService.MusicBinder binder = (MusicService.MusicBinder) iBinder;
                musicService = binder.getService();
                serviceBound = true;
                initPlayer();
            }
            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                serviceBound = false;
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        if (playIntent == null) {
            playIntent = new Intent(getContext(), MusicService.class);
            playIntent.setAction("");
            getActivity().bindService(playIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            getActivity().startService(playIntent);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_artist_list,container,false);
        recyclerView = (RecyclerView) view.findViewById(R.id.rv_artist_list);
        artists = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        artistAdapter = new ArtistAdapter(artists,getContext());
        if(serviceBound) {
            initPlayer();
        }
        return view;
    }

    public void initPlayer() {
            artists = musicService.getArtists();
            artistAdapter = new ArtistAdapter(artists, getContext());
            recyclerView.setAdapter(artistAdapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(serviceBound) {
            getActivity().unbindService(serviceConnection);
            serviceBound = false;
        }
    }
}
