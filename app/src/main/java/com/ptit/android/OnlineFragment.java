package com.ptit.android;

import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.androidhive.musicplayer.R;

public class OnlineFragment extends ListFragment {
    private String TAG = "FIREBASE";
    private static String STORE_FIREBASE_SERVER = "https://firebasestorage.googleapis.com/v0/b/musicapplication-f21a5.appspot.com/o/";
    private Button btnSearch;
    private ListView lvSong;
    private EditText edtSearch;
    private ArrayAdapter<String> adapter;
    private ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();
    private int currentSongIndex = 0;
    private String txtSearch;
    private Long ONLINE_MODE = 2L;
    private Long typeSearch;
    PlayMusicFragment playMusicFragment = new PlayMusicFragment();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.online_activity, container, false);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        edtSearch = view.findViewById(R.id.txtSearch);

        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1);
        lvSong = getListView();
        // Getting all songs list
//        Intent intent = getIntent();
//        currentSongIndex = intent.getExtras().getInt("songIndex");
//        txtSearch = intent.getExtras().getString("txtSearch");
//        typeSearch = intent.getExtras().getLong("typeSearch");
        if (typeSearch == null) {
            typeSearch = Constants.SEARCH_TYPE.TITLE;
        }
//        performSearch(txtSearch);
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch(edtSearch.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        lvSong.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // getting listitem index
                int songIndex = position;
                // Starting new intent
                txtSearch = edtSearch.getText().toString();
                Bundle bundle = new Bundle();
                bundle.putInt("songIndex", songIndex);
                bundle.putString("txtSearch", txtSearch);
                bundle.putLong("MODE", Constants.MODE.ONLINE);
                playMusicFragment = new PlayMusicFragment();
                playMusicFragment.setArguments(bundle);
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, playMusicFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
//                Intent in = new Intent(OnlineFragment.this, PlayMusicActivity.class);
//                // Sending songIndex to PlayMusicActivity
//                in.putExtra("songOnlineIndex", songIndex);
//                in.putExtra("txtSearch", txtSearch);
//                in.putExtra("MODE", ONLINE_MODE);
//                setResult(Constants.MODE.ONLINE.intValue(), in);
//                finish();
            }
        });

//        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
//        lvSong = getListView();
//        // Getting all songs list
//        Intent intent = getIntent();
//        currentSongIndex = intent.getExtras().getInt("songIndex");
//        txtSearch = intent.getExtras().getString("txtSearch");
//        typeSearch = intent.getExtras().getLong("typeSearch");
//        if(typeSearch == null) {
//            typeSearch = Constants.SEARCH_TYPE.TITLE;
//        }
//        performSearch(txtSearch);
//
//        lvSong.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                // getting listitem index
//                int songIndex = position;
//                // Starting new intent
//                Intent in = new Intent(OnlineFragment.this, PlayMusicActivity.class);
//                // Sending songIndex to PlayMusicActivity
//                in.putExtra("songOnlineIndex", songIndex);
//                in.putExtra("txtSearch", txtSearch);
//                in.putExtra("MODE", ONLINE_MODE);
//                setResult(Constants.MODE.ONLINE.intValue(), in);
//                finish();
//            }
//        });
    }

//    }
//
////        btnSearch.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View v) {
////                txtSearch = edtSearch.getText().toString();
////                if (txtSearch != null && !txtSearch.isEmpty()) {
////                    Intent intent = new Intent(MainActivity.this, OnlineFragment.class);
////                    intent.putExtra("txtSearch", txtSearch);
////                    startActivity(intent);
////                }
////            }
////        });
//
//    }

    public void performSearch(String txtSearch) {
        SongsManager songsManager = new SongsManager();
            songsManager.readData(txtSearch, typeSearch, new SongsManager.MyCallback() {
                @Override
                public void onCallback(ArrayList<HashMap<String, String>> songList) {
                    System.out.println("size songlist:" + songList.size());
                    ListAdapter adapter = new SimpleAdapter(getActivity(), songList,
                            R.layout.playlist_item, new String[]{"songTitle"}, new int[]{
                            R.id.songTitle});
                    setListAdapter(adapter);
                }
            });
        }

}
