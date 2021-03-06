package com.ptit.android;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import android.widget.TextView;
import android.widget.Toast;

import com.androidhive.musicplayer.R;
import com.ptit.android.MyAdapter.MyArrayAdapter;
import com.ptit.android.model.Song;
import com.ptit.android.speechrecognize.RecognizeCommands;


public class MainActivity<recordingBufferLock> extends AppCompatActivity {

    private Button btnOffline;
    private ImageButton btnOnline;
    private ListView lvSong;
    private SongsManager songsManager = new SongsManager();
    private TextView lblSeachResult;
    private ArrayAdapter<String> adapter;

    private EditText edtSearch;
    private static final int SAMPLE_RATE = 16000;
    private static final int SAMPLE_DURATION_MS = 1000;
    private static final int RECORDING_LENGTH = (int) (SAMPLE_RATE * SAMPLE_DURATION_MS / 1000);
    private static final long AVERAGE_WINDOW_DURATION_MS = 1000;
    private static final float DETECTION_THRESHOLD = 0.50f;
    private static final int SUPPRESSION_MS = 500;
    private static final int MINIMUM_COUNT = 3;
    private static final long MINIMUM_TIME_BETWEEN_SAMPLES_MS = 30;
    private static final String LABEL_FILENAME = "file:///android_asset/conv_v14.txt";
    private static final String MODEL_FILENAME = "file:///android_asset/conv_v16.tflite";

    // Working variables.
    short[] recordingBuffer = new short[RECORDING_LENGTH];
    int recordingOffset = 0;
    boolean shouldContinue = true;
    private Thread recordingThread;
    boolean shouldContinueRecognition = true;
    private Thread recognitionThread;
    private final ReentrantLock recordingBufferLock = new ReentrantLock();

    private List<String> labels = new ArrayList<String>();
    private List<String> displayedLabels = new ArrayList<>();
    private RecognizeCommands recognizeCommands = null;
    private Interpreter tfLite;
    private String txtSearch;
    public static Fragment onlineFragment = new com.ptit.android.OnlineFragment();
    public static Fragment offlineFragment = new com.ptit.android.OfflineFragment();
    public static Fragment playMusicFragment = new com.ptit.android.PlayMusicFragment();
    public static FragmentManager fragmentManager;
    // UI elements.
    private static final int REQUEST_RECORD_AUDIO = 13;
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    /**
     * Memory-map the model file in Assets.
     */
    private static MappedByteBuffer loadModelFile(AssetManager assets, String modelFilename)
            throws IOException {
        AssetFileDescriptor fileDescriptor = assets.openFd(modelFilename);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        BottomNavigationView navigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        navigationView.setOnNavigationItemSelectedListener(navListener);
        lblSeachResult = findViewById(R.id.lblSearchResult);
        fragmentManager = getSupportFragmentManager();
        loadFragment(offlineFragment, "offlineFragment");
//        fragmentManager.beginTransaction().add(R.id.fragment_container, onlineFragment)
//                .add(R.id.fragment_container, offlineFragment)
//                .add(R.id.fragment_container, playMusicFragment)
//                .commit();
        btnOnline = (ImageButton) findViewById(R.id.btnSearch);
//        btnOffline = (Button) findViewById(R.id.btnOffline);
        edtSearch = (EditText) findViewById(R.id.txtSearch);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
//        lvSearch = getListView();
//        lvSearch.setAdapter(adapter);

//
//        edtSearch.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                performSearch(edtSearch.getText().toString());
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//
//            }
//        });


//        lvSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                // getting listitem index
//                int songIndex = position;
//                // Starting new intent
//                Intent in = new Intent(MainActivity.this, PlayMusicActivity.class);
//                in.addFlags(
//                        Intent.FLAG_ACTIVITY_SINGLE_TOP
//                );
//                // Sending songIndex to PlayMusicActivity
//                txtSearch = edtSearch.getText().toString();
//                in.putExtra("songOnlineIndex", songIndex);
//                in.putExtra("txtSearch", txtSearch);
//                in.putExtra("MODE", Constants.MODE.ONLINE);
//                in.putExtra("typeSearch", Constants.SEARCH_TYPE.TITLE);
//                startActivity(in);
//                finish();
//            }
//        });

        String actualLabelFilename = LABEL_FILENAME.split("file:///android_asset/", -1)[1];
        Log.i(LOG_TAG, "Reading labels from: " + actualLabelFilename);
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(getAssets().open(actualLabelFilename)));
            String line;
            while ((line = br.readLine()) != null) {
                labels.add(line);
                if (line.charAt(0) != '_') {
                    displayedLabels.add(line.substring(0, 1).toUpperCase() + line.substring(1));
                }
            }
            br.close();
        } catch (IOException e) {
            throw new RuntimeException("Problem reading label file!", e);
        }

        // Set up an object to smooth recognition results to increase accuracy.
        recognizeCommands =
                new RecognizeCommands(
                        labels,
                        AVERAGE_WINDOW_DURATION_MS,
                        DETECTION_THRESHOLD,
                        SUPPRESSION_MS,
                        MINIMUM_COUNT,
                        MINIMUM_TIME_BETWEEN_SAMPLES_MS);

        String actualModelFilename = MODEL_FILENAME.split("file:///android_asset/", -1)[1];
        try {
            tfLite = new Interpreter(loadModelFile(getAssets(), actualModelFilename));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        tfLite.resizeInput(0, new int[]{RECORDING_LENGTH, 1});

        // Start the recording and recognition threads.
//        requestMicrophonePermission();
//        startRecording();
//        startRecognition();
    }


