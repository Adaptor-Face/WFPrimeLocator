package com.adaptorface.warframeprimelocator;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TableRow;

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
    HashSet<String> relics = null;
    HashMap<String, String> locations = null;
    TreeMap<String, ArrayList<String>> primeRelic = null;
    TreeMap<String, TreeMap<String, ArrayList<String>>> relicLocation = null;
    TreeMap<String, CheckBox> checkBoxes = new TreeMap<>();
    GenerateLists generateLists = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fetchDropLocations();
        generateDataSheetService();
        print();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findDropLocations(view);

            }
        });
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
    private void printLocations(){
        for (String location : relicLocation.keySet()) {
            String string = location + ", " + locations.get(location);
            for (String rotation : relicLocation.get(location).keySet()) {
                if(!(relicLocation.get(location).get(rotation).isEmpty())) {
                    string += " Rot: " + rotation;
                    for(String relic : relicLocation.get(location).get(rotation)) {
                        string += ", " + relic;
                    }
                }
            }
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
    private void generateDataSheetService() {
        File file = new File (getFilesDir().getAbsolutePath(), "MissionDecks.txt");
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

    private void createList(String string) {
        ViewGroup tableLayout = (ViewGroup) findViewById(R.id.tableLayout);
        CheckBox checkBox = new CheckBox(this);
        checkBox.setText(string);
        checkBoxes.put(string, checkBox);
        TableRow tableRow = new TableRow(this);
        tableRow.addView(checkBox);
        tableLayout.addView(tableRow);
    }

    private void generateDataSheet() {
        File file = new File (getFilesDir().getAbsolutePath(), "MissionDecks.txt");
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = "not read";
            Boolean relicFound = false;
            Boolean test = true;
            while ((line = br.readLine()) != null) {
                if (line.contains("[")) {
                    BufferedReader tbr = br;
                    String lineCheck = "not read";
                    while (!(lineCheck = tbr.readLine()).contains("[")) {
                        if(test){
                            createList(lineCheck);
                            test = false;
                        }
                        if (lineCheck.toLowerCase().contains("axi") || lineCheck.toLowerCase().contains("neo") || lineCheck.toLowerCase().contains("meso") || lineCheck.toLowerCase().contains("lith")) {
                            relicFound = true;
                        }
                    }
                }
                if (relicFound) {
                    if (line.contains("[")) {
                        String strArr[] = line.split("");
                        StringBuilder strBuilder = new StringBuilder();
                        for (int i = 0; i < strArr.length; i++) {
                            if (!(strArr[i].contains("[") || strArr[i].contains("]"))) {
                                strBuilder.append(strArr[i]);
                            }
                        }
                        String newString = strBuilder.toString();
                        String listElementString = newString.replaceAll("(.)([A-Z])", "$1 $2");
                        createList(listElementString);
                    } else if ((!(line.isEmpty())) && line.contains("-")) {
                        String strArr[] = line.split(", MT_");
                        String mission[] = strArr[0].split("-");
                        createList(mission[1]);
                    } else {
                        relicFound = false;
                    }
                }
            }
            br.close();
        }
        catch (IOException e) {
            //You'll need to add proper error handling here
        }
    }
}
