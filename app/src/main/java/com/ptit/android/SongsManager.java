package com.ptit.android;

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

	private ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();
    private ArrayList<HashMap<String, String>> songListOnline;
	private String DB_NAME = "songs";
	private FirebaseDatabase database;
	private static Long TITLE_SEARCH_TYPE = 1L;
	private static Long ARTST_SEARCH_TYPE = 2L;
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
	public ArrayList<HashMap<String, String>> getOfflineList(){
		// SDCard Path
		String MEDIA_PATH = new String("/sdcard/Download/");
		File home = new File(MEDIA_PATH);

		if (home.listFiles(new FileExtensionFilter()).length > 0) {
			for (File file : home.listFiles(new FileExtensionFilter())) {
				HashMap<String, String> song = new HashMap<String, String>();
				song.put("songTitle", file.getName().substring(0, (file.getName().length() - 4)));
				song.put("songPath", file.getPath());
				// Adding each song to SongList
				songsList.add(song);
			}
		}
		// return songs list array
		return songsList;
	}

	public interface MyCallback {
		void onCallback(ArrayList<HashMap<String, String>> value);
	}

	public void readData(final String text, final Long searchType, final MyCallback myCallback) {
		songListOnline = new ArrayList<HashMap<String, String>>();
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
								HashMap<String, String> song = new HashMap<String, String>();
								song.put("songTitle", songTitle);
								song.put("songPath", s.getSource());
								// Adding each song to SongList
								songListOnline.add(song);
							}
						} else if (ARTST_SEARCH_TYPE.equals(searchType)) {
							if (songArtist.contains(text.toUpperCase())) {
								HashMap<String, String> song = new HashMap<String, String>();
								song.put("songTitle", songTitle);
								song.put("songPath", s.getSource());
								// Adding each song to SongList
								songListOnline.add(song);
							}
						}
					}
					myCallback.onCallback(songListOnline);
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

}
