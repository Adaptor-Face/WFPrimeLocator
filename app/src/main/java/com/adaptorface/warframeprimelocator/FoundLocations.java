package com.adaptorface.warframeprimelocator;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TableRow;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class FoundLocations extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_found_locations);
        Intent intent = getIntent();
        TextView text = new TextView(this);

        ViewGroup layout = (ViewGroup) findViewById(R.id.activity_found_locations);
        ArrayList<String> checkedList = (ArrayList<String>) intent.getSerializableExtra("checkedBoxes");
        File file = new File (getFilesDir().getAbsolutePath(), "MissionDecks.txt");
        GenerateLists generateLists = new GenerateLists(file);
        HashSet<String> primes = new HashSet<>(generateLists.getPrimes());
        HashSet<String> relics = new HashSet<>(generateLists.getRelics());
        HashMap<String, String> locations = new HashMap<>(generateLists.getLocations());
        TreeMap<String, ArrayList<String>> primeRelic = generateLists.getPrimeRelic(primes, relics);
        TreeMap<String, TreeMap<String, ArrayList<String>>> relicLocation = generateLists.getRelicLocation(relics, locations.keySet());
        for(String prime : checkedList) {
            HashSet<String> relicsNeeded = new HashSet<>();
            String printString = prime + " drops from the following relics:\n";
            for (String relic : primeRelic.get(prime)) {
                if(printString.endsWith("\n")){
                    printString += relic;
                }else {
                    printString += ",  " + relic;
                }
                relicsNeeded.add(relic);
            }
            printString += "\nWhich are found from:\n"; ;
            for(String relic : relicsNeeded) {
                for (Map.Entry<String, TreeMap<String, ArrayList<String>>> relicLocationEntry : relicLocation.entrySet()) {
                    String location = relicLocationEntry.getKey();
                    TreeMap<String, ArrayList<String>> tm = relicLocationEntry.getValue();
                    boolean alreadyFoundInLocation = false;
                    for (Map.Entry<String, ArrayList<String>> ee : tm.entrySet()) {
                        String rotation = ee.getKey();
                        ArrayList<String> array = ee.getValue();
                        if(alreadyFoundInLocation){
                            printString += ", " + rotation;
                        }else if (array.contains(relic)){
                            printString += location+ " " + locations.get(location) + " Rot: " + rotation;
                            alreadyFoundInLocation = true;
                        }
                    }
                    if(alreadyFoundInLocation) {
                        printString += "\n";
                    }
                }
            }
            final TextView textView = new TextView(this);
            textView.setText(printString);
            textView.setClickable(true);
            textView.setPadding(0, 0, 0, 10);
            textView.setFocusableInTouchMode(false);
            textView.setMaxLines(7);
            textView.setTextColor(Color.parseColor("#000000"));
            layout.addView(textView);
        }
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
