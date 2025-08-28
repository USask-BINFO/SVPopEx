package com.javafxapp;

import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Call {
    private String type;
    private int length;
    private int start;
    private int end;
    private String id;
    HashMap<String,String> genotypes = new HashMap<>();
    public Call(String type, int length, int start, String id, HashMap<String,String> genotypes) {
        this.type = type;
        if (Objects.equals(type, "DEL")) {
            this.length = Math.abs(length);
        }
        else {
            this.length = length;
        }
        this.start = start;
        this.id = id;
        this.genotypes = genotypes;
        this.end = start + this.length;
    }
    public String toString() {
        return "TYPE " + this.type + " LENGTH " + this.length + " START " + this.start + " END " + this.end + " GENOTYPES " + genotypes.toString();
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

    public int getStart() {
        return this.start;
    }

    public int getEnd() {
        return this.end;
    }

    public String getId() {
        return this.id;
    }
}

