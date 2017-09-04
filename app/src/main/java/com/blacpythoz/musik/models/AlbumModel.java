package com.blacpythoz.musik.models;

import java.util.ArrayList;

/**
 * Created by deadsec on 9/4/17.
 */

public class AlbumModel {
    public final ArrayList<SongModel> songs;

    public AlbumModel(ArrayList<SongModel> songs) {
        this.songs = songs;
    }
}
