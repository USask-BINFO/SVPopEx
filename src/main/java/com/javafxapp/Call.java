package com.javafxapp;

import javafx.scene.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Call {
    private String type; // from SVTYPE
    private int length; // from SVLEN, but this is absolute value regardless of what is in the VCF file
    private String chromosome;
    private String qual;
    private String filter;
    private int start;
    private long absoluteStart;
    private int end; // calculated from start + SVLEN
    private String alternate;
    private String id; // refers to ID field for VCF call
    private Double alleleFreq;
    HashMap<String,String> genotypes = new HashMap<>();
    HashMap<Sample, ArrayList<Node>> nodes = new HashMap<>();
    // constructor
    public Call(String type, int length, String chromosome, String qual, String filter, int start, long absoluteStart, String alternate, String id, HashMap<String,String> genotypes) {
        this.type = type;
        // ensure length is absolute value
        if (Objects.equals(type, "DEL")) {
            this.length = Math.abs(length);
        }
        else {
            this.length = length;
        }
        this.chromosome = chromosome;
        this.qual = qual;
        this.filter = filter;
        this.start = start;
        this.absoluteStart = absoluteStart;
        this.alternate = alternate;
        this.id = id;
        this.genotypes = genotypes;
        this.end = start + this.length;
        this.alleleFreq = null;
    }

    /**
     * Prints information for Call
     * @return String holding information for Call
     */
    public String toString() {
        return "TYPE " + this.type + " LENGTH " + this.length + "CHROMOSOME " + this.chromosome + " START " + this.start + "ABSOLUTESTART " + this.absoluteStart + " END " + this.end + " GENOTYPES " + genotypes.toString();
    }

    public void addViewNodes(Sample sample) {
        if (!nodes.containsKey(sample)) {
            nodes.put(sample, new ArrayList<>());
        }
        else {

            //nodes.get(sample).add( )
        }
    }

    /**
     * Get structure holding sample genotypes for Call
     * @return Map of Sample name and genotype
     */
    public HashMap<String,String> getGenotypes() {
        return this.genotypes;
    }

    /**
     * Get SV type for Call
     * @return String holding value of SVTYPE
     */
    public String getType() {
        return this.type;
    }

    /**
     * Get length for Call
     * @return int holding absolute value of SVLEN
     */
    public int getLength() {
        return this.length;
    }

    /**
     * Get chromosome name for Call
     * @return String holding chromosome name
     */
    public String getChromosome() {
        return this.chromosome;
    }

    /**
     * Get quality score for Call
     * @return String holding quality score
     */
    public String getQual() {
        return this.qual;
    }

    /**
     * Get filter information for Call
     * @return String holding filter information
     */
    public String getFilter() {
        return this.filter;
    }

    /**
     * Get absolute start for Call (position calculated considering all chromosomes previous)
     * @return long value that is the absolute start for call
     */
    public long getAbsoluteStart() {
        return this.absoluteStart;
    }

    /**
     * Get start for Call
     * @return int start position for Call
     */
    public int getStart() {
        return this.start;
    }

    /**
     * Get end position for Call
     * @return int end position for Call
     */
    public int getEnd() {
        return this.end;
    }

    /**
     * Get alternate for Call
     * @return String describing the alternate field/allele
     */
    public String getAlternate() {
        return this.alternate;
    }

    /**
     * Get ID for Call
     * @return String describing ID field for Call
     */
    public String getId() {
        return this.id;
    }

    /**
     * Calculates and sets the allele frequency for Call by looping through all genotypes
     */
    public void setAlleleFreq() {
        int refCount = 0;
        int altCount = 0;
        // loop through all genotypes
        for (String genotype : genotypes.values()) {
            for (char c : genotype.toCharArray()) {
                if (c == '0') {
                    refCount++;
                } else if (c == '1') {
                    altCount++;
                } else {
                    // do nothing
                }
            }
        }
        // calculate frequency and set attribute
        double totalCount = refCount + altCount;
        this.alleleFreq = altCount / totalCount;
    }

    /**
     * Get the allele frequency for call
     * @return Double value for the allele frequency for the Call
     */
    public Double getAlleleFreq() {
        return this.alleleFreq;
    }
}
