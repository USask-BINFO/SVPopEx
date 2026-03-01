package com.javafxapp;

import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Call {
    private String type;
    private int length;
    private String chromosome;
    private int start;
    private long absoluteStart;
    private int end;
    private String alternate;
    private String id;
    private Double alleleFreq;
    HashMap<String,String> genotypes = new HashMap<>();
    public Call(String type, int length, String chromosome, int start, long absoluteStart, String alternate, String id, HashMap<String,String> genotypes) {
        this.type = type;
        if (Objects.equals(type, "DEL")) {
            this.length = Math.abs(length);
        }
        else {
            this.length = length;
        }
        this.chromosome = chromosome;
        this.start = start;
        this.absoluteStart = absoluteStart;
        this.alternate = alternate;
        this.id = id;
        this.genotypes = genotypes;
        this.end = start + this.length;
        this.alleleFreq = null;
    }
    public String toString() {
        return "TYPE " + this.type + " LENGTH " + this.length + "CHROMOSOME " + this.chromosome + " START " + this.start + "ABSOLUTESTART " + this.absoluteStart + " END " + this.end + " GENOTYPES " + genotypes.toString();
    }

    public HashMap<String,String> getGenotypes() {
        return this.genotypes;
    }

    public String getType() {
        return this.type;
    }

    public int getLength() {
        return this.length;
    }

    public String getChromosome() {
        return this.chromosome;
    }

    public long getAbsoluteStart() {
        return this.absoluteStart;
    }

    public int getStart() {
        return this.start;
    }

    public int getEnd() {
        return this.end;
    }

    public String getAlternate() {
        return this.alternate;
    }

    public String getId() {
        return this.id;
    }

    public void setAlleleFreq() {
        int refCount = 0;
        int altCount = 0;
        for (String genotype : genotypes.values()) {
            String alleleRegex = "(.+)/(.+)";
            Pattern allelePattern = Pattern.compile(alleleRegex);
            Matcher alleleMatcher = allelePattern.matcher(genotype);
            if (alleleMatcher.find()) {
                // first allele
                if (Objects.equals(alleleMatcher.group(1), ".")) {
                    // do nothing
                }
                else if (Integer.parseInt(alleleMatcher.group(1)) == 0) {
                    refCount++;
                }
                else if (Integer.parseInt(alleleMatcher.group(1)) == 1) {
                    altCount++;
                }
                else {
                    // unknown allele, do nothing
                }
                // second allele
                if (Objects.equals(alleleMatcher.group(2), ".")) {
                    // do nothing
                }
                else if (Integer.parseInt(alleleMatcher.group(2)) == 0) {
                    refCount++;
                }
                else if (Integer.parseInt(alleleMatcher.group(2)) == 1) {
                    altCount++;
                }
                else {
                    // unknown allele, do nothing
                }
            }
        }
        double totalCount = refCount + altCount;
        this.alleleFreq = altCount / totalCount;
    }

    public Double getAlleleFreq() {
        return this.alleleFreq;
    }
}

