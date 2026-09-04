package com.javafxapp;

// referring specifically to a GFF3 feature
public class Feature implements Component {
    private Chromosome chrom;
    private String type;
    private int start;
    private int end;
    private String id;
    private String name;
    private String description;
    private long absoluteStart;
    private int length;
    public Feature(Chromosome chrom, String type, int start, int end, String id, String name, String description, long absoluteStart) {
        this.chrom = chrom;
        this.type = type;
        this.start = start;
        this.end = end;
        this.id = id;
        this.name = name;
        this.description = description;
        this.absoluteStart = absoluteStart;
        this.length = end-start;
    }

    public int getStart() {
        return this.start;
    }

    public Chromosome getChromosome() {
        return this.chrom;
    }

    public long getAbsoluteStart() {
        return this.absoluteStart;
    }

    public int getLength() {
        return this.length;
    }

    public String getName() {
        return this.name;
    }

    public String getId() {
        return this.id;
    }

    public String getType() {
        return this.type;
    }

    public String getDescription() {
        return this.description;
    }

    public int getEnd() {
        return this.end;
    }


}
