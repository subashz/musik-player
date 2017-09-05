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
    public ArrayList<AlbumModel> getAlbums() {
        return this.albums;
    }
    public String getArtistName() {
      if(albums.size()>0) {
          return albums.get(0).getArtistName();
      } else {
          return "";
      }
    }
     public String getArtistImage() {
      if(albums.size()>0) {
          return albums.get(0).getCoverArt();
      } else {
          return "";
      }
    }
    public int getAlbumCount() {
        if (albums.size() > 0) {
            return albums.size();
        } else {
            return 0;
        }
    }


    public int getSongCount() {
        int sum =0;
        for(AlbumModel albumModel:albums) {
            sum+=albumModel.getNoOfSong();
        }
        return sum;
    }
}
