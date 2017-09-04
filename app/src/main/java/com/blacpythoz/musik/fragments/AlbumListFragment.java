package com.blacpythoz.musik.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blacpythoz.musik.R;
import com.blacpythoz.musik.adapters.AlbumAdapter;
import com.blacpythoz.musik.models.AlbumModel;
import com.blacpythoz.musik.services.MusicService;

import java.util.ArrayList;

/**
 * Created by deadsec on 9/3/17.
 */

public class AlbumListFragment extends Fragment {

    MusicService musicService;
    Intent playIntent;
    boolean serviceBound;
    ArrayList<AlbumModel> albums;
    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;

   ServiceConnection serviceConnection = new ServiceConnection() {
       @Override
       public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
           MusicService.MusicBinder binder = (MusicService.MusicBinder)iBinder;
           musicService=binder.getService();
           serviceBound=true;
           initPlayer();
       }

       @Override
       public void onServiceDisconnected(ComponentName componentName) {
            serviceBound=false;
       }
   };

   public void initPlayer() {
       albums = musicService.getAlbums();
       adapter = new AlbumAdapter(albums,getContext());
       recyclerView.setAdapter(adapter);

   }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        playIntent = new Intent(getActivity(),MusicService.class);
        playIntent.setAction("");
        getActivity().bindService(playIntent,serviceConnection, Context.BIND_AUTO_CREATE);
        getActivity().startService(playIntent);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_album_list,container,false);
        recyclerView = (RecyclerView)rootView.findViewById(R.id.rv_album_list);
        albums = new ArrayList<>();
        adapter = new AlbumAdapter(albums,getContext());
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(),2));
        recyclerView.setAdapter(adapter);



        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unbindService(serviceConnection);
    }
}
