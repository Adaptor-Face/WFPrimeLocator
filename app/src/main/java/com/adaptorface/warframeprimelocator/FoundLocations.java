package com.adaptorface.warframeprimelocator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

public class FoundLocations extends AppCompatActivity {
    TreeMap<String, TreeMap<String, ArrayList<String>>> relicLocation = null;
    HashMap<String, String> locations = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_found_locations);
        Intent intent = getIntent();
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        ViewGroup layout = (ViewGroup) findViewById(R.id.activity_found_locations);
        ArrayList<String> checkedList = (ArrayList<String>) intent.getSerializableExtra("checkedBoxes");
        File file = new File(getFilesDir().getAbsolutePath(), "MissionDecks.txt");
        GenerateLists generateLists = new GenerateLists(file);
        HashSet<String> primes = new HashSet<>(generateLists.getPrimes());
        HashSet<String> relics = new HashSet<>(generateLists.getRelics());
        locations = new HashMap<>(generateLists.getLocations());
        TreeMap<String, ArrayList<String>> primeRelic = generateLists.getPrimeRelic(primes, relics);
        relicLocation = generateLists.getRelicLocation(relics, locations.keySet());
        int i = 0;
        for (String prime : checkedList) {
            RadioGroup relicGroup = new RadioGroup(this);
            final RadioGroup.OnCheckedChangeListener ToggleListener = new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(final RadioGroup radioGroup, final int i) {
                    for (int j = 0; j < radioGroup.getChildCount(); j++) {
                        final ToggleButton view = (ToggleButton) radioGroup.getChildAt(j);
                        view.setChecked(view.getId() == i);
                    }
                }
            };
            relicGroup.setOnCheckedChangeListener(ToggleListener);
            relicGroup.setOrientation(LinearLayout.HORIZONTAL);
            TableLayout subLayout = new TableLayout(this);
            HashSet<String> relicsNeeded = new HashSet<>();
            String printString = prime + " drops from the following relics:   (click on the relics to show where to get them)";
            final TextView textView = new TextView(this);
            subLayout.addView(textView);
            subLayout.addView(relicGroup);
            for (String relic : primeRelic.get(prime)) {
                final ToggleButton relicToggle = new ToggleButton(this);
                final TextView relicText = new TextView(this);
                relicText.setTextColor(Color.parseColor("#000000"));
                relicToggle.setText(relic);
                relicToggle.setTextOff(relic);
                relicToggle.setTextOn(relic);
                relicText.setMaxLines(0);
                relicToggle.setId(i);
                textView.setId(i);
                relicText.setText(findLocation(relicToggle));
                i++;
                relicToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
                        if(isChecked) {
                            relicText.setMaxLines(Integer.MAX_VALUE);
                        }
                        else {
                            relicText.setMaxLines(0);

                        }
                    }
                });
                relicToggle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((RadioGroup) view.getParent()).check(view.getId());
                    }
                });
                relicGroup.addView(relicToggle);
                subLayout.addView(relicText);
            }
            textView.setText(printString);
            textView.setClickable(true);
            textView.setPadding(0, 0, 0, 10);
            textView.setFocusableInTouchMode(false);
            textView.setMaxLines(7);
            textView.setTextColor(Color.parseColor("#000000"));
            layout.addView(subLayout);

        }
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.app_bar, menu);
        return true;
    }

    private String findLocation(ToggleButton toggle) {
        String relic = toggle.getText().toString();
        String printString = "";
        boolean hasDropLocation = false;
        for (Map.Entry<String, TreeMap<String, ArrayList<String>>> relicLocationEntry : relicLocation.entrySet()) {
            String location = relicLocationEntry.getKey();
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            Set<String> planetSet = sharedPref.getStringSet("planet", new HashSet<String>());
            boolean alreadyFoundInLocation = false;
            for(String planet : planetSet) {
                if(location.contains(planet)) {
                    TreeMap<String, ArrayList<String>> tm = relicLocationEntry.getValue();
                    for (Map.Entry<String, ArrayList<String>> ee : tm.entrySet()) {
                        String rotation = ee.getKey();
                        ArrayList<String> array = ee.getValue();
                        if (alreadyFoundInLocation) {
                            printString += ", " + rotation;
                        } else if (array.contains(relic)) {
                            hasDropLocation = true;
                            String loc = locations.get(location);
                            String cap = loc.substring(0, 1).toUpperCase() + loc.substring(1);
                            Set<String> missionSet = sharedPref.getStringSet("mission_type", new HashSet<String>());
                            for (String missionType : missionSet) {
                                if (missionType.contains(cap)) {
                                    printString += location + "   " + cap + " Rotation: " + rotation;
                                    alreadyFoundInLocation = true;
                                }
                            }
                        }
                    }
                }
            }
            if (alreadyFoundInLocation) {
                printString += "\n";
            }
        }if(printString.trim().isEmpty() && !hasDropLocation){
            return relic + " " + getString(R.string.relic_not_found);
        } else if(printString.trim().isEmpty() && hasDropLocation) {
            return relic + " " + getString(R.string.relic_found_no_settings_match);
        }
        return printString;
    }
}