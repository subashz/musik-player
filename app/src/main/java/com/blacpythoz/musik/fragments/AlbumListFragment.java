package com.blacpythoz.musik.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blacpythoz.musik.R;
import com.blacpythoz.musik.adapters.AlbumAdapter;
import com.blacpythoz.musik.models.AlbumModel;
import com.blacpythoz.musik.services.MusicService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by deadsec on 9/3/17.
 */

public class AlbumListFragment extends MusicServiceFragment {

    private final String TAG="AlbumListFragment";
    private MusicService musicService;
    private boolean musicServiceStatus = false;
    private List<AlbumModel> albums;
    private RecyclerView recyclerView;
    private AlbumAdapter adapter;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_album_list, container, false);
        recyclerView = rootView.findViewById(R.id.rv_album_list);
        albums = new ArrayList<>();
        adapter = new AlbumAdapter(albums, getContext());
        recyclerView.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);
        if (musicServiceStatus) { initFragment(); }
        return rootView;
    }

    @Override
    public void onServiceConnected(MusicService musicService) {
       this.musicService = musicService;
        musicServiceStatus=true;
        initFragment();
    }


    public void initFragment() {
        albums = musicService.getAlbums();
        adapter = new AlbumAdapter(albums, getContext());
        recyclerView.setAdapter(adapter);
        handleAllListener();
    }

    public void handleAllListener() {
        adapter.setAlbumClickListener(new AlbumAdapter.AlbumClickListener() {
            @Override
            public void OnAlbumClickListener(View v, AlbumModel album, int pos) {
                Log.d(TAG, album.getArtistName());
            }
        });
    }

}
