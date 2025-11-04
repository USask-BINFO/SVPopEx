package com.javafxapp;

public class Chromosome {
    private String name;
    private int length;
    private int absoluteStart;

    /**
     *
     * @param name
     * @param length
     * @param absoluteStart reflects the start position from the start of the REFERENCE
     */
    public Chromosome(String name, int length, int absoluteStart) {
        this.name = name;
        this.length = length;
        this.absoluteStart = absoluteStart;
    }

    public int getAbsoluteStart() {
        return this.absoluteStart;
    }

    public int getLength() {
        return this.length;
    }

    public String getName() {
        return this.name;
    }
}
