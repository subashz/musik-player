package com.blacpythoz.musik.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.blacpythoz.musik.R;
import com.blacpythoz.musik.models.SongModel;
import com.blacpythoz.musik.adapters.SongAdapter;
import com.blacpythoz.musik.services.MusicService;

import java.util.ArrayList;

public class SongListFragment extends Fragment {

    RecyclerView recyclerView;
    ArrayList<SongModel> songs;
    SongAdapter adapter;

    //testing services
    private MusicService musicSrv;
    private Intent playIntent;
    ServiceConnection musicConnection;
    private boolean musicBound=false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_song_list, container, false);
        songs=new ArrayList<>();
        recyclerView=(RecyclerView)rootview.findViewById(R.id.rv_song_list);

        adapter=new SongAdapter(songs,getContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

        Log.d("Fragment","onCreateView");

        // if the service is already connected then initialize the players.
        if(musicBound) {
            initPlayer();
        }
        return rootview;
    }

    @Override
    public void onStart() {
        super.onStart();
        if(playIntent==null){
            playIntent = new Intent(getActivity(), MusicService.class);
            playIntent.setAction("");

            getActivity().bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            getActivity().startService(playIntent);
        }
        Log.d("Fragment","onStart()");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
         if(musicBound) {
            getActivity().unbindService(musicConnection);
            musicBound = false;
        }
        Log.d("Fragment","onStop()");
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        musicConnection= new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
                musicSrv = binder.getService();
                initPlayer();
                Log.i("Fragment","Connected to service");
                musicBound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                musicBound = false;
                Log.i("Fragment","ServiceDisconnected");
            }
        };
        Log.d("Fragment","onCreate()");

    }


    public void handleSongClick() {

        //click on song item title and artist name listener
        adapter.setOnSongItemClickListener(new SongAdapter.SongItemClickListener() {
            @Override
            public void onSongItemClick(View v, SongModel song, final int pos) {
                Log.i("Song Clicked is: ",song.getTitle());
                playSong(song);
            }
        });

        // long click on song title and artist listener
        adapter.setOnSongItemLongClickListener(new SongAdapter.SongItemLongClickListener() {

            @Override
            public void onSongItemLongClickListener(View v, SongModel song, int pos) {
                Log.i("Long","CLICKED");
            }
        });

        // song menu click listener
        adapter.setOnSongBtnClickListener(new SongAdapter.SongBtnClickListener() {
            @Override
            public void onSongBtnClickListener(ImageButton btn, View v, final SongModel song, final int pos) {
                final PopupMenu popupMenu=new PopupMenu(getContext(),btn);
                popupMenu.getMenuInflater().inflate(R.menu.song_action_menu,popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if(item.getTitle().equals("Play")) {
                            playSong(song);
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });
    }

    //initialize all the component
    public void initPlayer() {
        songs=musicSrv.getSongs();
        Log.i("GOt songs",songs.get(0).getTitle());
        adapter=new SongAdapter(songs,getContext());
        recyclerView.setAdapter(adapter);
        handleSongClick();
    }

    //make some changes while playing the songs
    public void playSong(SongModel song) {
        musicSrv.play(song);
    }
}