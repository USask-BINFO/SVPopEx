package com.javafxapp;

public class Chromosome {
    private String name;
    private int length;
    private int absoluteStart;
    private double pixelWidth;
    private double pixelAbsoluteOffset;

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
