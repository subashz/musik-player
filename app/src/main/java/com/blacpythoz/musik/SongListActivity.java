package com.blacpythoz.musik;

import android.Manifest;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class SongListActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<SongModel> songs;
    SongAdapter adapter;
    boolean permissionStat = false;
    MediaPlayer mediaPlayer;
    Handler handler=new Handler();
    ImageView actionBtn;
    ProgressBar progressBar;
    TextView currentSong;
    ImageView ivActionSongCoverArt;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.song_list_layout);
        songs=new ArrayList<>();
        mediaPlayer=new MediaPlayer();
        actionBtn=(ImageView)findViewById(R.id.iv_action_btn);
        progressBar=(ProgressBar)findViewById(R.id.pb_song_duration);

        recyclerView=(RecyclerView)findViewById(R.id.rv_song_list);
        currentSong=(TextView)findViewById(R.id.tv_current_song_name);
        ivActionSongCoverArt=(ImageView)findViewById(R.id.iv_action_song_cover);

        adapter=new SongAdapter(songs,getApplicationContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

        checkPermission();
        if(permissionStat) {
            loadSongs();
        }

    }

    public void handleActionListener() {
        actionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Randomly play the song if the Media Player
                // has no length i.e this is the first launch
                if(mediaPlayer.getDuration()<=0) {
                    Random random = new Random();
                     int pos=random.nextInt(songs.size()-1);
                       Log.i("Playing",songs.get(pos).getSongName());
                      playSong(songs.get(pos));
                }
                if(mediaPlayer.isPlaying()) {
                    actionBtn.setBackgroundResource(R.drawable.ic_media_play);
                    mediaPlayer.pause();
                }else {
                    mediaPlayer.start();
                    actionBtn.setBackgroundResource(R.drawable.ic_media_pause);
                }
            }
        });



    }

    public void handleSongClick() {

        //click on song item title and artist name listener
        adapter.setOnSongItemClickListener(new SongAdapter.SongItemClickListener() {
            @Override
            public void onSongItemClick(View v, SongModel song, int pos) {
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
            public void onSongBtnClickListener(ImageButton btn, View v, final SongModel song, int pos) {
                final PopupMenu popupMenu=new PopupMenu(SongListActivity.this,btn);
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


    public void playSong(final SongModel song) {
        // set text and image in bottom action bar
        currentSong.setText(song.getSongName());
        Picasso.with(getApplicationContext()).load(song.getAlbumArt()).into(ivActionSongCoverArt);
        actionBtn.setBackgroundResource(R.drawable.ic_media_pause);


        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                        mediaPlayer.reset();
//                        mediaPlayer.release();
                        mediaPlayer = null;
                        mediaPlayer = new MediaPlayer();
                    }
                    mediaPlayer.setDataSource(song.getSongUrl());
                    mediaPlayer.prepareAsync();
                    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mediaPlayer) {
                            mediaPlayer.start();
                            progressBar.setProgress(0);
                            progressBar.setMax(mediaPlayer.getDuration());
                        }
                    });

                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        public void onCompletion(MediaPlayer mp) {
                            Log.i("Completed","Music is completed");
                            Log.i("Completed",(mp.getDuration()+" "));
                            Random random = new Random();
                            int pos=random.nextInt(songs.size()-1);
                            Log.i("Playing",songs.get(pos).getSongName());
                            playSong(songs.get(pos));
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        handler.postDelayed(r,1000);

    }


    public void loadSongs() {
        Uri uri= MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection=MediaStore.Audio.Media.IS_MUSIC+"!=0";
        String sortOrder=MediaStore.Audio.Media.TITLE+" ASC";
        Cursor cursor= getContentResolver().query(uri,null,selection,null,sortOrder);
        int songNameColumnIndex=cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
        int artistNameColumnIndex=cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
        int songUriIndex=cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
        // album arts
         Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
        int songAlbumIndex=cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);

        cursor.moveToFirst();
        do{
            String songName = cursor.getString(songNameColumnIndex);
            String artistName=cursor.getString(artistNameColumnIndex);
            String albumArt=cursor.getString(songAlbumIndex);
            // for album arts
            Uri artUri = ContentUris.withAppendedId(sArtworkUri,Long.parseLong(albumArt));
            String songUri=cursor.getString(songUriIndex);

            songs.add(new SongModel(songName,artistName,songUri,artUri.toString()));
        }while(cursor.moveToNext());
        cursor.close();
        adapter.notifyDataSetChanged();
        handleSongClick();
        handleActionListener();
        Thread t = new SongProgressBarThread();
        t.start();
    }

    public void checkPermission() {
        if(Build.VERSION.SDK_INT >= 23) {
            if(ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},123);
            } else {
                permissionStat=true;
            }
        } else {
            permissionStat=true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==123) {
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED) {
                permissionStat=true;
                loadSongs();
            } else {
                permissionStat=false;
            }
        }else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private class SongProgressBarThread extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(1000);
                    if (mediaPlayer != null) {
                        progressBar.setProgress(mediaPlayer.getCurrentPosition());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.songmenu,menu);

        MenuItem menuItem=menu.findItem(R.id.searchSongItem);
        SearchView searchView = (SearchView)menuItem.getActionView();

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
}