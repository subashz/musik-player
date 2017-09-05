package com.blacpythoz.musik.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SearchView;

import com.blacpythoz.musik.R;
import com.blacpythoz.musik.adapters.SongAdapter;
import com.blacpythoz.musik.models.SongModel;
import com.blacpythoz.musik.services.MusicService;

import java.util.ArrayList;

/**
 * Created by deadsec on 9/3/17.
 */

public class SearchActivity  extends AppCompatActivity{

    RecyclerView recyclerView;
    SongAdapter adapter;
    ArrayList<SongModel> songs;
    MusicService musicService;
    boolean boundStatus=false;
    Intent serviceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        songs = new ArrayList<>();
        adapter = new SongAdapter(songs,this);
        recyclerView = (RecyclerView)findViewById(R.id.rv_search_song_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        serviceIntent = new Intent(this,MusicService.class);
        serviceIntent.setAction("");
        bindService(serviceIntent,serviceConnection, Context.BIND_AUTO_CREATE);
        startService(serviceIntent);
    }

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) iBinder;
            musicService=binder.getService();
            boundStatus=true;
            initSearch();

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            boundStatus=false;
        }
    };

    public void initSearch() {
        songs = musicService.getSongs();
        adapter = new SongAdapter(songs,this);
        recyclerView.setAdapter(adapter);
        handleSongClick();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = this.getMenuInflater();
        menuInflater.inflate(R.menu.search_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.searchSongItem);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setIconified(false);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
              adapter.filter(newText);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    public void handleSongClick() {

        //click on song item title and artist name listener
        adapter.setOnSongItemClickListener(new SongAdapter.SongItemClickListener() {
            @Override
            public void onSongItemClick(View v, SongModel song, final int pos) {
                musicService.play(song);
            }
        });

        // long click on song title and artist listener
        adapter.setOnSongItemLongClickListener(new SongAdapter.SongItemLongClickListener() {

            @Override
            public void onSongItemLongClickListener(View v, SongModel song, int pos) {
            }
        });

        // song menu click listener
        adapter.setOnSongBtnClickListener(new SongAdapter.SongBtnClickListener() {
            @Override
            public void onSongBtnClickListener(ImageButton btn, View v, final SongModel song, final int pos) {
                final PopupMenu popupMenu=new PopupMenu(getApplicationContext(),btn);
                popupMenu.getMenuInflater().inflate(R.menu.song_action_menu,popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if(item.getTitle().equals("Play")) {
                            musicService.play(song);
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });
    }
}
