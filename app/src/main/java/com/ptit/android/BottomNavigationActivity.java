package com.ptit.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import com.androidhive.musicplayer.R;

public class BottomNavigationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bottom_navigation);
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.actionOnline:
                        Toast.makeText(BottomNavigationActivity.this, "Online", Toast.LENGTH_SHORT).show();
                        Intent online = new Intent(BottomNavigationActivity.this, MainActivity.class);
                        online.addFlags(
                                Intent.FLAG_ACTIVITY_SINGLE_TOP
                        );
                        startActivity(online);
                        System.out.println("ONLINEEEEEEEEEEEE");
                        break;

                    case R.id.actionOffline:
                        Toast.makeText(BottomNavigationActivity.this, "Offline", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.actionPlaying:
                        Toast.makeText(BottomNavigationActivity.this, "Playing", Toast.LENGTH_SHORT).show();
                        Intent playing = new Intent(BottomNavigationActivity.this, PlayMusicActivity.class);
//                        in.addFlags(
//                                Intent.FLAG_ACTIVITY_SINGLE_TOP
//                        );
                        System.out.println("PLAYINGGGG");

                        startActivity(playing);
                        break;
                    case R.id.actionPersonal:
                        Toast.makeText(BottomNavigationActivity.this, "Personal", Toast.LENGTH_SHORT).show();
                        break;

                }
                return true;
            }
        });
    }
}
