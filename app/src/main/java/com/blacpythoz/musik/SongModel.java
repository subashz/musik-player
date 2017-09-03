package com.blacpythoz.musik;

/**
 * Created by deadsec on 9/2/17.
 */

public class SongModel {
    private String songName;
    private String artistName;
    private String songUrl;
    private String albumArt;


    public SongModel(String songName, String artistName, String songUrl,String albumArt) {
        this.songName = songName;
        this.artistName = artistName;
        this.songUrl = songUrl;
        this.albumArt=albumArt;
    }

    public String getSongName() {
        return songName;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getSongUrl() {
        return songUrl;
    }

    public String getAlbumArt() {
        return albumArt;
    }
}
