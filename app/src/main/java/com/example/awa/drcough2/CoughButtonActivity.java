package com.example.awa.drcough2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.app.PendingIntent.getActivity;

public class CoughButtonActivity extends Activity {
    private Button mic;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        setContentView(R.layout.activity_cough_button);



        mic = (Button) findViewById(R.id.record);
        mic.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        //    onPlay(false);
                        startRecordingService("start");
                        break;
                    case MotionEvent.ACTION_UP:
                        startRecordingService("stop");
                        startSurvey();
                        break;
                }
                return false;
            }
        });

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
/*
        isReading = false;
        if (audioRecord != null) {
            audioRecord.release();
        }*/
    }

    public void startSurvey(){
        Intent intent = new Intent(this, SurveyActivity.class);
        startActivity(intent);
    }



    public void startRecordingService(String command){
        Intent intentService = new Intent(this, Service1.class);
        intentService.putExtra(Service1.COMMAND, command);
        startService(intentService);
    }
}