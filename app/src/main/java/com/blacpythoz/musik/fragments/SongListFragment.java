package com.blacpythoz.musik.fragments;

import android.support.annotation.Nullable;
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
import java.util.List;

public class SongListFragment extends MusicServiceFragment {

    public static final String TAG="SongListFragment";

    RecyclerView recyclerView;
    List<SongModel> songs;
    SongAdapter adapter;
    private MusicService musicSrv;
    boolean musicServiceStatus = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_song_list, container, false);
        songs=new ArrayList<>();
        recyclerView=rootview.findViewById(R.id.rv_song_list);
        adapter=new SongAdapter(songs,getContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
        if(musicServiceStatus) { initFragment(); }
        return rootview;
    }

    @Override
    public void onServiceConnected(MusicService musicService) {
        musicSrv = musicService;
        musicServiceStatus=true;
        initFragment();
    }

    @Override
    public void onServiceDisconnected() {

    }

    public void handleSongClick() {

        //click on song item title and artist name listener
        adapter.setOnSongItemClickListener(new SongAdapter.SongItemClickListener() {
            @Override
            public void onSongItemClick(View v, SongModel song, final int pos) {
                Log.d(TAG,song.getTitle());
                playSong(song);
            }
        });

        // long click on song title and artist listener
        adapter.setOnSongItemLongClickListener(new SongAdapter.SongItemLongClickListener() {

            @Override
            public void onSongItemLongClickListener(View v, SongModel song, int pos) {
                Log.d(TAG,"onsongitemclick listener testing");
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
    public void initFragment() {
        songs=musicSrv.getSongs();
        Log.d(TAG,songs.get(0).getTitle());
        adapter=new SongAdapter(songs,getContext());
        recyclerView.setAdapter(adapter);
        handleSongClick();
    }

    //make some changes while playing the songs
    public void playSong(SongModel song) {
        musicSrv.play(song);
    }

}