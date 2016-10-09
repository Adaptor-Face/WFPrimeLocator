package com.adaptorface.warframeprimelocator;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.TreeSet;

public class MainActivity extends AppCompatActivity {
    HashSet<String> primes = null;
    HashMap<String, String> locations = null;
    TreeMap<String, TreeMap<String, ArrayList<String>>> relicLocation = null;
    TreeMap<String, CheckBox> checkBoxes = new TreeMap<>();
    GenerateLists generateLists = null;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.app_bar, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_settings:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivityForResult(i, 1);
                break;

        }

        return true;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        File file = new File(getFilesDir().getAbsolutePath(), "MissionDecks.txt");
        if (!file.exists()) {
            fetchDropLocations();
        }
        generateDataSheetService(file);
        if (primes.isEmpty()) {
            printConnectionError();
        } else {
            print();
            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    findDropLocations(view);

                }
            });
        }
    }

    private void printConnectionError() {
        ViewGroup tableLayout = (ViewGroup) findViewById(R.id.tableLayout);
        TextView textView = new TextView(this);
        textView.setText("Could not download data file.\nConnect to the Internet and try again.");
        textView.setTextColor(Color.parseColor("#000000"));
        textView.setTextSize(18);
        tableLayout.addView(textView);
    }

    private void findDropLocations(View view){
        Intent intent = new Intent(this, FoundLocations.class);
        ArrayList<String> checkedList = new ArrayList<>();
        for(CheckBox checkBox : checkBoxes.values()){
            if(checkBox.isChecked()){
                checkedList.add(checkBox.getText().toString());
            }
        }
        intent.putExtra("checkedBoxes", checkedList);
        intent.putExtra("relicLocation", relicLocation);
        startActivity(intent);

    }
    private void print() {
        for (String string : primes){

            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(string);
            checkBoxes.put(string, checkBox);
        }
        for (CheckBox cb : checkBoxes.values()) {
            ViewGroup tableLayout = (ViewGroup) findViewById(R.id.tableLayout);
            TableRow tableRow = new TableRow(this);
            tableRow.addView(cb);
            tableLayout.addView(tableRow);
        }
    }
    private void generateDataSheetService(File file) {
        generateLists = new GenerateLists(file);
        primes = new HashSet<>(generateLists.getPrimes());
    }
// generateLists commit
    private void fetchDropLocations() {

// instantiate it within the onCreate method
        Intent intent = new Intent(this, DownloadService.class);
        intent.putExtra("url", "https://raw.githubusercontent.com/VoiDGlitch/WarframeData/master/MissionDecks.txt");
        intent.putExtra("receiver", new DownloadReceiver(new Handler()));
        startService(intent);


    }
    class DownloadReceiver extends ResultReceiver{
        ProgressDialog mProgressDialog = new ProgressDialog(MainActivity.this);
        public DownloadReceiver(Handler handler) {
            super(handler);
            mProgressDialog.setMessage("A message");
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setCancelable(true);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            if (resultCode == DownloadService.UPDATE_PROGRESS) {
                int progress = resultData.getInt("progress");
                mProgressDialog.setProgress(progress);
                if (progress == 100) {
                    mProgressDialog.dismiss();
                }
            }
        }
    }
}
