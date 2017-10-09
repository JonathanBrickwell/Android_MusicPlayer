package com.example.aspirees15.musicplayer;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import com.example.aspirees15.musicplayer.MusicService.MusicBinder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;

public class MainActivity extends AppCompatActivity implements MediaPlayerControl {

    private ArrayList<Song> songList;
    private ListView songView;
    private MusicService musicService;
    private Intent playIntent;
    private boolean musicBound = false;
    private MediaController controller;
    private boolean paused = false, playbackPaused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.GRAY));

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if(checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions(new String[] {android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                return;

            }
        }

        songView = (ListView) findViewById(R.id.songList);
        songList = new ArrayList<Song>();

        getSongList();


        Collections.sort(songList, new Comparator<Song>() {
            @Override
            public int compare(Song o1, Song o2) {
                return o1.getArtist().compareTo(o2.getArtist());
            }
        });

        SongAdapter songADP = new SongAdapter(this, songList);
        songView.setAdapter(songADP);

        setController();

    }

    @Override
    protected void onStart()
    {
        super.onStart();
        if(playIntent == null)
        {
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicBinder binder = (MusicBinder) service;

            musicService = binder.getService();
            musicService.setList(songList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    public void getSongList()
    {
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        if(musicCursor != null && musicCursor.moveToFirst())
        {
            int tittleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);

            do
            {
                long thisID = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(tittleColumn);
                String thisArtist = musicCursor.getString(artistColumn);

                songList.add(new Song(thisID, thisTitle, thisArtist));

            }
            while(musicCursor.moveToNext());
        }

    }

    public void songPicked(View view)
    {
        musicService.setSong(Integer.parseInt(view.getTag().toString()));
        musicService.playSong();

        if(playbackPaused)
        {
            setController();
            playbackPaused = false;
        }

        controller.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_shuffle:
                musicService.setShuffle();
                break;
            case R.id.action_end:
                stopService(playIntent);
                musicService = null;
                System.exit(0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy()
    {
        if(musicBound)
        {
            unbindService(musicConnection);
        }

        stopService(playIntent);
        musicService = null;
        super.onDestroy();
    }

    // Medija Kontroler metode za uporabu medija player kontrolera
    // fluentnost komentiranja na djelu.
    // ha-ca.


    private void setController()
    {
        controller = new MediaController(this);

        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrevious();
            }
        });

        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.songList));
        controller.setEnabled(true);
    }

    private void playNext() {
        musicService.playNext();

        if(playbackPaused)
        {
            setController();
            playbackPaused = false;
        }

        controller.show(0);
    }

    private void playPrevious(){
        musicService.playPrevious();

        if(playbackPaused)
        {
            setController();
            playbackPaused = false;
        }

        controller.show(0);
    }

    @Override
    public void start() {
        musicService.go();
    }

    @Override
    public void pause() {
        playbackPaused = true;
        controller.show();
        musicService.pausePlayer();
    }

    @Override
    public int getDuration() {

        if(musicService != null && musicBound && musicService.isPng()){
            return musicService.getDur();
        }
        else return 0;
    }

    @Override
    public int getCurrentPosition() {
        if(musicService!=null && musicBound && musicService.isPng()){
            return musicService.getPosn();
        }
        else
        {
            return 0;
        }
    }

    @Override
    public void seekTo(int pos) {
        musicService.seek(pos);
    }

    @Override
    public boolean isPlaying() {

        if(musicService != null && musicBound){
            return musicService.isPng();
        }
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    protected void onPause(){
        super.onPause();
        paused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(paused){
            setController();
            paused = false;
        }
    }

    @Override
    protected void onStop() {
        controller.hide();
        super.onStop();
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }
}
