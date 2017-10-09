package com.example.aspirees15.musicplayer;

/**
 * Created by aspirees15 on 5/12/17.
 */

public class Song {

    private long id;
    private String title;
    private String artist;

    public Song(long songID, String songTitle, String songArtist)
    {
        id=songID;
        title=songTitle;
        artist=songArtist;
    }

    public long getID(){return id;}
    public String getTitle(){return title;}
    public String getArtist(){return artist;}



}
