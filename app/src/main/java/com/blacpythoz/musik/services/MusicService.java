package com.blacpythoz.musik.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;

import com.blacpythoz.musik.R;
import com.blacpythoz.musik.activities.PermitActivity;
import com.blacpythoz.musik.activities.PlayerActivity;
import com.blacpythoz.musik.interfaces.PlayerInterface;
import com.blacpythoz.musik.loader.SongDataLab;
import com.blacpythoz.musik.models.AlbumModel;
import com.blacpythoz.musik.models.ArtistModel;
import com.blacpythoz.musik.models.SongModel;
import com.blacpythoz.musik.utils.MusicPreference;
import com.blacpythoz.musik.utils.NotificationHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by deadsec on 9/3/17.
 */

public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener, PlayerInterface,
        AudioManager.OnAudioFocusChangeListener {

    private static final String TAG = MusicService.class.getSimpleName();
    private static final int NOTIFICATION_ID = 123;
    private MediaPlayer player;
    private SongModel currentSong;
    private int currentSongPosition;
    Callback callback;
    private final IBinder musicBind = new MusicBinder();
    private AudioManager audioManager;
    private boolean audioFocusState = false;
    PlayerThread mPlayerThread;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        player = new MediaPlayer();
        mPlayerThread=new PlayerThread();
        mPlayerThread.start();
        initMusicService();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        switch (intent.getAction()) {
            case "":
                NotificationHandler.createNotification(this, currentSong);
                break;
            case "action.prev":
                playPrev();
                NotificationHandler.createNotification(this, currentSong);
                break;
            case "action.play":
                NotificationHandler.createNotification(this, currentSong);
                if (player != null) {
                    if (isPlaying()) {
                        pause();
                    } else {
                        start();
                    }
                } else {
                    initMusicService();
                    start();
                }
                break;
            case "action.next":
                playNext();
                NotificationHandler.createNotification(this, currentSong);
                break;
            case "action.stop":
                stop();
                stopForeground(true);
                stopSelf();
                break;
        }
        return START_STICKY;
    }

    public void initMusicService() {
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        restoreSong();
        if (!audioFocusState) {
            Log.i(TAG, "Focus before is: " + audioFocusState);
            requestAudioFocusManager();
            Log.i(TAG, "Focus after is: " + audioFocusState);
        }
    }

    private void restoreSong() {
        long currentSongId = MusicPreference.get(this).getLastPlayedSongId();
        if (currentSongId != 0) {
            currentSong = SongDataLab.get(this).getSong(currentSongId);
        } else {
            currentSong = SongDataLab.get(this).getRandomSong();
        }
        long currentSongDuration = MusicPreference.get(this).getLastPlayedSongDuration();
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (callback != null) {
            callback.onCompletion(currentSong);
        }
    }

    // if the audio focus changes, i.e whether
    // the user switch to another apps or new notification sound popup
    // or any video gets called
    @Override
    public void onAudioFocusChange(int i) {
        switch (i) {
            // if apps gets audio focus
            case AudioManager.AUDIOFOCUS_GAIN:
                if (player == null) initMusicService();
                else if (!player.isPlaying()) player.start();
                player.setVolume(1.0f, 1.0f);
                break;
            // if loss focus for an less time: stop and release player
            case AudioManager.AUDIOFOCUS_LOSS:
                try {
                    if (player != null) player.stop();
                    player.release();
                    player = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the player player because playback
                // is likely to resume
                if (player.isPlaying()) player.pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (player.isPlaying()) player.setVolume(0.1f, 0.1f);
                break;
        }
    }

    private void requestAudioFocusManager() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            audioFocusState = true;
        } else {
            audioFocusState = false;
        }

    }

    //remove the audio focus
    private boolean removeAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager.abandonAudioFocus(this);
    }

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public void onDestroy() {
        MusicPreference.get(this).setCurrentSongStatus(currentSong.getId(), player.getCurrentPosition());
        player.stop();
        player.release();
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
    }

    // Overriding the Player Interface methods
    @Override
    public void start() {
        player.start();
    }

    @Override
    public void play(long songId) {
        player.reset();
        SongModel playSong = SongDataLab.get(this).getSong(songId);
        play(playSong);
    }

    @Override
    public void play(SongModel song) {
        mPlayerThread.play(song);
//        currentSong = song;
//        if (player != null) {
//            player.reset();
//            try {
//                player.setDataSource(song.getData());
//                Log.i(TAG, song.getBookmark() + "");
//                player.prepareAsync();
//                this.callback.onTrackChange(song);
//            } catch (Exception e) {
//                Log.e(TAG, "Error playing from data source", e);
//            }
//        }
    }

    @Override
    public void pause() {
        if (player != null) {
            player.pause();
            callback.onPause();
        }
    }

    @Override
    public void stop() {
        if (player != null) {
            player.stop();
        }
    }

    @Override
    public boolean isPlaying() {
        if (player != null) {
            return player.isPlaying();
        } else {
            return false;
        }
    }

    @Override
    public int getCurrentStreamPosition() {
        return player.getCurrentPosition();
    }

    @Override
    public long getDuration() {
        return player.getDuration();
    }

    @Override
    public void seekTo(int position) {
        player.seekTo(position);
    }

    @Override
    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    // Services Helper Methods
    public void setSong(int songIndex) {
        currentSongPosition = songIndex;
    }

    public String getCurrentSongName() {
        return currentSong.getTitle();
    }

    public SongModel getCurrentSong() {
        return currentSong;
    }

    public void playNext() {
        play(currentSongPosition + 1);
    }

    public void playPrev() {
        play(currentSongPosition - 1);
    }

    public List<AlbumModel> getAlbums() {
        return SongDataLab.get(this).getAlbums();
    }

    public List<ArtistModel> getArtists() {
        return SongDataLab.get(this).getArtists();
    }

    public List<SongModel> getSongs() {
        return SongDataLab.get(this).getSongs();
    }

    // switch the current service to the foreground by creating the
    // notifications
    public void toForeground() {
        startForeground(NOTIFICATION_ID, NotificationHandler.createNotification(this, currentSong));
    }

    // kill the foreground notification, so that service
    // can run in background
    public void toBackground() {
        stopForeground(true);
    }

    public class PlayerThread extends Thread {
        private Handler mHandler;

        @Override
        public void run() {
            super.run();
            Looper.prepare();
            mHandler=new Handler();
            Looper.loop();
        }

        public void play(final SongModel song) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (player != null) {
                        player.reset();
                        try {
                            player.setDataSource(song.getData());
                            Log.i(TAG, song.getBookmark() + "");
                            player.prepareAsync();
                            MusicService.this.callback.onTrackChange(song);
                        } catch (Exception e) {
                            Log.e(TAG, "Error playing from data source", e);
                        }
                    }
                }
            });
        }

        public void prepareNext() {

    }

    public void exit() {
        mHandler.getLooper().quit();
    }
}


}
