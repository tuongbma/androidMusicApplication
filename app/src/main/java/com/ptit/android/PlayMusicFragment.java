package com.ptit.android;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.androidhive.musicplayer.R;

public class PlayMusicFragment extends Fragment  {
    ImageButton btnSearch;
    EditText searchText;
    private ImageButton btnPlay;
    private ImageButton btnForward;
    private ImageButton btnBackward;
    private ImageButton btnNext;
    private ImageButton btnPrevious;
    private ImageButton btnPlaylist;
    private ImageButton btnRepeat;
    private ImageButton btnShuffle;
    private SongsManager songManager;
    private SeekBar songProgressBar;
    private TextView songTitleLabel;
    private TextView songCurrentDurationLabel;
    private TextView songTotalDurationLabel;
    private ImageView albumPic;
    // Media Player
    private MediaPlayer mp;
    // Handler to update UI timer, progress bar etc,.
    private Handler mHandler = new Handler();
    private Long mode;
    private Long typeSearch;
    private Utilities utils;
    private int seekForwardTime = 5000; // 5000 milliseconds
    private int seekBackwardTime = 5000; // 5000 milliseconds
    private int currentSongIndex = 0;
    private String textSearch;
    private boolean isShuffle = false;
    private boolean isRepeat = false;
    private static final String SERVER_STORAGE = "https://firebasestorage.googleapis.com/v0/b/musicapplication-f21a5.appspot.com/o/";
    private ArrayList<HashMap<String, String>> songsListOffline = new ArrayList<HashMap<String, String>>();
    private ArrayList<HashMap<String, String>> songsListOnline = new ArrayList<HashMap<String, String>>();
    private ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.play_music, null);
