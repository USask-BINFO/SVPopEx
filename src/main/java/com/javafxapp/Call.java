package com.javafxapp;

import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Call {
    private String type;
    private int length;
    private int start;
    private int end;
    private String callRectId;
    LinkedHashMap<String,String> genotypes = new LinkedHashMap<>();
    public Call(String type, int length, int start, LinkedHashMap<String,String> genotypes) {
        this.type = type;
        if (Objects.equals(type, "DEL")) {
            this.length = Math.abs(length);
        }
        else {
            this.length = length;
        }
        this.start = start;
        this.genotypes = genotypes;
        this.end = start + this.length;
    }
    public String toString() {
        return "TYPE " + this.type + " LENGTH " + this.length + " START " + this.start + " END " + this.end + " GENOTYPES " + genotypes.toString();
    }

    public LinkedHashMap<String,String> getGenotypes() {
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

    public void setCallRectId(String id) {
        this.callRectId = id;
    }

    public String getCallRectId() {
        return this.callRectId;
    }
}

