package com.blacpythoz.musik.models;

/**
 * Created by deadsec on 9/2/17.
 */

public class SongModel {

    private int id;
    private int trackNumber;
    private int year;
    private int albumId;
    private int artistId;
    private long duration;
    private long dateModified;
    private long dateAdded;
    private long bookmark;
    private String title;
    private String artistName;
    private String composer;
    private String albumName;
    private String albumArt;
    private String data;


    public static SongModel EMPTY() {
        return new SongModel(0,"","","","","","",0,0,0,0,0,0,0,0);
    }

    public SongModel(int id, String title, String artistName,
                     String composer, String albumName, String albumArt,
                     String data, int trackNumber, int year, long duration,
                     long dateModified, long dateAdded, int albumId, int artistId,
                     long bookmark) {
        this.id = id;
        this.title = title;
        this.artistName = artistName;
        this.composer = composer;
        this.albumName = albumName;
        this.albumArt = albumArt;
        this.data = data;
        this.trackNumber = trackNumber;
        this.year = year;
        this.duration = duration;
        this.dateModified = dateModified;
        this.dateAdded = dateAdded;
        this.albumId = albumId;
        this.artistId = artistId;
        this.bookmark = bookmark;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getComposer() {
        return composer;
    }

    public String getAlbumName() {
        return albumName;
    }

    public String getAlbumArt() {
        return albumArt;
    }

    public String getData() {
        return data;
    }

    public int getTrackNumber() {
        return trackNumber;
    }

    public int getYear() {
        return year;
    }

    public long getDuration() {
        return duration;
    }

    public long getDateModified() {
        return dateModified;
    }

    public long getDateAdded() {
        return dateAdded;
    }

    public int getAlbumId() {
        return albumId;
    }

    public int getArtistId() {
        return artistId;
    }

    public long getBookmark() {
        return bookmark;
    }
}