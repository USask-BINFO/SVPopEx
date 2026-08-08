package com.javafxapp;

public class Sample {
    private String name;
    // constructor
    public Sample(String sampleName) {
        this.name = sampleName;
    }

    /**
     * Get Sample name
     * @return String holding Sample name
     */
    public String getName() {
        return this.name;
    }
}
