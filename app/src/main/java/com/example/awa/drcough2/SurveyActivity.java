package com.example.awa.drcough2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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


public class SurveyActivity extends ActionBarActivity {
    static int cnt=1;
    static int id = 0;
    String serverResponse;
    String diseases[] = {"angina", "tuberculosis", "bronchitis", "flu", "sinusitis", "pneumonia",
            "pharyngitis", "rhinitis", "tracheitis", "tonsillitis"};
    String questions[] = {"Do you feel pain in your throat?", "", "", "", "", "Do you have headache?",
            "Do you feel chills sometimes?", "Do you have swellings in the throat ?", "",
            "Do you have respiratory difficulties?", "", "", "", "Do you feel rapid heartbeat?", "",
    "Do you feel fatigue?", "", "", "", "", "", "", "", "", "", "", "Do you have muscular pain?", "", "", "", "",
            "Do you feel chest pain?", "", "", "", "", "", "", "", "", ""};
    int nrQ = 42;
    TextView question;
    private  String responses="";
    String stringUrl = "http://188.226.248.47:8888/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        cnt = 1;
        setContentView(R.layout.activity_survey);
        Intent intent = getIntent();
        question = (TextView) findViewById(R.id.question);
        question.setText(questions[cnt-1]);
        cnt++;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
       // getMenuInflater().inflate(R.menu.menu_survey, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void yes(View view){
        Button yesButton = (Button) findViewById(R.id.YES);
        Button noButton = (Button) findViewById(R.id.NO);
        Button showResButton = (Button) findViewById(R.id.showResBtn);
        TextView question = (TextView) findViewById(R.id.question);

        boolean not = true;
        while(cnt<nrQ&&not){
            if (questions[cnt-1]=="")
            {
                cnt++;
                responses+="0 ";
            }
            else{
                not = false;
                question.setText(questions[cnt-1]);
                cnt++;
            }
        }
        if(cnt>=nrQ){
            yesButton.setVisibility(View.INVISIBLE);
            noButton.setVisibility(View.INVISIBLE);
            showResButton.setVisibility(View.VISIBLE);
            question.setText("Well done! Are you curious to see the results?");
        }
        responses+="1 ";
    }

    public void no(View view){
        Button yesButton = (Button) findViewById(R.id.YES);
        Button noButton = (Button) findViewById(R.id.NO);
        Button showResButton = (Button) findViewById(R.id.showResBtn);
        TextView question = (TextView) findViewById(R.id.question);

        boolean not = true;
        while(cnt<nrQ&&not){
            if (questions[cnt-1]=="")
            {
                cnt++;
                responses+="0 ";
            }
            else{
                not = false;
                question.setText(questions[cnt-1]);
                cnt++;
            }
        }
        if(cnt>=nrQ){
            yesButton.setVisibility(View.INVISIBLE);
            noButton.setVisibility(View.INVISIBLE);
            showResButton.setVisibility(View.VISIBLE);
            question.setText("Well done! Are you curious to see the results?");
        }
        responses+="0 ";
    }


    public void sendData(View view){
        responses = responses.substring(0, responses.length()-1);
        Log.e("DATAforSERVEER:", "request 1:"+"survey="+responses);
        Intent intentService = new Intent(this, Service1.class);
        intentService.putExtra(Service1.COMMAND, "diagnose");
        intentService.putExtra(Service1.SURVEYDATA, responses);
        startService(intentService);
    }
/*
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

    private String downloadUrl() throws IOException {
        InputStream is = null;
        OutputStream os = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 500;

        try {
            URL url = new URL(stringUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds *);
            conn.setConnectTimeout(15000 /* milliseconds *);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));

            String message = "Diana";
            int hash = message.hashCode();
            responses = responses.substring(0, responses.length()-1);
            Log.e("SERVER:", "request 1:"+"survey="+responses);
            writer.write("survey="+responses);
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
            serverResponse = contentAsString;
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
*/


}
