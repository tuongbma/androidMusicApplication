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
import android.util.Log;
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
import com.ptit.android.model.Song;

public class PlayMusicFragment extends Fragment implements OnCompletionListener, SeekBar.OnSeekBarChangeListener  {
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
    private ArrayList<Song> songsList = new ArrayList<Song>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.play_music, null);
//
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        btnPlay = (ImageButton) v.findViewById(R.id.btnPlay);
        btnForward = (ImageButton) v.findViewById(R.id.btnForward);
        btnBackward = (ImageButton) v.findViewById(R.id.btnBackward);
        btnNext = (ImageButton) v.findViewById(R.id.btnNext);
        btnPrevious = (ImageButton) v.findViewById(R.id.btnPrevious);
        btnPlaylist = (ImageButton) v.findViewById(R.id.btnPlaylist);
        btnRepeat = (ImageButton) v.findViewById(R.id.btnRepeat);
        btnShuffle = (ImageButton) v.findViewById(R.id.btnShuffle);
        songProgressBar = (SeekBar) v.findViewById(R.id.songProgressBar);
        songTitleLabel = (TextView) v.findViewById(R.id.songTitle);
        songCurrentDurationLabel = (TextView) v.findViewById(R.id.songCurrentDurationLabel);
        songTotalDurationLabel = (TextView) v.findViewById(R.id.songTotalDurationLabel);
        albumPic = v.findViewById(R.id.albumPic);

        // Mediaplayer
        mp = new MediaPlayer();
        songManager = new SongsManager();
        utils = new Utilities();

        // Listeners
        songProgressBar.setOnSeekBarChangeListener(this); // Important
        mp.setOnCompletionListener(this); // Important

        Bundle bundle = getArguments();
        try{
            mode = bundle.getLong("MODE");
            typeSearch = bundle.getLong("typeSearch");
            currentSongIndex = bundle.getInt("songIndex");
            textSearch = bundle.getString("txtSearch");
            if(typeSearch == 0) {
                typeSearch = Constants.SEARCH_TYPE.TITLE;
            }
            //kiem tra xem online hay offline
            if(Constants.MODE.ONLINE.equals(mode)) {
                playSongOnline(currentSongIndex);
            } else {
                SongsManager songMng = new SongsManager();
                ArrayList<Song> totalSongOffline = songManager.getOfflineList();
                songsList = songMng.getSearchSongOffline(totalSongOffline, Constants.SEARCH_TYPE.TITLE, textSearch);
                System.out.println("current song " + currentSongIndex);
                playSongOffline(currentSongIndex);
            }

        } catch (NullPointerException e){
            Toast.makeText(getActivity(), "Không có bài hát nào được phát", Toast.LENGTH_SHORT).show();
        }

        /**
         * Play button click event
         * plays a song and changes button to pause image
         * pauses a song and changes button to play image
         * */
        btnPlay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // check for already playing
                if (mp.isPlaying()) {
                    if (mp != null) {
                        mp.pause();
                        // Changing button image to play button
                        btnPlay.setImageResource(R.drawable.btn_play);
                    }
                } else {
                    // Resume song
                    if (mp != null) {
                        mp.start();
                        // Changing button image to pause button
                        btnPlay.setImageResource(R.drawable.btn_pause);
                    }
                }

            }
        });

        /**
         * Forward button click event
         * Forwards song specified seconds
         * */
        btnForward.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // get current song position
                int currentPosition = mp.getCurrentPosition();
                // check if seekForward time is lesser than song duration
                if (currentPosition + seekForwardTime <= mp.getDuration()) {
                    // forward song
                    mp.seekTo(currentPosition + seekForwardTime);
                } else {
                    // forward to end position
                    mp.seekTo(mp.getDuration());
                }
            }
        });

        /**
         * Backward button click event
         * Backward song to specified seconds
         * */
        btnBackward.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // get current song position
                int currentPosition = mp.getCurrentPosition();
                // check if seekBackward time is greater than 0 sec
                if (currentPosition - seekBackwardTime >= 0) {
                    // forward song
                    mp.seekTo(currentPosition - seekBackwardTime);
                } else {
                    // backward to starting position
                    mp.seekTo(0);
                }

            }
        });

        /**
         * Next button click event
         * Plays next song by taking currentSongIndex + 1
         * */
        btnNext.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // check if next song is there or not
                if(Constants.MODE.ONLINE.equals(mode)) {
                    playSongOffline(currentSongIndex);
                } else {
                    if (currentSongIndex < (songsList.size() - 1)) {
                        playSongOffline(currentSongIndex + 1);
                        currentSongIndex = currentSongIndex + 1;
                    } else {
                        // play first song
                        playSongOffline(0);
                        currentSongIndex = 0;
                    }
                }
            }
        });

        /**
         * Back button click event
         * Plays previous song by currentSongIndex - 1
         * */
        btnPrevious.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if(Constants.MODE.ONLINE.equals(mode)) {
                    playSongOffline(currentSongIndex);
                } else {
                    if (currentSongIndex > 0) {
                        playSongOffline(currentSongIndex - 1);
                        currentSongIndex = currentSongIndex - 1;
                    } else {
                        // play last song
                        playSongOffline(songsList.size() - 1);
                        currentSongIndex = songsList.size() - 1;
                    }
                }
            }
        });

        /**
         * Button Click event for Repeat button
         * Enables repeat flag to true
         * */
        btnRepeat.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (isRepeat) {
                    isRepeat = false;
                    Toast.makeText(getActivity(), "Repeat is OFF", Toast.LENGTH_SHORT).show();
                    btnRepeat.setImageResource(R.drawable.btn_repeat);
                } else {
                    // make repeat to true
                    isRepeat = true;
                    Toast.makeText(getActivity(), "Repeat is ON", Toast.LENGTH_SHORT).show();
                    // make shuffle to false
                    isShuffle = false;
                    btnRepeat.setImageResource(R.drawable.btn_repeat_focused);
                    btnShuffle.setImageResource(R.drawable.btn_shuffle);
                }
            }
        });

        /**
         * Button Click event for Shuffle button
         * Enables shuffle flag to true
         * */
        btnShuffle.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (isShuffle) {
                    isShuffle = false;
                    Toast.makeText(getActivity(), "Shuffle is OFF", Toast.LENGTH_SHORT).show();
                    btnShuffle.setImageResource(R.drawable.btn_shuffle);
                } else {
                    // make repeat to true
                    isShuffle = true;
                    Toast.makeText(getActivity(), "Shuffle is ON", Toast.LENGTH_SHORT).show();
                    // make shuffle to false
                    isRepeat = false;
                    btnShuffle.setImageResource(R.drawable.btn_shuffle_focused);
                    btnRepeat.setImageResource(R.drawable.btn_repeat);
                }
            }
        });

