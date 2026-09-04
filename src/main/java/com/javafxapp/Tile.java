package com.javafxapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Tile {
    private int num; // id/number of the tile (starts from 1,2,....)
    private int start; // genomic start of tile region
    private int end; // genomic end of tile region
    private ArrayList<Call> tileCalls; // ArrayList of all calls for that tile
    private HashMap<String,ArrayList<Feature>> tileFeatures;
    private HashMap<Sample,ArrayList<Call>> sampleCalls; // HashMap holding Samples (key) and ArrayList of Calls (value)
    // constructor
    public Tile(int num, int start, int end) {
        this.num = num;
        this.start = start;
        this.end = end;
        this.tileCalls = new ArrayList<Call>();
        this.tileFeatures = new HashMap<>();
        this.sampleCalls = new HashMap<>();
    }

    /**
     * Adds given Call to given Sample calls
     * @param sample Sample to add Call to
     * @param call Call to add to Sample calls structure
     */
    public void addSampleCall(Sample sample, Call call) {
        // try get sample and add call
        try {
            sampleCalls.get(sample).add(call);
        }
        // if NullPointer, add Sample and empty ArrayList, then add Call
        catch (NullPointerException e) {
            System.err.println("Error: Caught NullPointer when trying to add a call to Sample's arraylist in Tile.");
            sampleCalls.put(sample, new ArrayList<>());
            sampleCalls.get(sample).add(call);
        }
    }

    public void addFeature(Feature currentFeature, String annotationID) {
        // if annotationID already added as key, add feature
        if (this.tileFeatures.containsKey(annotationID)) {
            this.tileFeatures.get(annotationID).add(currentFeature);
        }
        // otherwise, add key to LinkedHashMap and add feature
        else {
            this.tileFeatures.put(annotationID, new ArrayList<>());
            this.tileFeatures.get(annotationID).add(currentFeature);
        }
    }

    public int getEnd() {
        return this.end;
    }

    public int getStart() {
        return this.start;
    }

    public HashMap<String,ArrayList<Feature>> getTileFeatures() {
        return this.tileFeatures;
    }

    /**
     * Method for printing Tile info
     * @return String of Tile info to print
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        // loop through samples
        for (Map.Entry<Sample, ArrayList<Call>> entry : sampleCalls.entrySet()) {
            // add sample name
            sb.append(entry.getKey().getName()).append(":\n");
            // add each call
            for (Call call : entry.getValue()) {
                sb.append("    ").append(call).append("\n");
            }
        }
        return ("Called Tile.toString() for tile " + num + " , start: " + start + " , end: " + end + " SAMPLE CALLS " + sb);
    }

    /**
     * Add call to Tile calls
     * @param call Call to add to Tile calls
     */
    public void add(Call call) {
        this.tileCalls.add(call);
    }

    /**
     * Gets all Tile calls
     * @return ArrayList holding all Tile calls
     */
    public ArrayList<Call> getTileCalls() {
        return this.tileCalls;
    }

    /**
     * Gets Sample calls for Tile
     * @return Map holding Sample and associated ArrayList with calls
     */
    public HashMap<Sample,ArrayList<Call>> getSampleCalls() {
        return this.sampleCalls;
    }
}
