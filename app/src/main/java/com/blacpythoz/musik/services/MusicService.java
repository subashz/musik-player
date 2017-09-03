package com.blacpythoz.musik.services;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.blacpythoz.musik.R;
import com.blacpythoz.musik.activities.MainActivity;
import com.blacpythoz.musik.interfaces.PlayBackInterface;
import com.blacpythoz.musik.models.SongModel;
import java.util.ArrayList;

/**
 * Created by deadsec on 9/3/17.
 */

public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener, PlayBackInterface{

    private MediaPlayer player;
    private ArrayList<SongModel> songs;
    private int songPosn;
    private SongModel currentSong;
    Callback callback;
    private final IBinder musicBind = new MusicBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        songPosn = 0;
        player = new MediaPlayer();
        songs=new ArrayList<>();
        currentSong=new SongModel(null,null,null,null);
        initMusicPlayer();
        Log.i("Services:","OnCreate()");
        if(currentSong.getSongName() != null) {
            Log.i("Start","Re-come here");
            player.start();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String LOG_TAG="service_tag";
       if (intent.getAction().equals("action.prev")) {
            Log.i(LOG_TAG, "Clicked Previous");
            Toast.makeText(this, "Clicked Previous!", Toast.LENGTH_SHORT).show();

        } else if (intent.getAction().equals("action.play")) {
            Log.i(LOG_TAG, "Clicked Play");
            Toast.makeText(this, "Clicked Play!", Toast.LENGTH_SHORT).show();

        } else if (intent.getAction().equals("action.next")){
            Log.i(LOG_TAG, "Clicked Next");
            Toast.makeText(this, "Clicked Next!", Toast.LENGTH_SHORT).show();

        } else if (intent.getAction().equals("action.stop")) {
            Log.i(LOG_TAG, "Received Stop Foreground Intent");
            stopForeground(true);
            stopSelf();
        }
        showNotification();
           return START_STICKY;
    }


    public void initMusicPlayer(){
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        loadMedia();
        Log.i("Services: ","initMusicPlayer()");
    }

    public void setList(ArrayList<SongModel> theSongs){
        songs=theSongs;
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
//
//    @Override
//    public boolean onUnbind(Intent intent){
//        player.stop();
//        player.release();
//        return false;
//    }

//    @Override
//    public void onDestroy() {
//        player.stop();
//        player.release();
//    }


    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
    }

    public void setSong(int songIndex){
        songPosn=songIndex;
    }

    @Override
    public void loadMedia() {
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
        songs.clear();
        do{
            String songName = cursor.getString(songNameColumnIndex);
            String artistName=cursor.getString(artistNameColumnIndex);
            String albumArt=cursor.getString(songAlbumIndex);
            // for album arts
            Uri artUri = ContentUris.withAppendedId(sArtworkUri,Long.parseLong(albumArt));
            String songUri=cursor.getString(songUriIndex);
            this.songs.add(new SongModel(songName,artistName,songUri,artUri.toString()));
        }while(cursor.moveToNext());
        cursor.close();
    }

    @Override
    public ArrayList<SongModel> getSongs() {
        return this.songs;
    }


    @Override
    public void start() {
        player.start();
    }

    @Override
    public void stop() {
        player.stop();
    }

    @Override
    public void setState(int state) {
        //
    }

    @Override
    public int getState() {
        return 0;
    }

    @Override
    public boolean isPlaying() {
        return player.isPlaying();
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
    public void setCurrentStreamPosition(int pos) {
        //
    }

    @Override
    public void play(int pos){
        songPosn=pos;
        player.reset();
        SongModel playSong = songs.get(pos);
        try{
            player.setDataSource(playSong.getSongUrl());
            Log.i("Service","Playing From Service");
        }
        catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        player.prepareAsync();
    }

    @Override
    public void play(SongModel song){
        currentSong=song;
        player.reset();
        try{
            player.setDataSource(song.getSongUrl());
            Log.i("Service","Playing From Service");
        }
        catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        player.prepareAsync();
    }


    @Override
    public void pause() {
        player.pause();
    }

    @Override
    public void seekTo(int position) {
        player.seekTo(position);
    }

    @Override
    public void setCallback(Callback callback) {
        this.callback=callback;
    }

    public void showNotification() {

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction("action.main");
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Intent previousIntent = new Intent(this, MusicService.class);
        previousIntent.setAction("action.prev");
        PendingIntent ppreviousIntent = PendingIntent.getService(this, 0,
                previousIntent, 0);

        Intent playIntent = new Intent(this, MusicService.class);
        playIntent.setAction("action.play");
        PendingIntent pplayIntent = PendingIntent.getService(this, 0,
                playIntent, 0);

        Intent nextIntent = new Intent(this, MusicService.class);
        nextIntent.setAction("action.next");
        PendingIntent pnextIntent = PendingIntent.getService(this, 0,
                nextIntent, 0);

//        Bitmap icon = BitmapFactory.decodeFile(currentSong.getAlbumArt());
        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("Music Player")
                .setTicker("Song Name: "+currentSong.getSongName())
                .setContentText("Artist Name: "+currentSong.getArtistName())
                .setSmallIcon(R.drawable.music_icon)
//                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .addAction(android.R.drawable.ic_media_previous, "Previous",
                        ppreviousIntent)
                .addAction(android.R.drawable.ic_media_play, "Play",
                        pplayIntent)
                .addAction(android.R.drawable.ic_media_next, "Next",
                        pnextIntent).build();

        startForeground(101, notification);

    }
}
