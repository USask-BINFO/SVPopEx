package com.javafxapp;

public class Selection {
    private double startX; // start X coordinate of the start of the selection rectangle relative to parent container
    private double endX; // end X coordinate of the end of the selection rectangle relative to the parent container
    private double length; // X coordinate difference (pixel length) of selection
    private double zoomLevel;
    private double genomicStart;
    private double genomicEnd;
    private String chromosome;
    // constructor
    public Selection(double startX, double endX, String chromosome, double zoomLevel) {
        this.startX = startX;
        this.endX = endX;
        this.length = endX - startX;
        this.zoomLevel = zoomLevel;
        this.genomicStart = this.startX/this.zoomLevel;
        this.genomicEnd = this.endX/this.zoomLevel;
        this.chromosome = chromosome;
        System.out.println("Genomic Start: " + genomicStart + ", Genomic End: " + genomicEnd + ", Pixel Start: " + startX + ", Pixel End: " + endX);
    }

    /**
     * Gets information about Selection
     * @return String containing information about Selection
     */
    public String toString() {
        return "Genomic Start: " + genomicStart + ", Genomic End: " + genomicEnd + ", Pixel Start: " + startX + ", Pixel End: " + endX;
    }

    /**
     * Gets pixel start coordinate for Selection
     * @return double value for pixel start coordinate
     */
    public double getPixelStart() {
        return this.startX;
    }

    /**
     * Gets pixel length of Selection
     * @return double value for pixel length of Selection
     */
    public double getPixelLength() {
        return this.length;
    }

    /**
     * Get start coordinate of Selection
     * @return double value for Selection start coordinate
     */
    public double getGenomicStart() {
        return this.genomicStart;
    }

    /**
     * Get end coordinate of Selection
     * @return double value for Selection end coordinate
     */
    public double getGenomicEnd() {
        return this.genomicEnd;
    }

    /**
     * Get zoom level at the time Selection was made
     * @return double value representing zoom level when the Selection was made
     */
    public double getZoomLevel() {
        return this.zoomLevel;
    }

    /**
     * Get name for the Chromosome the Selection was made in
     * @return String representing name of Chromosome the Selection was made
     */
    public String getChromosome() {
        return this.chromosome;
    }
}
