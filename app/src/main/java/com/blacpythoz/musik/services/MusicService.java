package com.blacpythoz.musik.services;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.blacpythoz.musik.R;
import com.blacpythoz.musik.activities.MainActivity;
import com.blacpythoz.musik.interfaces.PlayerInterface;
import com.blacpythoz.musik.loader.DataLoader;
import com.blacpythoz.musik.models.AlbumModel;
import com.blacpythoz.musik.models.ArtistModel;
import com.blacpythoz.musik.models.SongModel;

import java.util.ArrayList;

/**
 * Created by deadsec on 9/3/17.
 */

public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener, PlayerInterface {

    private String LOG="MUSIC SERVICE";
    private MediaPlayer player;
    private ArrayList<SongModel> songs;
    private ArrayList<AlbumModel> albums;
    private ArrayList<ArtistModel> artists;
    private SongModel currentSong;
    private int currentSongPosition;
    Callback callback;
    private final IBinder musicBind = new MusicBinder();
    boolean firstLaunch;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        player = new MediaPlayer();
        songs=new ArrayList<>();
        currentSong=SongModel.EMPTY();
        currentSongPosition = 0;
        initMusicService();
        firstLaunch = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String LOG_TAG="service_tag";
        if(intent.getAction().equals("")) {
            createNotification();
        } else if (intent.getAction().equals("action.prev")) {
            playPrev();
            createNotification();
            Toast.makeText(this, "Clicked Previous!", Toast.LENGTH_SHORT).show();

        } else if (intent.getAction().equals("action.play")) {
            createNotification();
            if(isPlaying()) {
                pause();
            }else {
                start();
            }

        } else if (intent.getAction().equals("action.next")){
            playNext();
            createNotification();
        } else if (intent.getAction().equals("action.stop")) {
            stop();
            stopForeground(true);
            stopSelf();
        }
        return START_STICKY;
    }

    public void initMusicService(){
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        // load all the medias
        loadMedia();
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if(callback != null) {
            callback.onCompletion(currentSong);
        }
    }

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public void onDestroy() {
        player.stop();
        player.release();
        Log.i("Service","Called on destry");
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
    public void start() { player.start(); }

   @Override
    public void play(int pos){
        currentSongPosition=pos;
        player.reset();
        SongModel playSong = songs.get(pos);

       if(nbuilder != null) {
           updateNotificationOnPlay(playSong);
       }

        try{
            player.setDataSource(playSong.getData());
            Log.i("bookmark",playSong.getBookmark()+"");
            player.prepareAsync();
            callback.onTrackChange(playSong);
            Log.i(LOG,"Playing From Service");
        }
        catch(Exception e){
            Log.e(LOG, "Error setting data source", e);
        }
    }

    @Override
    public void play(SongModel song){
        currentSong=song;
        player.reset();
        try{
            player.setDataSource(song.getData());
            Log.i("bookmark",song.getBookmark()+"");
            player.prepareAsync();
            Log.i("proces","SOnged");
            this.callback.onTrackChange(song);
        }
        catch(Exception e){
            Log.e(LOG, "Error playing from data source", e);
        }
    }

    @Override
    public void pause() {
        player.pause();
        callback.onPause();
    }

     @Override
    public void stop() { player.stop(); }

    @Override
    public void loadMedia() {
        loadSong();
        loadAlbum();
        loadArtist();
    }

    @Override
    public boolean isPlaying() { return player.isPlaying(); }

    @Override
    public int getCurrentStreamPosition() { return player.getCurrentPosition(); }

    @Override
    public long getDuration() { return player.getDuration(); }

    @Override
    public void seekTo(int position) {
        player.seekTo(position);
    }

    @Override
    public void setCallback(Callback callback) { this.callback=callback; }


    // Services Helper Methods
    public void setSong(int songIndex){ currentSongPosition=songIndex; }
    public String getCurrentSongName(){ return currentSong.getTitle(); }
    public SongModel getCurrentSong() { return currentSong; }
    public void loadSong() { songs = DataLoader.getSongs(this); }
    public void loadAlbum() { albums = DataLoader.getAlbums(this); }
    public void loadArtist() { artists = DataLoader.getArtists(this); }
    public ArrayList<SongModel> getSongs() { return this.songs; }
    public ArrayList<AlbumModel> getAlbums() { return this.albums; }
    public ArrayList<ArtistModel> getArtists() { return this.artists; }
    public void setList(ArrayList<SongModel> newSongs){ songs=newSongs; }
    public void playNext() { play(currentSongPosition+1); }
    public void playPrev() { play(currentSongPosition-1); }

    // switch the current service to the foreground by creating the
    // notifications
    public void toForeground() {
        startForeground(123,createNotification());
    }
    // kill the foreground notification, so that service
    // can run in background
    public void toBackground() {
        stopForeground(true);
    }

    Notification.Builder nbuilder = null;
    public Notification createNotification() {
        Intent openAppIntent = new Intent(this,MainActivity.class);
        PendingIntent pendingOpenAppIntent = PendingIntent.getActivity(this,0,openAppIntent,0);

        Intent previousIntent = new Intent(this, MusicService.class);
        previousIntent.setAction("action.prev");
        PendingIntent ppreviousIntent = PendingIntent.getService(this, 0, previousIntent, 0);

        Intent playIntent = new Intent(this, MusicService.class);
        playIntent.setAction("action.play");
        PendingIntent pplayIntent = PendingIntent.getService(this, 0, playIntent, 0);

        Intent nextIntent = new Intent(this, MusicService.class);
        nextIntent.setAction("action.next");
        PendingIntent pnextIntent = PendingIntent.getService(this, 0, nextIntent, 0);

        Bitmap bitmap = getBitmap(currentSong.getAlbumArt());
        nbuilder = new Notification.Builder(this)
                    .setContentTitle(currentSong.getTitle())
                    .setTicker(currentSong.getTitle())
                    .setContentText(currentSong.getArtistName())
                    .setSmallIcon(R.drawable.music_icon)
                    .setContentIntent(pendingOpenAppIntent)
                    .setOngoing(true)
                    .setLargeIcon(bitmap)
                    .addAction(android.R.drawable.ic_media_previous, "Previous", ppreviousIntent)
                    .addAction(android.R.drawable.ic_media_play, "Toggle", pplayIntent)
                    .addAction(android.R.drawable.ic_media_next, "Next", pnextIntent)
                    .setLargeIcon(bitmap);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            nbuilder.setStyle(new Notification.MediaStyle());
        }
        return nbuilder.build();
    }

    // this method handles the updating of the notification
    // here is code redundant which must be clean up
    public void updateNotificationOnPlay(SongModel song) {
         Bitmap bitmap = getBitmap(song.getAlbumArt());
        nbuilder.setLargeIcon(bitmap)
        .setContentTitle(song.getTitle())
        .setTicker(song.getTitle())
        .setContentText(song.getArtistName());
        startForeground(123,nbuilder.build());
    }

    public Bitmap getBitmap(String uri) {
        Bitmap bitmap=null;
          try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(uri));
        }catch (Exception e) {
            bitmap = BitmapFactory.decodeResource(this.getResources(),R.drawable.album_default);
        }
        return bitmap;
    }
}
