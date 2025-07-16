package com.javafxapp;

public class Selection {
    private double startX;
    private double endX;
    public Selection(double startX, double endX) {
        this.startX = startX;
        this.endX = endX;
    }
    public String toString() {
        return "Start: " + startX + " , End: " + endX;
    }
}
