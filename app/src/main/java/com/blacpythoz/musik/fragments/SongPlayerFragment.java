package com.blacpythoz.musik.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.blacpythoz.musik.R;
import com.blacpythoz.musik.activities.MusicServiceActivity;
import com.blacpythoz.musik.interfaces.PlayerInterface;
import com.blacpythoz.musik.models.SongModel;
import com.blacpythoz.musik.services.MusicService;
import com.blacpythoz.musik.utils.Helper;

import java.io.FileNotFoundException;
import java.io.IOException;


/**
 * Created by deadsec on 9/20/17.
 */

public class SongPlayerFragment extends MusicServiceFragment {

    public static final String TAG="SongPlayerFragment";

    private SeekBar seekBar;
    private TextView currentSong;
    private TextView currentArtist;

    private ImageView currentCoverArt;
    private ImageView actionBtn;
    private ImageView panelPlayBtn;
    private ImageView panelNextBtn;
    private ImageView panelPrevBtn;

    private BottomSheetBehavior bottomSheetBehavior;
    private ConstraintLayout panelLayout;
    private ConstraintLayout.LayoutParams params;

    private MusicService musicService;
    private boolean musicServiceStatus = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.panel_player_interface, container, false);

        panelLayout =  view.findViewById(R.id.cl_player_interface);
        bottomSheetBehavior = BottomSheetBehavior.from(panelLayout);

        // dp to pixel
        int heightInPixel = Helper.dpToPx(getActivity(), 70);
        bottomSheetBehavior.setPeekHeight(heightInPixel);

        currentSong =  view.findViewById(R.id.tv_panel_song_name);
        currentArtist =  view.findViewById(R.id.tv_panel_artist_name);
        currentCoverArt =  view.findViewById(R.id.iv_pn_cover_art);
        actionBtn =  view.findViewById(R.id.iv_pn_action_btn);
        seekBar =  view.findViewById(R.id.sb_pn_player);

        panelPlayBtn = view.findViewById(R.id.iv_pn_play_btn);
        panelNextBtn = view.findViewById(R.id.iv_pn_next_btn);
        panelPrevBtn = view.findViewById(R.id.iv_pn_prev_btn);

        params = (ConstraintLayout.LayoutParams) currentSong.getLayoutParams();

        if(musicServiceStatus) { updateUI(); handleAllAction(); }

        return view;
    }

    @Override
    public void onServiceConnected(MusicService musicService) {
        this.musicService = musicService;
        musicServiceStatus=true;
        updateUI();
        handleAllAction();
    }

    // done in this methods
    public void handleAllAction() {

        //set default
        actionBtn.setBackgroundResource(R.drawable.ic_media_play);
        //for the action button
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
                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                // b refresests user changed value
                if (musicService != null && b) {
                    // multiply by 1000 is needed, as the value is passed by dividing 1000
                    musicService.seekTo(i * 1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

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
                    panelPlayBtn.animate().rotationX(10).setDuration(500);
                    panelPlayBtn.setBackgroundResource(R.drawable.ic_action_play);
                    musicService.pause();
                } else {
                    musicService.start();
                    panelPlayBtn.animate().rotationX(-10).setDuration(500);
                    panelPlayBtn.setBackgroundResource(R.drawable.ic_action_pause);
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
                params.topMargin = Helper.dpToPx(getActivity(), slideOffset * 30 + 4);
                actionBtn.setAlpha(1 - slideOffset);
                currentCoverArt.setAlpha(slideOffset);
                panelNextBtn.setAlpha(slideOffset);
                panelPlayBtn.setAlpha(slideOffset);
                panelPrevBtn.setAlpha(slideOffset);
                currentSong.setLayoutParams(params);
            }
        });

        //issue with the oncompletion should be solved fast..
        musicService.setCallback(new PlayerInterface.Callback() {
            @Override
            public void onCompletion(SongModel song) {
            }

            @Override
            public void onTrackChange(SongModel song) {
//                seekBar.setProgress(0);
//                seekBar.setMax((int)song.getDuration()/1000);
                Log.i(TAG, "track duration:" + song.getDuration());
                updateUiOnTrackChange(song);
            }

            @Override
            public void onPause() {
                //actionBtn.setBackgroundResource(R.drawable.ic_media_play);
            }
        });
    }

    // progress bar thread on the bottom of the action bar
    private class SongSeekBarThread extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(1000);
                    if (musicService != null) {
                        //    seekBar.setProgress(musicService.getCurrentStreamPosition()/1000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // this updates the ui on music changes in new runnable
    public void updateUiOnTrackChange(final SongModel song) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                currentSong.setText(song.getTitle());
                actionBtn.setBackgroundResource(R.drawable.ic_media_pause);
                panelPlayBtn.setBackgroundResource(R.drawable.ic_action_pause);
                currentArtist.setText(song.getArtistName());
                Uri imageUri = Uri.parse(song.getAlbumArt());
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                } catch (FileNotFoundException e) {
                    bitmap = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.default_main_cover_art);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                currentCoverArt.setImageBitmap(bitmap);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    public void updateUI() {
        if(musicService!=null) {
            SongModel song = musicService.getCurrentSong();
            updateUiOnTrackChange(song);
        }
    }

}
