package com.blacpythoz.musik.interfaces;
import com.blacpythoz.musik.models.SongModel;

import java.util.ArrayList;

/**
 * Created by deadsec on 9/3/17.
 */

public interface PlayerInterface {

    void start();
    void play(long songId);
    void play(SongModel song);
    void pause();
    void stop();
    void seekTo(int position);
    boolean isPlaying();
    long getDuration();
    int getCurrentStreamPosition();
    void setCallback(Callback callback);

    interface Callback {
        void onCompletion(SongModel song);
        void onTrackChange(SongModel song);
        void onPause();
    }
}

