package com.adaptorface.warframeprimelocator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;

/**
 * Created by Face on 08.10.2016.
 */
public class GenerateLists {
    File file = null;
    public GenerateLists(File file){
        this.file = file;
    }
    public ArrayList<String> getPrimes (){
        ArrayList<String> stack = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = "not read";
            while ((line = br.readLine()) != null) {
                if(line.contains("PRIME")) {
                    String[] lineSplit = line.split(" 1 ");
                    lineSplit = lineSplit[1].split(", ");
                    stack.add(lineSplit[0]);
                }
            }
            br.close();
        }
        catch (IOException e) {
            //You'll need to add proper error handling here
        }
        return stack;
    }

    public ArrayList<String> getRelics (){
        ArrayList<String> stack = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = "not read";
            while ((line = br.readLine()) != null) {
                if (line.contains("AXI") || line.contains("NEO") || line.contains("MESO") || line.contains("LITH")) {
                    line = line.trim();
                    line = line.substring(2);
                    String[] lineSplit = line.split(" RELIC");
                    stack.add(lineSplit[0]);
                }
            }
            br.close();
        }
        catch (IOException e) {
            //You'll need to add proper error handling here
        }
        return stack;
    }

    public HashMap<String, String> getLocations (){
        HashMap<String, String> stack = new HashMap<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = "not read";
            while ((line = br.readLine()) != null) {
                if (line.contains("[")) {
                    while(((line = br.readLine()) != null) && line.startsWith(" - ") && !line.contains("RELIC")){
                        line = line.substring(3);
                        String[] strings = line.split(", MT_");
                        String[] args = strings[1].split(", ");
                        String mission = args[0].toLowerCase().replace("_", " ").replace("intel", "spy").replace("territory", "interception").replace("excavate", "excavation");
                        stack.put(strings[0], mission);
                    }
                }
            }
            br.close();
        }
        catch (IOException e) {
            //You'll need to add proper error handling here
        }
        return stack;
    }

    public TreeMap<String, TreeMap<String, ArrayList<String>>> getRelicLocation(HashSet<String> relic, Set<String> location){
        TreeMap<String, TreeMap<String, ArrayList<String>>> hm = new TreeMap<>();
        for (String string : location) {
            TreeMap<String, ArrayList<String>> treeMap = new TreeMap<>();
            treeMap.put("A", new ArrayList<String>());
            treeMap.put("C", new ArrayList<String>());
            treeMap.put("B", new ArrayList<String>());
            hm.put(string, treeMap);
        }
        ArrayList<String> table = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = "not read";
            while ((line = br.readLine()) != null) {
                table.add(line);
            }
            br.close();
        }
        catch (IOException e) {
            //You'll need to add proper error handling here
        }
        for(String locationString : location){
            Iterator<String> it = table.iterator();
            while (it.hasNext()){
                String string = it.next();
                if (string.contains("- " + locationString)){
                    boolean next = false;
                    String rotation = "";
                    while (it.hasNext() && !next) {
                        String relicFind = it.next();
                        if (relicFind.startsWith("Rotation ")){
                            rotation = relicFind.substring(9, 10);
                        }
                        if (!(relicFind.contains("Stripped") || relicFind.equals(""))) {
                            Iterator<String> itr = relic.iterator();
                            while (itr.hasNext() && !next) {
                                String relicString = itr.next();
                                if (relicFind.contains("[")) {
                                    next = true;
                                }
                                if (relicFind.contains(relicString)) {
                                    TreeMap<String, ArrayList<String>> treeMap = hm.get(locationString);
                                    ArrayList<String> arrayList = treeMap.get(rotation);
                                    arrayList.add(relicString);
                                    hm.remove(locationString);
                                    hm.put(locationString, treeMap);
                                }
                            }
                        }
                    }
                }
            }
        }
        Iterator<String> hmRemove = hm.keySet().iterator();
        while(hmRemove.hasNext()){
            String locationToRemove = hmRemove.next();
            boolean rotationsHasEntry = false;
            for (ArrayList<String> arrayList : hm.get(locationToRemove).values()){
                if(!arrayList.isEmpty()){
                    rotationsHasEntry = true;
                }
            }
            if(!rotationsHasEntry){
                hmRemove.remove();
            }
        }
        return hm;
    }
    public TreeMap<String, ArrayList<String>> getPrimeRelic(HashSet<String> prime, HashSet<String> relic){
        TreeMap<String, ArrayList<String>> hm = new TreeMap<>();
        for (String string : prime) {
            ArrayList<String> arrayList = new ArrayList<>();
            hm.put(string, arrayList);
        }
        ArrayList<String> table = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = "not read";
            while ((line = br.readLine()) != null) {
                table.add(line);
            }
            br.close();
        }
        catch (IOException e) {
            //You'll need to add proper error handling here
        }
        for(String relicString : relic){
            Iterator<String> it = table.iterator();
            while (it.hasNext()){
                String string = it.next();
                if (string.contains("- " + relicString)){
                    boolean next = false;
                    while (it.hasNext() && !next) {
                        String primeFind = it.next();
                        Iterator<String> itp = prime.iterator();
                        while(itp.hasNext() && !next){
                            String primeString = itp.next();
                            if (primeFind.contains("[")){
                                next = true;
                            }
                            if (primeFind.contains(primeString)) {
                                ArrayList<String> arrayList = hm.get(primeString);
                                arrayList.add(relicString);
                                hm.remove(primeString);
                                hm.put(primeString, arrayList);
                            }
                        }
                    }
                }
            }
        }
        return hm;
    }
}
