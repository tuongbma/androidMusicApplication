package com.ptit.android;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.androidhive.musicplayer.R;
import com.ptit.android.MyAdapter.MyArrayAdapter;
import com.ptit.android.model.Song;

public class OfflineFragment extends ListFragment {
    // Songs list
    public ArrayList<Song> songsList = new ArrayList<>();
    private PlayMusicFragment playMusicFragment = new PlayMusicFragment();
    private Bundle bundle = new Bundle();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.show_list_songs, container, false);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        SongsManager songsManager = new SongsManager();
        // get all songs from sdcard
        this.songsList = songsManager.getOfflineList();
        if (songsList.size() == 0) {
            toastMessage("Khong co bai hat nao");
        } else {
            // looping through show_list_songs
//		for (int i = 0; i < songsList.size(); i++) {
//			// creating new HashMap
//			HashMap<String, String> song = songsList.get(i);
//			// adding HashList to ArrayList
//			listOffline.add(song);
//		}
            // selecting single ListView item
            ListView lv = getListView();

            // Adding menuItems to ListView
//            ListAdapter adapter = new SimpleAdapter(getActivity(), songsList,
//                    R.layout.playlist_item, new String[]{"songTitle"}, new int[]{
//                    R.id.songTitle});
//
//            setListAdapter(adapter);
            MyArrayAdapter mayArr = new MyArrayAdapter(getActivity(), R.layout.list_row, songsList);
            lv.setAdapter(mayArr);
            // listening to single listitem click
            lv.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    // getting listitem index
                    int songIndex = position;
                    // Starting new intent
                    Intent in = new Intent();
                    // Sending songIndex to PlayMusicActivity
                    in.putExtra("songOfflineIndex", songIndex);
//                    setResult(100, in);
//                    finish();

                    bundle.putInt("songIndex", songIndex);
                    bundle.putLong("MODE", Constants.MODE.OFFLINE);
                    bundle.putLong("typeSearch", Constants.SEARCH_TYPE.TITLE);
                    playMusicFragment = new PlayMusicFragment();
                    playMusicFragment.setArguments(bundle);
                }
            });
        }
    }

    //    @Override
//    public void onCreate(final Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.show_list_songs);
//            SongsManager songsManager = new SongsManager();
//            // get all songs from sdcard
//            this.songsList = songsManager.getOfflineList();
//            if (songsList.size() == 0) {
//                toastMessage("Khong co bai hat nao");
//            } else {
//                // looping through show_list_songs
////		for (int i = 0; i < songsList.size(); i++) {
////			// creating new HashMap
////			HashMap<String, String> song = songsList.get(i);
////			// adding HashList to ArrayList
////			listOffline.add(song);
////		}
//
//                // Adding menuItems to ListView
//                ListAdapter adapter = new SimpleAdapter(this, songsList,
//                        R.layout.playlist_item, new String[]{"songTitle"}, new int[]{
//                        R.id.songTitle});
//
//                setListAdapter(adapter);
//
//                // selecting single ListView item
//                ListView lv = getListView();
//                // listening to single listitem click
//                lv.setOnItemClickListener(new OnItemClickListener() {
//
//                    @Override
//                    public void onItemClick(AdapterView<?> parent, View view,
//                                            int position, long id) {
//                        // getting listitem index
//                        int songIndex = position;
//                        // Starting new intent
//                        Intent in = new Intent();
//                        // Sending songIndex to PlayMusicActivity
//                        in.putExtra("songOfflineIndex", songIndex);
//                        setResult(100, in);
//                        finish();
//                    }
//                });
//            }
//
////		final ArrayList<HashMap<String, String>> listOffline = new ArrayList<HashMap<String, String>>();
//
//        SongsManager songsManager = new SongsManager();
//        // get all songs from sdcard
//        this.songsList = songsManager.getOfflineList();
//        if (songsList.size() == 0) {
//            toastMessage("Khong co bai hat nao");
//        } else {
//            // looping through show_list_songs
////		for (int i = 0; i < songsList.size(); i++) {
////			// creating new HashMap
////			HashMap<String, String> song = songsList.get(i);
////			// adding HashList to ArrayList
////			listOffline.add(song);
////		}
//
//            // Adding menuItems to ListView
//            ListAdapter adapter = new SimpleAdapter(this, songsList,
//                    R.layout.playlist_item, new String[]{"songTitle"}, new int[]{
//                    R.id.songTitle});
//
//            setListAdapter(adapter);
//
//            // selecting single ListView item
//            ListView lv = getListView();
//            // listening to single listitem click
//            lv.setOnItemClickListener(new OnItemClickListener() {
//
//                @Override
//                public void onItemClick(AdapterView<?> parent, View view,
//                                        int position, long id) {
//                    // getting listitem index
//                    int songIndex = position;
//                    // Starting new intent
//                    Intent in = new Intent();
//                    // Sending songIndex to PlayMusicActivity
//                    in.putExtra("songOfflineIndex", songIndex);
//                    setResult(100, in);
//                    finish();
//                }
//            });
//        }
////
//    }

    private void toastMessage(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

}
