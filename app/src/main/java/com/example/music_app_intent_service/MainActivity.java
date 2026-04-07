package com.example.music_app_intent_service;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    ImageButton btnplay, btnstop;
    Boolean flag = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        btnplay = findViewById(R.id.btnplay);
        btnstop = findViewById(R.id.btnstop);

        // set initial icon based on service state
        if (MyService.isPlaying) {
            btnplay.setImageResource(R.drawable.stop);
            flag = false;
        } else {
            btnplay.setImageResource(R.drawable.play);
            flag = true;
        }

        // Handle system back (back button and gestures) using OnBackPressedDispatcher
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Close Activity; service continues as foreground
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

        // xu ly su kien click cho nut play
        btnplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                // make music volume max before starting service
                AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
                if (audioManager != null) {
                    int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, max, 0);
                }

                Intent intent1 = new Intent(MainActivity.this, MyService.class);
                startService(intent1);
                // toggle visuals - service will toggle playback
                if (flag == true) {
                    btnplay.setImageResource(R.drawable.stop);
                    flag = false;
                }
                else
                {
                    btnplay.setImageResource(R.drawable.play);
                    flag = true;
                }
            }
        });
        btnstop.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick (View view) {
                // ask service to stop via action so it can stop foreground and release
                Intent intent2 = new Intent(MainActivity.this, MyService.class);
                intent2.setAction(MyService.ACTION_STOP);
                startService(intent2);

                // update UI and finish Activity (exit app)
                btnplay.setImageResource(R.drawable.play);
                flag = true;
                finish();
            }
        });

    }

}
