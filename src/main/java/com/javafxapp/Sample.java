package com.javafxapp;

import java.util.ArrayList;

public class Sample {
    private String name;
    private ArrayList<Call> allSampleCalls;
    // constructor
    public Sample(String sampleName) {
        this.name = sampleName;
        this.allSampleCalls = new ArrayList<>();
    }

    /**
     * Get Sample name
     * @return String holding Sample name
     */
    public String getName() {
        return this.name;
    }

    public ArrayList<Call> getAllSampleCalls() {
        return allSampleCalls;
    }
}
