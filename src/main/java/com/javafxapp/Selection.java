package com.javafxapp;

public class Selection {
    // NOTE THAT THESE ARE THE VIEWPORT DISTANCES/COORDS NOT GENOMIC
    private double startX;
    private double endX;
    private double length;
    private double zoomLevel;
    private double genomicStart;
    private double genomicEnd;
    private String chromosome;
    public Selection(double startX, double endX, String chromosome, double zoomLevel) {
        this.startX = startX;
        this.endX = endX;
        this.length = endX - startX;
        this.zoomLevel = zoomLevel;
        this.genomicStart = this.startX/this.zoomLevel;
        this.genomicEnd = this.endX/this.zoomLevel;
        this.chromosome = chromosome;
    }
    public String toString() {
        System.out.println("START: " + String.format("%.0f", genomicStart));
        System.out.println("END: " + String.format("%.0f", genomicEnd));
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

    public double getGenomicStart() {
        return this.genomicStart;
    }

    public double getGenomicEnd() {
        return this.genomicEnd;
    }

    public double getZoomLevel() {
        return this.zoomLevel;
    }

    public String getChromosome() {
        return this.chromosome;
    }
}
