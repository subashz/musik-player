package com.blacpythoz.musik.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.blacpythoz.musik.R;
import com.blacpythoz.musik.adapters.SectionsPageAdapter;
import com.blacpythoz.musik.fragments.AlbumListFragment;
import com.blacpythoz.musik.fragments.ArtistListFragment;
import com.blacpythoz.musik.fragments.PlayListFragment;
import com.blacpythoz.musik.fragments.SongListFragment;
import com.blacpythoz.musik.interfaces.PlayerInterface;
import com.blacpythoz.musik.models.SongModel;
import com.blacpythoz.musik.services.MusicService;
import com.squareup.picasso.Picasso;

public class PlayerActivity extends AppCompatActivity {
    private SectionsPageAdapter sectionsPageAdapter;
    private ViewPager viewPager;
    private TabLayout tabLayout;

    ImageView actionBtn;
    ProgressBar progressBar;
    TextView currentSong;
    ImageView ivActionSongCoverArt;

    MusicService musicService;
    Intent playIntent;
    boolean boundService =false;
    Thread progressBarThread= new SongProgressBarThread();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_player);
        handleAllView();
    }

    // Service is also created in PlayerActivity so that
    // it can create the notification before unbinding to the service
    // When main activiy gets stopped, it create the notifcation if service is active,
    // so that service can play the music
    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected (ComponentName componentName, IBinder iBinder){
            MusicService.MusicBinder binder = (MusicService.MusicBinder) iBinder;
            musicService = binder.getService();
            // dont show the notification
            handleAllAction();
            musicService.toBackground();
            boundService = true;
        }
        @Override
        public void onServiceDisconnected (ComponentName componentName){ }
    };

    // initialize all the things for services
    @Override
    protected void onStart() {
        super.onStart();
        playIntent = new Intent(this,MusicService.class);
        playIntent.setAction("");
        bindService(playIntent,serviceConnection,Context.BIND_AUTO_CREATE);
        startService(playIntent);
        progressBarThread.start();
    }

    public void handleAllView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        sectionsPageAdapter=new SectionsPageAdapter(getSupportFragmentManager());
        viewPager=(ViewPager)findViewById(R.id.container);
        setupViewPager(viewPager);
        tabLayout=(TabLayout)findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        currentSong=(TextView)findViewById(R.id.tv_current_song_name);
        ivActionSongCoverArt=(ImageView)findViewById(R.id.iv_action_song_cover);
        actionBtn=(ImageView)findViewById(R.id.iv_action_btn);
        progressBar=(ProgressBar)findViewById(R.id.pb_song_duration);
    }

    public void handleAllAction()  {

        actionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(musicService.isPlaying()) {
                    actionBtn.setBackgroundResource(R.drawable.ic_media_play);
                    musicService.pause();
                }else {
                    musicService.start();
                    actionBtn.setBackgroundResource(R.drawable.ic_media_pause);
                }
            }
        });

        // issue with the oncompletion should be solved fast..
        musicService.setCallback(new PlayerInterface.Callback() {
            @Override
            public void onCompletion(SongModel song) {
//                currentSong.setText(song.getSongName());
//                Log.i("Completed","Songs using callback");
//                Picasso.with(getContext()).load(song.getAlbumArt()).into(ivActionSongCoverArt);
            }
            @Override
            public void onTrackChange(SongModel song) {
                currentSong.setText(song.getTitle());
                Picasso.with(getApplicationContext()).load(song.getAlbumArt()).into(ivActionSongCoverArt);
                actionBtn.setBackgroundResource(R.drawable.ic_media_pause);
                progressBar.setMax((int)song.getDuration());
                Log.i("PlayerActivity","OnTrackChange called");
            }

            @Override
            public void onPause() {
                actionBtn.setBackgroundResource(R.drawable.ic_media_play);
            }
        });
    }

    private void setupViewPager(ViewPager viewPager) {
        SectionsPageAdapter adapter=new SectionsPageAdapter(getSupportFragmentManager());
        adapter.addFragment(new SongListFragment(),"All Songs");
        adapter.addFragment(new AlbumListFragment(),"Albums");
        adapter.addFragment(new ArtistListFragment(),"Artist");
        adapter.addFragment(new PlayListFragment(),"PlayList");
        viewPager.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent k=new Intent(this, SettingsActivity.class);
            startActivity(k);
            return true;
        } else if(id==R.id.searchSongItem) {
            Intent search=new Intent(this,SearchActivity.class);
            startActivity(search);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // this is the main for switching the
    // service to the background process
    @Override
    protected void onStop() {
        super.onStop();
        if(boundService) {
            if(musicService.isPlaying()) {
                musicService.toForeground();
            } else {
                stopService(playIntent);
            }
            unbindService(serviceConnection);
            boundService = false;
        }
    }

        // progress bar thread on the bottom of the action bar
    private class SongProgressBarThread extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(1000);
                    if (musicService != null) {
                        progressBar.setProgress(musicService.getCurrentStreamPosition());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // configuration changes
    // for screen orientation
    // to be continu...
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.i("Configuration","Changes");
        if(musicService.isPlaying()) {
            SongModel song = musicService.getCurrentSong();
            currentSong.setText(song.getTitle());
            Picasso.with(getApplicationContext()).load(song.getAlbumArt()).into(ivActionSongCoverArt);
            actionBtn.setBackgroundResource(R.drawable.ic_media_pause);
            //progressBar.setMax((int)song.getDuration());
        }
    }
}

