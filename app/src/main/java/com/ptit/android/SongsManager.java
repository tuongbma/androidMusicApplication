package com.ptit.android;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;

import com.androidhive.musicplayer.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ptit.android.model.Song;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;

public class SongsManager {

    private ArrayList<Song> songList;
	private String DB_NAME = "songs";
	private FirebaseDatabase database;
	private static Long TITLE_SEARCH_TYPE = 1L;
	private static Long ARTST_SEARCH_TYPE = 2L;
	private MediaMetadataRetriever metaRetriver;
    // Constructor
	public SongsManager(){
	    database = FirebaseDatabase.getInstance();
	}

    public DatabaseReference getFireBaseReference() {
        return database.getReference(DB_NAME);
    }
	
	/**
	 * Function to read all mp3 files from sdcard
	 * and store the details in ArrayList
	 * */
	public ArrayList<Song> getOfflineList(){
		// SDCard Path
		String MEDIA_PATH = new String("/sdcard/Download/audio/");
		System.out.println(MEDIA_PATH);
		songList = new ArrayList<>();
		File home = new File(MEDIA_PATH);
		System.out.println("size: " + home.listFiles(new FileExtensionFilter()).length);
		if (home.listFiles(new FileExtensionFilter()).length > 0) {
			for (File file : home.listFiles(new FileExtensionFilter())) {
				Song bean = new Song();
//				System.out.println(file.getName());
//				HashMap<String, String> song = new HashMap<String, String>();
//				song.put("songTitle", file.getName().substring(0, (file.getName().length() - 4)));
//				song.put("songPath", file.getPath());
				// Adding each song to SongList
				songList.add(bean);
			}
		}
		// return songs list array
		return songList;
	}






	public interface MyCallback {
		void onCallback(ArrayList<Song> value);
	}

	public void readData(final String text, final Long searchType, final MyCallback myCallback) {
        songList = new ArrayList<Song>();
		DatabaseReference myRef = getFireBaseReference();
		if (text != null && !text.isEmpty()) {
			myRef.addValueEventListener(new ValueEventListener() {
				@Override
				public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
					for (DataSnapshot data : dataSnapshot.getChildren()) {
						String searchTxt = text.toLowerCase();
						Song s = data.getValue(Song.class);
						String songTitle = s.getTitle();
						String songArtist = s.getArtist();
						if (TITLE_SEARCH_TYPE.equals(searchType)) {
							if (songTitle.toLowerCase().contains(searchTxt)) {
//								HashMap<String, String> song = new HashMap<String, String>();
//								song.put("songTitle", songTitle);
//								song.put("songPath", s.getSource());
								// Adding each song to SongList
                                songList.add(s);
							}
						} else if (ARTST_SEARCH_TYPE.equals(searchType)) {
							if (songArtist.contains(text.toUpperCase())) {
								HashMap<String, String> song = new HashMap<String, String>();
								song.put("songTitle", songTitle);
								song.put("songPath", s.getSource());
								// Adding each song to SongList
                                songList.add(s);
							}
						}
					}
					myCallback.onCallback(songList);
				}

				@Override
				public void onCancelled(@NonNull DatabaseError databaseError) {

				}
			});
		}
	}
			/**
	 * Class to filter files which are having .mp3 extension
	 * */
	class FileExtensionFilter implements FilenameFilter {
		public boolean accept(File dir, String name) {
			return (name.endsWith(".mp3") || name.endsWith(".MP3"));
		}
	}

	public Song getInfoSongFromSource(String source) {
		System.out.println("source" + source);
		source = Constants.STORE_FIREBASE_SERVER + source;
		Song song = new Song();
		metaRetriver = new MediaMetadataRetriever();
		metaRetriver.setDataSource(source, new HashMap<String,String>());

		byte[] art = metaRetriver.getEmbeddedPicture();
		Bitmap songImage = BitmapFactory.decodeByteArray(art, 0, art.length);
		song.setTitle(metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
		song.setArtist(metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
		String durationStr = formateMilliSeccond(Long.parseLong(metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)));
		song.setDuration(durationStr);
		song.setSongImage(songImage);
//		song.setSongId(id);
		return song;
//		songTitleLabel.setText(songTitle);
	}

	/**
	 * Function to convert milliseconds time to
	 * Timer Format
	 * Hours:Minutes:Seconds
	 */
	public static String formateMilliSeccond(long milliseconds) {
		String finalTimerString = "";
		String secondsString = "";

		// Convert total duration into time
		int hours = (int) (milliseconds / (1000 * 60 * 60));
		int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
		int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);

		// Add hours if there
		if (hours > 0) {
			finalTimerString = hours + ":";
		}

		// Prepending 0 to seconds if it is one digit
		if (seconds < 10) {
			secondsString = "0" + seconds;
		} else {
			secondsString = "" + seconds;
		}

		finalTimerString = finalTimerString + minutes + ":" + secondsString;

		//      return  String.format("%02d Min, %02d Sec",
		//                TimeUnit.MILLISECONDS.toMinutes(milliseconds),
		//                TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
		//                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)));

		// return timer string
		return finalTimerString;
	}
}
