package com.example.awa.drcough2;

import android.app.AlertDialog;
import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
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
import java.security.Provider;

public class Service1 extends IntentService {
    static String stringUrl = "http://188.226.248.47:8888/";
    String messageToServer = "nimic";
    private final String TAG = "myLogs";
    static String audioData;
    static String survey = "noDATAforSURVEY";
    static volatile int diagnoseID = 0;
    static volatile String probability = "0";

    int myBufferSize = 8192;
    AudioRecord audioRecord = null;

    int sampleRate = 8000;
    int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    int audioFormat = AudioFormat.ENCODING_PCM_16BIT;

    int minInternalBufferSize = AudioRecord.getMinBufferSize(sampleRate,
            channelConfig, audioFormat);
    int internalBufferSize = minInternalBufferSize * 4;

    boolean isReading = false;

    public static final String COMMAND = "command";
    public static final String SURVEYDATA = "no data";
    public static final String[] diseases = {"angina", "tuberculosis", "bronchitis",
            "flu", "sinusitis", "pneumonia", "pharyngitis", "rhinitis", "tracheitis", "tonsillitis"};

    byte[] header = new byte[44];

    public Service1(){
        super("Service1");
    }
    public void onCreate() {
        //isReading = false;
       // recordStop();
        //audioRecord.release();
        if (audioRecord != null) {
            audioRecord.release();
        }
        createAudioRecorder();
        Log.e(TAG, "init state = " + audioRecord.getState());
    }

    protected void onHandleIntent(Intent intent) {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //TODO do something useful
        Log.e(TAG, "OnstartCommand");
        String command = intent.getStringExtra(COMMAND);
        survey = intent.getStringExtra(SURVEYDATA);
        Log.e(TAG, "Command:"+command);
        if(command.equals("start")){
            Log.e(TAG, "touch1");
            recordStart();
            Log.e(TAG, "touch2");
        }
        else
        if(command.equals("stop")) {
            Log.e(TAG, "run");
            recordStop();
            Log.e(TAG, "release2");
            ///////////////////
            writeHeader();
            Log.e("AUDIO=", messageToServer);
            audioData = readAudioData();
            Log.e("AudioData=", audioData);
            /////////////////////////
        }
        else
        if(command.equals("diagnose")){
            Intent intentActivity = new Intent(this, ShowResults.class);
            intentActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            intentActivity.putExtra(ShowResults.DIAGNOSE, diseases[diagnoseID]);
            //!!!!!int!!!!!!!!!!!!!!
            intentActivity.putExtra(ShowResults.PROBABILITY, probability);

            sendData();
            ///!!!!!!!!!!!!!!!!
            startActivity(intentActivity);
        }
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        //TODO for communication return IBinder implementation
        return null;
    }


    String readAudioData() {
        byte[] myBuffer = new byte[myBufferSize];
        audioRecord.read(myBuffer, 0, myBufferSize);
        String audioData = new String(myBuffer);
        return audioData;
    }

    void createAudioRecorder() {
        isReading = false;
        if (audioRecord != null) {
            audioRecord.release();
        }


        Log.d(TAG, "minInternalBufferSize = " + minInternalBufferSize
                + ", internalBufferSize = " + internalBufferSize
                + ", myBufferSize = " + myBufferSize);

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                sampleRate, channelConfig, audioFormat, internalBufferSize);

    }


    public void recordStart() {
        Log.d(TAG, "record start");
        audioRecord.startRecording();
        int recordingState = audioRecord.getRecordingState();
        Log.d(TAG, "recordingState = " + recordingState);
    }

    public void recordStop() {
        Log.d(TAG, "record stop");
        audioRecord.stop();
    }

    public void sendData() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadWebpageTask().execute(stringUrl);
        } else {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("No network connection available.")
                    .setTitle("ERROR");
            AlertDialog dialog = builder.create();
        }
    }


    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl();
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }
    }


    private void writeHeader() {
        int channels = 2;
        long byteRate = 16 * sampleRate * channels / 8;
        ////////
        int totalAudioLen = 10000;
        /////////


        header[0] = 'R';  // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (myBufferSize & 0xff);
        header[5] = (byte) ((myBufferSize >> 8) & 0xff);
        header[6] = (byte) ((myBufferSize >> 16) & 0xff);
        header[7] = (byte) ((myBufferSize >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';  // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;  // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (sampleRate & 0xff);
        header[25] = (byte) ((sampleRate >> 8) & 0xff);
        header[26] = (byte) ((sampleRate >> 16) & 0xff);
        header[27] = (byte) ((sampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8);  // block align
        header[33] = 0;
        header[34] = 16;  // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        messageToServer = new String(header);
        //messageToServer = header.toString();

    }

    private String downloadUrl() throws IOException {
        InputStream is = null;
        OutputStream os = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 500;
        Log.e("SERVER:", "Trying to send data to server");
        try {
            byte[] myBuffer = new byte[myBufferSize];
            int readCount = 0;
            int totalCount = 0;
            while (isReading) {
                readCount = audioRecord.read(myBuffer, 0, myBufferSize);
                totalCount += readCount;
                Log.d(TAG, "readCount = " + readCount + ", totalCount = "
                        + totalCount);
            }

            URL url = new URL(stringUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));

            writeHeader();
           // Log.e("MESSAGETOSERVER", messageToServer);
            Log.e("AudioData=", audioData);
            String message = "survey=" + survey  + "&audiodata=" + messageToServer + audioData;
            int hash = message.hashCode();

            Log.e("SERVER:", "request 1:" + message);
            writer.write(message);
            Log.d("SERVER:", "message is: " + message + " hash is:" + hash);

            writer.flush();
            writer.close();
            os.close();

            // Starts the query
            //  conn.connect();

            int response = conn.getResponseCode();
            Log.d("SERVER:", "The response is: " + response);
            //textView.append("The response is: " + response);
            is = conn.getInputStream();

            // Convert the InputStream into a string
            String contentAsString = readIt(is, len);

            diagnoseID = Integer.parseInt(contentAsString.substring(0, contentAsString.indexOf(" ")));
            probability = contentAsString.substring(contentAsString.lastIndexOf(" "), contentAsString.length()-1);
            return contentAsString;

            // Makes sure that the InputStream is closed after the app is finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }


    public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }
/*
    public String byteToString(byte[] myArray, int size) {
        String buffer = "";
        for (int i = 0; i < size; i++) {
            buffer += (char)myArray[i];
        }
        return buffer;
    }
*/
}
