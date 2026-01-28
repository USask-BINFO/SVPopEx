package com.javafxapp;

import java.util.ArrayList;
import java.util.HashMap;

public class Chromosome {
    private String name;
    private int length;
    private int absoluteStart;
    private double pixelWidth;
    private double pixelAbsoluteOffset;
    private HashMap<Integer, ArrayList<Call>> tiledCallStarts;
    private ArrayList<Call> allCalls;

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
        this.tiledCallStarts = new HashMap<>();
        this.allCalls = new ArrayList<>();
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

    public void addTiledCall(Call call, int tileSize) {
        int start = call.getStart();
        int startInterval = (start - 1) / tileSize + 1;
        if (tiledCallStarts.containsKey(startInterval)) {
            // do nothing
        }
        else {
            tiledCallStarts.put(startInterval, new ArrayList<>());
        }
        tiledCallStarts.get(startInterval).add(call);
    }

    public HashMap<Integer, ArrayList<Call>> getTiledCallStarts() {
        return this.tiledCallStarts;
    }

    public void addCall(Call call) {
        this.allCalls.add(call);
    }

    public ArrayList<Call> getAllCalls() {
        return this.allCalls;
    }
}
