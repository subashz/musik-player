package com.blacpythoz.musik.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.blurry.Blurry;


/**
 * Created by deadsec on 9/20/17.
 */

public class SongPlayerFragment extends MusicServiceFragment {

    public static final String TAG = "SongPlayerFragment";

    private SeekBar seekBar;
    private TextView currentSong;
    private TextView currentArtist;
    private TextView totalTime;
    private TextView remainingTime;

    private CircleImageView currentCoverArt;
    private ImageView currentCoverArtShadow;
    private ImageView actionBtn;
    private ImageView panelPlayBtn;
    private ImageView panelNextBtn;
    private ImageView panelPrevBtn;

    private BottomSheetBehavior bottomSheetBehavior;
    private ConstraintLayout panelLayout;
    private ConstraintLayout.LayoutParams params;

    private MusicService musicService;
    private boolean musicServiceStatus = false;
    private SongSeekBarThread seekBarThread;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        seekBarThread = new SongSeekBarThread();
        seekBarThread.start();
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.panel_player_interface, container, false);

        panelLayout = view.findViewById(R.id.cl_player_interface);
        bottomSheetBehavior = BottomSheetBehavior.from(panelLayout);

//        // dp to pixel
//        int heightInPixel = Helper.dpToPx(getActivity(), 70);
//        bottomSheetBehavior.setPeekHeight(heightInPixel);

        currentSong = view.findViewById(R.id.tv_panel_song_name);
        currentArtist = view.findViewById(R.id.tv_panel_artist_name);
        currentCoverArt = view.findViewById(R.id.iv_pn_cover_art);
        currentCoverArtShadow = view.findViewById(R.id.iv_pn_cover_art_shadow);
        actionBtn = view.findViewById(R.id.iv_pn_action_btn);
        seekBar = view.findViewById(R.id.sb_pn_player);
        totalTime = view.findViewById(R.id.tv_pn_total_time);
        remainingTime = view.findViewById(R.id.tv_pn_remain_time);

        panelPlayBtn = view.findViewById(R.id.iv_pn_play_btn);
        panelNextBtn = view.findViewById(R.id.iv_pn_next_btn);
        panelPrevBtn = view.findViewById(R.id.iv_pn_prev_btn);

        params = (ConstraintLayout.LayoutParams) currentSong.getLayoutParams();

        if (musicServiceStatus) {
            updateUI();
            handleAllAction();
        }

        return view;
    }

    @Override
    public void onServiceConnected(MusicService musicService) {
        this.musicService = musicService;
        musicServiceStatus = true;
        updateUI();
        handleAllAction();
    }

    @Override
    public void onServiceDisconnected() {
        musicServiceStatus = false;
    }

    // done in this methods
    public void handleAllAction() {

        //set default
        if (musicService.isPlaying()) {
            actionBtn.setBackgroundResource(R.drawable.ic_media_pause);
        } else {
            actionBtn.setBackgroundResource(R.drawable.ic_media_play);

        }

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

        if (musicService.isPlaying()) {
            panelPlayBtn.setBackgroundResource(R.drawable.ic_action_pause);
        } else {
            panelPlayBtn.setBackgroundResource(R.drawable.ic_action_play);
        }


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
                params.topMargin = Helper.dpToPx(getActivity(), slideOffset * 30 + 5);
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
                    if (musicServiceStatus) {
                        final String strTotalTime = Helper.toTimeFormat(musicService.getDuration());
                        final String strRemainTIme = Helper.toTimeFormat(musicService.getDuration() - musicService.getCurrentStreamPosition());
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                seekBar.setProgress(musicService.getCurrentStreamPosition() / 1000);
                                totalTime.setText(strTotalTime);
                                remainingTime.setText(strRemainTIme);

                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // this updates the ui on music changes in new runnable
    public void updateUiOnTrackChange(final SongModel song) {
        Thread updateThread = new Thread() {
            Bitmap bitmap = null;

            @Override
            public void run() {
                Uri imageUri = Uri.parse(song.getAlbumArt());
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                } catch (FileNotFoundException e) {
                    bitmap = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.default_main_cover_art);
                } catch (IOException e) {
                    e.printStackTrace();
                }


                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        seekBar.setProgress(0);
                        seekBar.setMax((int) song.getDuration() / 1000);
                        seekBar.setMax((int) song.getDuration() / 1000);
                        if (musicServiceStatus) {
                            seekBar.setProgress(musicService.getCurrentStreamPosition());
                        }
                        currentSong.setText(song.getTitle());
                        actionBtn.setBackgroundResource(R.drawable.ic_media_pause);
                        panelPlayBtn.setBackgroundResource(R.drawable.ic_action_pause);
                        currentArtist.setText(song.getArtistName());
                        currentCoverArt.setImageBitmap(bitmap);
                        Blurry.with(getActivity()).radius(20).sampling(2).from(bitmap).into(currentCoverArtShadow);

                    }
                });
            }
        };
        updateThread.start();

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "SongPlayer Fragment On Pause Called");

        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "SongPlayer Fragment On Resume Called");
        updateUI();
    }

    public void updateUI() {
        if (musicService != null) {
            SongModel song = musicService.getCurrentSong();
            updateUiOnTrackChange(song);
            Log.d(TAG, "updateUI called with current song is: " + song.getTitle());
        }
    }

}
