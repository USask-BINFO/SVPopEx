package com.javafxapp;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Objects;

public class Sample {
    private String name;
    private ArrayList<Call> calls = new ArrayList<>();
    private LinkedHashMap<String, ArrayList<Call>> callsByChromosome = new LinkedHashMap<>();
    private HashMap<String, HashMap<Integer, ArrayList<Call>>> tiledCalls = new HashMap<>();
    public Sample(String sampleName, LinkedHashMap<String, Chromosome> refChromosomes) {
        this.name = sampleName;
        // add each Chromosome name to callsByRegion hashmap and set value to empty ArrayList
        for (String regionName : refChromosomes.keySet()) {
            callsByChromosome.put(regionName, new ArrayList<>());
        }
    }


    public String getName() {
        return this.name;
    }

    public void addCall(String chromosomeName, Call call) {
        // all calls
        this.calls.add(call);
        // calls for the chromosome
        callsByChromosome.get(chromosomeName).add(call);
    }

    public void addToTiledCalls(String chromosomeName, Call call, int tileSize) {
        if (tiledCalls.containsKey(chromosomeName)) {
            // do nothing, already contains chromosome hashmap
        } else {
            tiledCalls.put(chromosomeName, new HashMap<>());
        }
        int start = call.getStart();
        int end = call.getEnd();
        int startInterval = (start - 1) / tileSize + 1;
        int endInterval = (end - 1) / tileSize + 1;
        for (int i=startInterval; i<endInterval+1; i++) {
            if (tiledCalls.get(chromosomeName).containsKey(i)) {
                // do nothing
            }
            else {
                tiledCalls.get(chromosomeName).put(i, new ArrayList<>());
            }
            tiledCalls.get(chromosomeName).get(i).add(call);
        }
    }

    public HashMap<String, HashMap<Integer, ArrayList<Call>>> getTiledCalls() {
//        System.out.println("SHOWING TILED CALLS");
//        for (String chr : tiledCalls.keySet()) {
//            HashMap<Integer, ArrayList<Call>> tiles = tiledCalls.get(chr);
//
//            System.out.println(chr);
//
//            for (Integer tile : tiles.keySet()) {
//                int count = tiles.get(tile) == null ? 0 : tiles.get(tile).size();
//                System.out.println("  tile " + tile + ": " + count + " calls");
//            }
//        }
        return this.tiledCalls;
    }




    public ArrayList<Call> getCalls() {
        return this.calls;
    }

    public ArrayList<Call> getChromosomeCalls(String chrom) {
        return this.callsByChromosome.get(chrom);
    }

}
