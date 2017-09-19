package com.blacpythoz.musik.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
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
import com.blacpythoz.musik.utils.Helper;
import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.io.IOException;

import jp.wasabeef.blurry.Blurry;

public class PlayerActivity extends AppCompatActivity {
    private SectionsPageAdapter sectionsPageAdapter;
    private ViewPager viewPager;
    private TabLayout tabLayout;

    ImageView actionBtn;
    SeekBar seekBar;
    TextView currentSong;
    TextView currentArtist;
    ImageView currentCoverArt;

    ImageView panelPlayBtn;
    ImageView panelNextBtn;
    ImageView panelPrevBtn;

    BottomSheetBehavior bottomSheetBehavior;
    ConstraintLayout.LayoutParams params;
    ConstraintLayout panelLayout;
    ImageView panelBackground;

    MusicService musicService;
    Intent playIntent;
    boolean boundService = false;

    boolean seekBarStatus=false;
    Thread seekBarThread= new SongSeekBarThread();

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
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) iBinder;
            musicService = binder.getService();
            // dont show the notification
            handleAllAction();
            musicService.toBackground();
            boundService = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    // initialize all the things for services
    @Override
    protected void onStart() {
        super.onStart();
        playIntent = new Intent(this, MusicService.class);
        playIntent.setAction("");
        bindService(playIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        startService(playIntent);
          seekBarThread.start();
    }

    public void handleAllView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        sectionsPageAdapter = new SectionsPageAdapter(getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.container);
        setupViewPager(viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        View bottomView = findViewById(R.id.cl_player_interface);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomView);

        // dp to pixel
        int heightInPixel = Helper.dpToPx(this, 70);
        bottomSheetBehavior.setPeekHeight(heightInPixel);

        panelLayout = (ConstraintLayout) findViewById(R.id.cl_player_interface);
        panelBackground = (ImageView) findViewById(R.id.iv_panel_background);
        currentSong = (TextView) findViewById(R.id.tv_panel_song_name);
        currentArtist = (TextView) findViewById(R.id.tv_panel_artist_name);
        currentCoverArt = (ImageView) findViewById(R.id.iv_pn_cover_art);
        actionBtn = (ImageView) findViewById(R.id.iv_pn_action_btn);
        seekBar = (SeekBar) findViewById(R.id.sb_pn_player);

        panelPlayBtn = (ImageView) findViewById(R.id.iv_pn_play_btn);
        panelNextBtn = (ImageView) findViewById(R.id.iv_pn_next_btn);
        panelPrevBtn = (ImageView) findViewById(R.id.iv_pn_prev_btn);

        params = (ConstraintLayout.LayoutParams) currentSong.getLayoutParams();
    }

    // all the listeners and action handlers are
    // done in this methods
    public void handleAllAction() {

        // for the action button
        actionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (musicService.isPlaying()) {
                    actionBtn.setBackgroundResource(R.drawable.ic_media_play);
                    musicService.pause();
                } else {
                    musicService.start();
                    actionBtn.setBackgroundResource(R.drawable.ic_media_pause);
                }
            }
        });

        panelLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }
        });

        // for the sliding panel buttons
        panelNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                musicService.playNext();
            }
        });

        panelPrevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                musicService.playPrev();
            }
        });

        panelPlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (musicService.isPlaying()) {
                    panelPlayBtn.setBackgroundResource(R.drawable.ic_action_pause);
                    musicService.pause();
                } else {
                    musicService.start();
                    panelPlayBtn.setBackgroundResource(R.drawable.ic_action_play);
                }
            }
        });

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    panelPlayBtn.animate().rotation(360).setDuration(1000);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // animating the views when panel expanding and collapsing
                params.topMargin = Helper.dpToPx(getApplicationContext(), slideOffset * 30 + 4);
                actionBtn.setAlpha(1 - slideOffset);
                currentCoverArt.setAlpha(slideOffset);
                panelNextBtn.setAlpha(slideOffset);
                panelPlayBtn.setAlpha(slideOffset);
                panelPrevBtn.setAlpha(slideOffset);
                currentSong.setLayoutParams(params);
                panelBackground.setAlpha(slideOffset);
            }
        });

        // issue with the oncompletion should be solved fast..
        musicService.setCallback(new PlayerInterface.Callback() {
            @Override
            public void onCompletion(SongModel song) {
            }

            @Override
            public void onTrackChange(SongModel song) {
                updateUiOnTrackChange(song);
            }

            @Override
            public void onPause() {
                actionBtn.setBackgroundResource(R.drawable.ic_media_play);
            }
        });
    }

    private void setupViewPager(ViewPager viewPager) {
        SectionsPageAdapter adapter = new SectionsPageAdapter(getSupportFragmentManager());
        adapter.addFragment(new SongListFragment(), "All Songs");
        adapter.addFragment(new AlbumListFragment(), "Albums");
        adapter.addFragment(new ArtistListFragment(), "Artist");
        adapter.addFragment(new PlayListFragment(), "PlayList");
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
            Intent k = new Intent(this, SettingsActivity.class);
            startActivity(k);
            return true;
        } else if (id == R.id.searchSongItem) {
            Intent search = new Intent(this, SearchActivity.class);
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
        if (boundService) {
            if (musicService.isPlaying()) {
                musicService.toForeground();
            } else {
                stopService(playIntent);
            }
            unbindService(serviceConnection);
            boundService = false;
        }
    }

    // progress bar thread on the bottom of the action bar
    private class SongSeekBarThread extends Thread {
        @Override
        public void run() {
            try {
                Thread.sleep(1000);
                if (musicService != null) {
                    seekBar.setProgress(musicService.getCurrentStreamPosition());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    // configuration changes
    // for screen orientation
    // to be continu...
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    // this updates the ui on music changes in new runnable
    public void updateUiOnTrackChange(final SongModel song) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                actionBtn.setBackgroundResource(R.drawable.ic_media_pause);
                currentSong.setText(song.getTitle());
                currentArtist.setText(song.getArtistName());
                seekBar.setMax((int)song.getDuration());
                Uri imageUri = Uri.parse(song.getAlbumArt());
                Bitmap bitmap=null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), imageUri);
                } catch (FileNotFoundException e) {
                    bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.default_main_cover_art);
                } catch(IOException e) {
                    e.printStackTrace();
                }
                Blurry.with(getApplicationContext()).from(bitmap).into(panelBackground);
                currentCoverArt.setImageBitmap(bitmap);
            }
        }, 200);
    }
}

