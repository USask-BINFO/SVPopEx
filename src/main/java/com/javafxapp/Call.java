package com.javafxapp;

import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Call {
    private String type;
    private int length;
    private int start;
    LinkedHashMap<String,String> genotypes = new LinkedHashMap<>();
    public Call(String type, int length, int start, LinkedHashMap<String,String> genotypes) {
        this.type = type;
        this.length = length;
        this.start = start;
        this.genotypes = genotypes;
    }
    public String toString() {
        return "TYPE " + this.type + " LENGTH " + this.length + " START " + this.start + " GENOTYPES " + genotypes.toString();
    }

    public String getType() {
        return this.type;
    }

    public int getLength() {
        return this.length;
    }

    public int getStart() {
        return this.start;
    }
}