//    public void performSearch(String txtSearch) {
//        final SongsManager songsManager = new SongsManager();
//        songsManager.readData(txtSearch, Constants.SEARCH_TYPE.TITLE, new SongsManager.MyCallback() {
//            @Override
//            public void onCallback(ArrayList<Song> songList) {
//                System.out.println("size songlist:" + songList.size());
////                ListAdapter adapter = new SimpleAdapter(MainActivity.this, songList,
////                        R.layout.playlist_item, new String[]{"songTitle"}, new int[]{
////                        R.id.songTitle});
////                setListAdapter(adapter);
//                ArrayList<Song> songLst = new ArrayList<>();
//                for (Song song : songList) {
//                    Song songBean = songsManager.getInfoSongFromSource(song.getSource());
//                    songLst.add(songBean);
//                }
//                MyArrayAdapter mayArr = new MyArrayAdapter(MainActivity.this, R.layout.list_row, songLst);
//                lvSong.setAdapter(mayArr);
//            }
//
//        });
//    }


    private void requestMicrophonePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                    new String[]{android.Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_RECORD_AUDIO
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startRecording();
            startRecognition();
        }
    }

    public synchronized void startRecording() {
        if (recordingThread != null) {
            return;
        }
        shouldContinue = true;
        recordingThread =
                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                record();
                            }
                        });
        recordingThread.start();
    }

    public synchronized void stopRecording() {
        if (recordingThread == null) {
            return;
        }
        shouldContinue = false;
        recordingThread = null;
    }

    private void record() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);

        // Estimate the buffer size we'll need for this device.
        int bufferSize =
                AudioRecord.getMinBufferSize(
                        SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            bufferSize = SAMPLE_RATE * 2;
        }
        short[] audioBuffer = new short[bufferSize / 2];

        AudioRecord record =
                new AudioRecord(
                        MediaRecorder.AudioSource.DEFAULT,
                        SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        bufferSize);

        if (record.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e(LOG_TAG, "Audio Record can't initialize!");
            return;
        }

        record.startRecording();

        Log.v(LOG_TAG, "Start recording");

        // Loop, gathering audio data and copying it to a round-robin buffer.
        while (shouldContinue) {
            int numberRead = record.read(audioBuffer, 0, audioBuffer.length);
            int maxLength = recordingBuffer.length;
            int newRecordingOffset = recordingOffset + numberRead;
            int secondCopyLength = Math.max(0, newRecordingOffset - maxLength);
            int firstCopyLength = numberRead - secondCopyLength;
            // We store off all the data for the recognition thread to access. The ML
            // thread will copy out of this buffer into its own, while holding the
            // lock, so this should be thread safe.
            recordingBufferLock.lock();
            try {
                System.arraycopy(audioBuffer, 0, recordingBuffer, recordingOffset, firstCopyLength);
                System.arraycopy(audioBuffer, firstCopyLength, recordingBuffer, 0, secondCopyLength);
                recordingOffset = newRecordingOffset % maxLength;
            } finally {
                recordingBufferLock.unlock();
            }
        }

        record.stop();
        record.release();
    }

    public synchronized void startRecognition() {
        if (recognitionThread != null) {
            return;
        }
        shouldContinueRecognition = true;
        recognitionThread =
                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                recognize();
                            }
                        });
        recognitionThread.start();
    }

    public synchronized void stopRecognition() {
        if (recognitionThread == null) {
            return;
        }
        shouldContinueRecognition = false;
        recognitionThread = null;
    }

    private void recognize() {

        Log.v(LOG_TAG, "Start recognition");

        short[] inputBuffer = new short[RECORDING_LENGTH];
        float[][] floatInputBuffer = new float[RECORDING_LENGTH][1];
        float[][] outputScores = new float[1][labels.size()];
        int[] sampleRateList = new int[]{SAMPLE_RATE};

        // Loop, grabbing recorded data and running the recognition model on it.
        while (shouldContinueRecognition) {
            long startTime = new Date().getTime();
            // The recording thread places data in this round-robin buffer, so lock to
            // make sure there's no writing happening and then copy it to our own
            // local version.
            recordingBufferLock.lock();
            try {
                int maxLength = recordingBuffer.length;
                int firstCopyLength = maxLength - recordingOffset;
                int secondCopyLength = recordingOffset;
                System.arraycopy(recordingBuffer, recordingOffset, inputBuffer, 0, firstCopyLength);
                System.arraycopy(recordingBuffer, 0, inputBuffer, firstCopyLength, secondCopyLength);
            } finally {
                recordingBufferLock.unlock();
            }

            // We need to feed in float values between -1.0f and 1.0f, so divide the
            // signed 16-bit inputs.
            for (int i = 0; i < RECORDING_LENGTH; ++i) {
                floatInputBuffer[i][0] = inputBuffer[i] / 32767.0f;
            }

            Object[] inputArray = {floatInputBuffer};
            Map<Integer, Object> outputMap = new HashMap<>();
            outputMap.put(0, outputScores);

            // Run the model.
            tfLite.runForMultipleInputsOutputs(inputArray, outputMap);

            // Use the smoother to figure out if we've had a real recognition event.
            long currentTime = System.currentTimeMillis();
            final RecognizeCommands.RecognitionResult result =
                    recognizeCommands.processLatestResults(outputScores[0], currentTime);

            Log.d("LISTENING", result.foundCommand + " - " + result.score + " - " + result.isNewCommand);
            if (result.foundCommand.equals("kiki") && result.isNewCommand) {
                Log.d("LISTENING ", "ACTIVATEEEEEEEEEEEEEEEEEEE");
//                try {
//                    // We don't need to run too frequently, so snooze for a bit.
//                    Thread.sleep(5000);
//                } catch (InterruptedException e) {
//                    // Ignore
//                }
                stopRecording();
                stopRecognition();
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, 10);
                } else {
                    Toast.makeText(this, "Your Device Don't Support Speech Input", Toast.LENGTH_SHORT).show();
                }
            }
            try {
                // We don't need to run too frequently, so snooze for a bit.
                Thread.sleep(MINIMUM_TIME_BETWEEN_SAMPLES_MS);
            } catch (InterruptedException e) {
                // Ignore
            }
        }

        Log.v(LOG_TAG, "End recognition");
    }

    private boolean loadFragment(Fragment fragment, String fragmentTag) {
        if (fragment != null) {
            fragmentManager.beginTransaction().add(R.id.fragment_container, fragment, fragmentTag).commit();
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 10:
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    Log.d("LISTENING ", result.get(0));
//                    textView.setText(result.get(0));

                }
                break;
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    Fragment fragment = null;
                    if (offlineFragment != null && onlineFragment != null && playMusicFragment != null) {
                        switch (menuItem.getItemId()) {
                            case R.id.actionOnline:
                                if (!checkIfFragmentExisted("onlineFragment")) {
                                    loadFragment(onlineFragment, "onlineFragment");
                                }
                                showHideFragment(onlineFragment, offlineFragment, playMusicFragment);
                                break;
                            case R.id.actionOffline:
                                if (!checkIfFragmentExisted("offlineFragment")) {
                                    loadFragment(offlineFragment, "offlineFragment");
                                }
                                showHideFragment(offlineFragment, onlineFragment, playMusicFragment);
                                break;
                            case R.id.actionPlaying:
                                if (!checkIfFragmentExisted("playMusicFragment")) {
                                    System.out.println("NOT AĐDD");
                                    loadFragment(playMusicFragment, "playMusicFragment");
                                }
                                showHideFragment(playMusicFragment, onlineFragment, offlineFragment);
                                break;
                            case R.id.actionPersonal:
                                break;
                        }
                        return true;
                    }
                    return false;
                }
            };

    public void showHideFragment(Fragment fragment1, Fragment fragment2, Fragment fragment3) {
        if (fragment1.isHidden()) {
            fragmentManager.beginTransaction()
                    .show(fragment1)
                    .commit();
        }
        fragmentManager.beginTransaction()
                .hide(fragment2)
                .hide(fragment3)
                .commit();
    }

    public boolean checkIfFragmentExisted(String fragmentTag){
        Fragment fragment = fragmentManager.findFragmentByTag(fragmentTag);
        if (fragment == null) {
            return false;
        }
        else{
            return true;
        }
    }
}
