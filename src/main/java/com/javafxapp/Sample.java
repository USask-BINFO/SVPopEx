package com.javafxapp;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class Sample {
    private String name;
    private ArrayList<Call> calls = new ArrayList<>();
    private LinkedHashMap<String, ArrayList<Call>> callsByRegion = new LinkedHashMap<>();
    public Sample(String sampleName, LinkedHashMap<String, Chromosome> refChromosomes) {
        this.name = sampleName;
        // add each Chromosome name to callsByRegion hashmap and set value to empty ArrayList
        for (String regionName : refChromosomes.keySet()) {
            callsByRegion.put(regionName, new ArrayList<>());
        }
    }


    public String getName() {
        return this.name;
    }

    public void addCall(String chromosomeName, Call call) {
        System.out.println(" ADDING CALLS FOR CHROMOSOME " + chromosomeName);
        this.calls.add(call);
        callsByRegion.get(chromosomeName).add(call);
    }

    public ArrayList<Call> getCalls() {
        return this.calls;
    }

    public ArrayList<Call> getRegionCalls(String region) {
        return this.callsByRegion.get(region);
    }

}