//        /**
//         * Button Click event for Play list click event
//         * Launches list activity which displays list of songs
//         * */
//        btnPlaylist.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View arg0) {
//                Intent in;
//                if(Constants.MODE.OFFLINE.equals(mode)) {
//                    songsList = songsListOffline;
//                    in = new Intent(getActivity(), OnlineFragment.class);
//                    startActivityForResult(in, Constants.MODE.OFFLINE.intValue());
//                } else {
//                    in = new Intent(getActivity(), OfflineFragment.class);
//                    in.putExtra("txtSearch", textSearch);
//                    in.putExtra("typeSearch", typeSearch);
//                    startActivityForResult(in, Constants.MODE.ONLINE.intValue());
//                }
//
////				startActivity(i);
//            }
//        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) { // play offline
            try{
                currentSongIndex = data.getExtras().getInt("songOfflineIndex");
                playSongOffline(currentSongIndex);
            } catch (NullPointerException ex){

            }
            // play selected song

        }
        if (requestCode == Constants.MODE.ONLINE.intValue()) { // play online
            try{
                textSearch = data.getExtras().getString("txtSearch");
                currentSongIndex = data.getExtras().getInt("songOnlineIndex");
                playSongOnline(currentSongIndex);
            } catch (NullPointerException ex){

            }
            // play selected song

        }
    }

    /**
     * Function to play a song
     *
     *
     * @param songIndex - index of song
     */
    public void playSongOffline(int songIndex) {
        // Play song
        try {
            mp.reset();
            String source = songsList.get(songIndex).getSource();
            System.out.println("soure song: " + source);
            mp.setDataSource(source);
            setInfoPlayingSong(source);
            mp.prepare();
            mp.start();

            // Changing Button Image to pause image
            btnPlay.setImageResource(R.drawable.btn_pause);

            // set Progress bar values
            songProgressBar.setProgress(0);
            songProgressBar.setMax(100);

            // Updating progress bar
            updateProgressBar();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void  playSongOnline(final int songIndex){
        // Play song
        try {
            mp.reset();
            mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
            songManager.readData(textSearch, typeSearch, new SongsManager.MyCallback() {
                @Override
                public void onCallback(ArrayList<Song> songList) {
                    try {
                        String source = SERVER_STORAGE + songList.get(songIndex).getSource();
                        setInfoPlayingSong(source);
                        mp.setDataSource(source);
                        mp.prepare();
                        mp.start();
                        btnPlay.setImageResource(R.drawable.btn_pause);
                        // set Progress bar values
                        songProgressBar.setProgress(0);
                        songProgressBar.setMax(100);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            updateProgressBar();

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    /**
     * Update timer on seekbar
     */
    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    public void setInfoPlayingSong(String source) {
        MediaMetadataRetriever metaRetriver = new MediaMetadataRetriever();
        if(Constants.MODE.ONLINE.equals(mode)) {
            metaRetriver.setDataSource(source, new HashMap<String,String>());
        } else {
            metaRetriver.setDataSource(source);
        }

        byte[] art = metaRetriver.getEmbeddedPicture();
        Bitmap songImage = BitmapFactory.decodeByteArray(art, 0, art.length);
        albumPic.setImageBitmap(songImage);

        String songTitle = metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        songTitleLabel.setText(songTitle);
    }

    /**
     * Background Runnable thread
     */
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long totalDuration = mp.getDuration();
            long currentDuration = mp.getCurrentPosition();

            // Displaying Total Duration time
            songTotalDurationLabel.setText("" + utils.milliSecondsToTimer(totalDuration));
            // Displaying time completed playing
            songCurrentDurationLabel.setText("" + utils.milliSecondsToTimer(currentDuration));

            // Updating progress bar
            int progress = (int) (utils.getProgressPercentage(currentDuration, totalDuration));
            //Log.d("Progress", ""+progress);
            songProgressBar.setProgress(progress);

            // Running this thread after 100 milliseconds
            mHandler.postDelayed(this, 100);
        }
    };

    /**
     *
     * */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {

    }

    /**
     * When user starts moving the progress handler
     */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // remove message Handler from updating progress bar
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    /**
     * When user stops moving the progress hanlder
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
        int totalDuration = mp.getDuration();
        int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);

        // forward or backward to certain seconds
        mp.seekTo(currentPosition);

        // update timer progress again
        updateProgressBar();
    }

    /**
     * On Song Playing completed
     * if repeat is ON play same song again
     * if shuffle is ON play random song
     */
    @Override
    public void onCompletion(MediaPlayer arg0) {

        // check for repeat is ON or OFF
        if (isRepeat) {
            // repeat is on play same song again
            playSongOffline(currentSongIndex);
        } else if (isShuffle) {
            // shuffle is on - play a random song
            Random rand = new Random();
            currentSongIndex = rand.nextInt((songsList.size() - 1) - 0 + 1) + 0;
            playSongOffline(currentSongIndex);
        } else {
            // no repeat or shuffle ON - play next song
            if (currentSongIndex < (songsList.size() - 1)) {
                playSongOffline(currentSongIndex + 1);
                currentSongIndex = currentSongIndex + 1;
            } else {
                // play first song
                playSongOffline(0);
                currentSongIndex = 0;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mp.release();
    }
}
