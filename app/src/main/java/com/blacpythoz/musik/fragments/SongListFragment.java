package com.blacpythoz.musik.fragments;

import android.Manifest;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.blacpythoz.musik.R;
import com.blacpythoz.musik.models.SongModel;
import com.blacpythoz.musik.adapters.SongAdapter;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Random;

public class SongListFragment extends Fragment {

    RecyclerView recyclerView;
    ArrayList<SongModel> songs;
    SongAdapter adapter;
    MediaPlayer mediaPlayer;
    Handler handler=new Handler();
    ImageView actionBtn;
    ProgressBar progressBar;
    TextView currentSong;
    ImageView ivActionSongCoverArt;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.song_list_layout, container, false);

        songs=new ArrayList<>();
        mediaPlayer=new MediaPlayer();
        actionBtn=(ImageView)rootview.findViewById(R.id.iv_action_btn);
        progressBar=(ProgressBar)rootview.findViewById(R.id.pb_song_duration);

        recyclerView=(RecyclerView)rootview.findViewById(R.id.rv_song_list);
        currentSong=(TextView)rootview.findViewById(R.id.tv_current_song_name);
        ivActionSongCoverArt=(ImageView)rootview.findViewById(R.id.iv_action_song_cover);

        adapter=new SongAdapter(songs,getContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
        loadSongs();
        return rootview;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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


    public void playSong(final SongModel song) {
        // set text and image in bottom action bar
        currentSong.setText(song.getSongName());
        Picasso.with(getContext()).load(song.getAlbumArt()).into(ivActionSongCoverArt);
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
        Cursor cursor= getActivity().getContentResolver().query(uri,null,selection,null,sortOrder);
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


}