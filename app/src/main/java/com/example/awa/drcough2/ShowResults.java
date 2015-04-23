package com.example.awa.drcough2;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;


public class ShowResults extends ActionBarActivity {
    public static final String DIAGNOSE = "flu";
    public static final String PROBABILITY = "0";
    TextView diagnView;
    TextView probView;
    TextView adviceView;
    String diagnose, probability;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        diagnose = intent.getStringExtra(DIAGNOSE);
        probability = intent.getStringExtra(PROBABILITY);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        Log.e("DIAGNOSE=:", intent.getStringExtra(DIAGNOSE));
        Log.e("PROBABILITY=:", intent.getStringExtra(PROBABILITY));
        setContentView(R.layout.activity_show_results);
        diagnView = (TextView) findViewById(R.id.diagnoseText);
        diagnView.setText("You probably have "+ diagnose + "!");
        probView = (TextView) findViewById(R.id.probabilityText);
        probView.setText("Probability: " + probability + "%");
        adviceView = (TextView) findViewById(R.id.advice);
        if(diagnose.equals("tuberculosis"))
            adviceView.setText("Be careful! You health is at serious risk! "+
                    "You should mind seeing a doctor!\n");
        if(probability.equals("0")){
            diagnView.setText("Good news! You are healthy!");
            probView.setText("Note! This app is just for guidance. It is better to consult a doctor just to be sure 100%");
            adviceView.setText("");
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_show_results, menu);
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
    public void returnToMain(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
