package com.blacpythoz.musik.interfaces;
import com.blacpythoz.musik.models.SongModel;

import java.util.ArrayList;

/**
 * Created by deadsec on 9/3/17.
 */

public interface PlayBackInterface {

    void start();

    void stop();

    void setState(int state);

    int getState();

    boolean isPlaying();

    int getCurrentStreamPosition();

    long getDuration();

    void setCurrentStreamPosition(int pos);

    void play(int pos);

    void play(SongModel song);

    void pause();

    void seekTo(int position);

    interface Callback {
        void onCompletion(SongModel song);

        void onPlaybackStatusChanged(int state);

        void onError(String error);
    }

    void setCallback(Callback callback);
    void loadMedia();
    ArrayList<SongModel> getSongs();

}

