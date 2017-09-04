package com.blacpythoz.musik.models;

import java.util.ArrayList;

/**
 * Created by deadsec on 9/4/17.
 */

public class ArtistModel {
    public final ArrayList<AlbumModel> albums;

    public ArtistModel(ArrayList<AlbumModel> albums) {
        this.albums = albums;
    }
}