//
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
//        btnPlay = (ImageButton) v.findViewById(R.id.btnPlay);
//        btnForward = (ImageButton) v.findViewById(R.id.btnForward);
//        btnBackward = (ImageButton) v.findViewById(R.id.btnBackward);
//        btnNext = (ImageButton) v.findViewById(R.id.btnNext);
//        btnPrevious = (ImageButton) v.findViewById(R.id.btnPrevious);
//        btnPlaylist = (ImageButton) v.findViewById(R.id.btnPlaylist);
//        btnRepeat = (ImageButton) v.findViewById(R.id.btnRepeat);
//        btnShuffle = (ImageButton) v.findViewById(R.id.btnShuffle);
//        songProgressBar = (SeekBar) v.findViewById(R.id.songProgressBar);
//        songTitleLabel = (TextView) v.findViewById(R.id.songTitle);
//        songCurrentDurationLabel = (TextView) v.findViewById(R.id.songCurrentDurationLabel);
//        songTotalDurationLabel = (TextView) v.findViewById(R.id.songTotalDurationLabel);
//        albumPic = v.findViewById(R.id.albumPic);
    }

    //
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == 100) { // play offline
//            try{
//                currentSongIndex = data.getExtras().getInt("songOfflineIndex");
//                playSong(currentSongIndex);
//            } catch (NullPointerException ex){
//
//            }
//            // play selected song
//
//        }
//        if (requestCode == Constants.MODE.ONLINE.intValue()) { // play online
//            try{
//                Intent intent = getIntent();
//                textSearch = intent.getExtras().getString("txtSearch");
//                currentSongIndex = data.getExtras().getInt("songOnlineIndex");
//                playSongOnline(currentSongIndex);
//            } catch (NullPointerException ex){
//
//            }
//            // play selected song
//
//        }
//    }
//
//    /**
//     * Function to play a song
//     *
//     * @param songIndex - index of song
//     */
//    public void playSong(int songIndex) {
//        // Play song
//        try {
//            mp.reset();
//            mp.setDataSource(songsList.get(songIndex).get("songPath"));
//            mp.prepare();
//            mp.start();
//            // Displaying Song title
//            String songTitle = songsList.get(songIndex).get("songTitle");
//            songTitleLabel.setText(songTitle);
//
//            // Changing Button Image to pause image
//            btnPlay.setImageResource(R.drawable.btn_pause);
//
//            // set Progress bar values
//            songProgressBar.setProgress(0);
//            songProgressBar.setMax(100);
//
//            // Updating progress bar
//            updateProgressBar();
//        } catch (IllegalArgumentException e) {
//            e.printStackTrace();
//        } catch (IllegalStateException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void  playSongOnline(final int songIndex){
//        // Play song
//        try {
//            mp.reset();
//            mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
//            songManager.readData(textSearch, typeSearch, new SongsManager.MyCallback() {
//                @Override
//                public void onCallback(ArrayList<HashMap<String, String>> value) {
//                    try {
//                        songsList = value;
//                        String source = SERVER_STORAGE + value.get(songIndex).get("songPath");
//                        setInfoPlayingSong(source);
//                        mp.setDataSource(source);
//                        mp.prepare();
//                        mp.start();
//                        btnPlay.setImageResource(R.drawable.btn_pause);
//                        // set Progress bar values
//                        songProgressBar.setProgress(0);
//                        songProgressBar.setMax(100);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            });
//            updateProgressBar();
//
//        } catch (IllegalArgumentException e) {
//            e.printStackTrace();
//        } catch (IllegalStateException e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * Update timer on seekbar
//     */
//    public void updateProgressBar() {
//        mHandler.postDelayed(mUpdateTimeTask, 100);
//    }
//
//    public void setInfoPlayingSong(String source) {
//        MediaMetadataRetriever metaRetriver = new MediaMetadataRetriever();
//        metaRetriver.setDataSource(source, new HashMap<String,String>());
//
//        byte[] art; art = metaRetriver.getEmbeddedPicture();
//        Bitmap songImage = BitmapFactory.decodeByteArray(art, 0, art.length);
//        albumPic.setImageBitmap(songImage);
//
//        String songTitle = metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
//        songTitleLabel.setText(songTitle);
//    }
//
//    /**
//     * Background Runnable thread
//     */
//    private Runnable mUpdateTimeTask = new Runnable() {
//        public void run() {
//            long totalDuration = mp.getDuration();
//            long currentDuration = mp.getCurrentPosition();
//
//            // Displaying Total Duration time
//            songTotalDurationLabel.setText("" + utils.milliSecondsToTimer(totalDuration));
//            // Displaying time completed playing
//            songCurrentDurationLabel.setText("" + utils.milliSecondsToTimer(currentDuration));
//
//            // Updating progress bar
//            int progress = (int) (utils.getProgressPercentage(currentDuration, totalDuration));
//            //Log.d("Progress", ""+progress);
//            songProgressBar.setProgress(progress);
//
//            // Running this thread after 100 milliseconds
//            mHandler.postDelayed(this, 100);
//        }
//    };
//
//    /**
//     *
//     * */
//    @Override
//    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
//
//    }
//
//    /**
//     * When user starts moving the progress handler
//     */
//    @Override
//    public void onStartTrackingTouch(SeekBar seekBar) {
//        // remove message Handler from updating progress bar
//        mHandler.removeCallbacks(mUpdateTimeTask);
//    }
//
//    /**
//     * When user stops moving the progress hanlder
//     */
//    @Override
//    public void onStopTrackingTouch(SeekBar seekBar) {
//        mHandler.removeCallbacks(mUpdateTimeTask);
//        int totalDuration = mp.getDuration();
//        int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);
//
//        // forward or backward to certain seconds
//        mp.seekTo(currentPosition);
//
//        // update timer progress again
//        updateProgressBar();
//    }

    /**
     * On Song Playing completed
     * if repeat is ON play same song again
     * if shuffle is ON play random song
     */
//    @Override
//    public void onCompletion(MediaPlayer arg0) {
//
//        // check for repeat is ON or OFF
//        if (isRepeat) {
//            // repeat is on play same song again
//            playSong(currentSongIndex);
//        } else if (isShuffle) {
//            // shuffle is on - play a random song
//            Random rand = new Random();
//            currentSongIndex = rand.nextInt((songsList.size() - 1) - 0 + 1) + 0;
//            playSong(currentSongIndex);
//        } else {
//            // no repeat or shuffle ON - play next song
//            if (currentSongIndex < (songsList.size() - 1)) {
//                playSong(currentSongIndex + 1);
//                currentSongIndex = currentSongIndex + 1;
//            } else {
//                // play first song
//                playSong(0);
//                currentSongIndex = 0;
//            }
//        }
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        mp.release();
//    }
//
//}
}