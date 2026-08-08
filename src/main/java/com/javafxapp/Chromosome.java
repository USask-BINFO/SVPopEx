package com.javafxapp;

import java.util.ArrayList;
import java.util.HashMap;

public class Chromosome {
    private String name;
    private long length; // length extracted from length in ##contig line
    private long absoluteStart; // start of the chromosome considering all chromosomes previous
    private double pixelWidth;
    private double pixelAbsoluteOffset;
    private int tileEndInterval; // end tile interval for Chromosome (tile intervals range from [1,end] inclusive
    // constructor
    public Chromosome(String name, long length, long absoluteStart, int tileSize) {
        this.name = name;
        this.length = length;
        this.absoluteStart = absoluteStart;
        this.tileEndInterval = Math.toIntExact((length - 1) / tileSize + 1);
    }

    /**
     * Get the end tile interval for Chromosome
     * @return integer of the end tile interval
     */
    public int getTileEndInterval() {
        return this.tileEndInterval;
    }

    /**
     * Get absolute start position for Chromosome (considering all chromosomes previous)
     * @return long value of the absolute start for Chromosome
     */
    public long getAbsoluteStart() {
        return this.absoluteStart;
    }

    /**
     * Get length of Chromosome
     * @return long value for length of Chromosome
     */
    public long getLength() {
        return this.length;
    }

    /**
     * Get name of Chromosome
     * @return String of Chromosome name
     */
    public String getName() {
        return this.name;
    }

    public void setPixelWidth(double pixelWidth) {
        this.pixelWidth = pixelWidth;
    }

    public double getPixelWidth() {
        return this.pixelWidth;
    }

    public void setPixelAbsoluteOffset(double pixelAbsoluteOffset) {
        this.pixelAbsoluteOffset = pixelAbsoluteOffset;
    }

    public double getPixelAbsoluteOffset() {
        return this.pixelAbsoluteOffset;
    }
}
