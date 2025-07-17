package com.javafxapp;

public class Selection {
    // NOTE THAT THESE ARE THE VIEWPORT DISTANCES/COORDS NOT GENOMIC
    private double startX;
    private double endX;
    private double length;
    public Selection(double startX, double endX) {
        this.startX = startX;
        this.endX = endX;
        this.length = endX - startX;
    }
    public String toString() {
        return "Start: " + startX + " , End: " + endX;
    }

    public double getStart() {
        return this.startX;
    }

    public double getEndX() {
        return this.endX;
    }

    public double getLength() {
        return this.length;
    }
}
