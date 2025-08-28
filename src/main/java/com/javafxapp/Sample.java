package com.javafxapp;
import javafx.scene.layout.Pane;

import java.util.ArrayList;

public class Sample {
    private String name;
    public ArrayList<Call> calls = new ArrayList<>();
    public Sample(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void addCall(Call call) {
        this.calls.add(call);
    }

}
