package com.example.aspirees15.musicplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import java.util.ArrayList;
import android.content.ContentUris;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;
import java.util.Random;
import android.app.Notification;
import android.app.PendingIntent;

/**
 * Created by aspirees15 on 5/12/17.
 */

public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener
{

    private MediaPlayer player;
    private ArrayList<Song> songs;
    private int songPosn;
    private final IBinder musicBind = new MusicBinder();
    private String songTitle="";
    private String artist = "";
    private static final int NOTIFY_ID=1;
    private boolean shuffle = false;
    private Random random;

    public void onCreate()
    {
        super.onCreate();
        songPosn = 0;
        player = new MediaPlayer();
        random = new Random();

        initMusicPlayer();
    }

    public void initMusicPlayer()
    {
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    public void setList(ArrayList<Song> theSongs)
    {
        songs = theSongs;
    }


    public void playSong()
    {
        player.reset();

        Song playSong = songs.get(songPosn);
        songTitle = playSong.getTitle();
        artist = playSong.getArtist();
        long currSong = playSong.getID();

        Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currSong);

        try
        {
            player.setDataSource(getApplicationContext(), trackUri);
        }
        catch (Exception a)
        {
            Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_SHORT);
        }
        player.prepareAsync();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        player.stop();
        player.release();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp)
    {
        mp.start();

        Intent notIntentAliIpakJe = new Intent(this, MainActivity.class);
        notIntentAliIpakJe.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notIntentAliIpakJe, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);

        builder.setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.hifi_stream)
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentTitle(artist)
                .setContentText(songTitle);
        Notification not = builder.build();
        
        startForeground(NOTIFY_ID, not);



    }


    @Override
    public void onCompletion(MediaPlayer mp)
    {
        if(player.getCurrentPosition() > 0)
        {
            mp.reset();
            playNext();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra)
    {
        mp.reset();

        return false;
    }

    public void setSong(int songIndex)
    {
        songPosn = songIndex;
    }


    public class MusicBinder extends Binder{
        MusicService getService() {
            return MusicService.this;
        }
    }

    // Metoda za shuffle, shuffle it yeah.

    public void setShuffle(){
        if(shuffle) {
            shuffle = false;
        }
        else
        {
            shuffle = true;
        }
    }


    // Playback metode, triba nam to za dobru aplikacijuju.

    @Override
    public void onDestroy(){
        stopForeground(true);
    }

    public int getPosn(){
        return player.getCurrentPosition();
    }

    public int getDur(){
        return player.getDuration();
    }

    public boolean isPng(){
        return player.isPlaying();
    }

    public void pausePlayer(){
        player.pause();
    }

    public void seek(int posn){
        player.seekTo(posn);
    }

    public void go(){
        player.start();
    }

    public void playPrevious(){
        songPosn--;
        if (songPosn < 0){
            songPosn = songs.size()-1;
        }
        playSong();
    }

    public void playNext(){

        if(shuffle){
            int newSong = songPosn;

            while(newSong == songPosn)
            {
                newSong = random.nextInt(songs.size());
            }
            songPosn = newSong;
        }
        else
        {
            songPosn++;
            if(songPosn >= songs.size()){
                songPosn = 0;
            }
        }
        playSong();
    }

}
