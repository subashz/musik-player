package com.blacpythoz.musik.models;

import java.util.ArrayList;

/**
 * Created by deadsec on 9/4/17.
 */

public class AlbumModel {
    public final ArrayList<SongModel> songs;
    public int id;

    public AlbumModel(ArrayList<SongModel> songs) {
        this.songs = songs;
    }
    public void setId(int i) {
        this.id=i;
    }
    public ArrayList<SongModel> getAlbumSongs() {
        return songs;
    }

    public  String getName() {
        if(songs.size()>0) {
            return songs.get(0).getAlbumName();
        }else {
            return " ";
        }
    }
    public int getNoOfSong() {
        return songs.size();
    }

    public String getCoverArt() {
        if(songs.size()>0) {
            return songs.get(0).getAlbumArt();
        }else {
            return " ";
        }
    }
}
