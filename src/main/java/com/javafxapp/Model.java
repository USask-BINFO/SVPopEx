package com.javafxapp;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import javafx.scene.paint.Color;

public class Model {
    private String refName;
    private int refLength;
    private ArrayList<Sample> samples = new ArrayList<>();
    private ArrayList<Call> calls = new ArrayList<>();
    private ArrayList<Selection> selections = new ArrayList<>();
    private double zoomLevel = 0.2;
    private final double baseLevel = 0.2;

    public String loadFile(java.io.File file) throws java.io.IOException {
        return new String(java.nio.file.Files.readAllBytes(file.toPath()));
    }

    public double getZoomLevel() {
        return this.zoomLevel;
    }

    public double getBaseLevel() {
        return this.baseLevel;
    }

    public double updateZoomLevel(double factor) {
        this.zoomLevel *= factor;
        return this.zoomLevel;
    }

    public String getRefName() {
        return this.refName;
    }

    public void setRefName(String name) {
        this.refName = name;
    }

    public int getRefLength() {
        return this.refLength;
    }

    public void setRefLength(int length) {
        this.refLength = length;
    }

    public void addSelection(Selection selection) {
        this.selections.add(selection);
        for (int i=0; i<selections.size(); i++) {
            System.out.println(selections.get(i).toString());
        }
    }

    public LinkedHashMap<Selection, LinkedHashMap<String,Color>> processSelections() {
        LinkedHashMap<Selection, LinkedHashMap<String,Color>> result = new LinkedHashMap<>();
        if (this.selections.isEmpty()) {
            return result;
        }
        else {
            for (Selection selection : selections) {
                double selectionStart = selection.getGenomicStart();
                double selectionEnd = selection.getGenomicEnd();
                LinkedHashMap<String, ArrayList<String>> equiv = new LinkedHashMap<>();
                LinkedHashMap<String, Boolean> locked = new LinkedHashMap<>();
                LinkedHashMap<String, Color> colors = new LinkedHashMap<>();
                for (int i=0; i<samples.size(); i++) {
                    colors.put(samples.get(i).getName(), this.getRandomColor());
                    if (i == 0) {
                        equiv.put(samples.get(i).getName(), new ArrayList<String>());
                        equiv.get(samples.get(i).getName()).add(samples.get(i).getName());
                        locked.put(samples.get(i).getName(), Boolean.TRUE);
                    }
                    else {
                        equiv.put(samples.get(i).getName(), new ArrayList<String>());
                        locked.put(samples.get(i).getName(), Boolean.FALSE);
                    }
                }
                for (Call call : calls) {
                    if (call.getStart() > selectionStart && call.getEnd() < selectionEnd ||
                    call.getStart() < selectionStart && call.getEnd() < selectionEnd ||
                    call.getStart() > selectionStart && call.getEnd() > selectionEnd) {
                        for (int i=0; i<samples.size(); i++) {
                            String curName = samples.get(i).getName();
                            String curGT = call.getGenotypes().get(curName);
                            if (locked.get(curName) == Boolean.TRUE) {
                                // do nothing
                            }
                            else {
                                if (equiv.get(curName).isEmpty()) {
                                    for (int j=0; j<i; j++) {
                                        if (Objects.equals(call.getGenotypes().get(samples.get(j).getName()), curGT)) {
                                            equiv.get(curName).add(samples.get(j).getName());
                                        }
                                    }
                                    // if still no equivalences, add itself and lock
                                    if (equiv.get(curName).isEmpty()) {
                                        equiv.get(curName).add(curName);
                                        locked.put(curName, Boolean.TRUE);
                                    }
                                }
                                else {
                                    for (int j=0; j<equiv.get(curName).size(); j++) {
                                        if (Objects.equals(call.getGenotypes().get(samples.get(j).getName()), curGT)) {
                                            // do nothing
                                        }
                                        else {
                                            equiv.get(curName).remove(j);
                                            if (equiv.get(curName).isEmpty()) {
                                                equiv.get(curName).add(curName);
                                                locked.put(curName, Boolean.TRUE);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                result.put(selection, new LinkedHashMap<>());
                for (int i=0; i<samples.size(); i++) {
                    result.get(selection).put(samples.get(i).getName(), colors.get(equiv.get(samples.get(i).getName()).getFirst()));
                }
                for (Map.Entry<String, ArrayList<String>> entry : equiv.entrySet()) {
                    System.out.println(entry.getKey() + " = " + entry.getValue());
                }
            }
        }
        return result;
    }

    public static Color getRandomColor() {
        return Color.rgb(new Random().nextInt(256), new Random().nextInt(256), new Random().nextInt(256));
    }

    public ArrayList<Sample> getSamples() {
        return this.samples;
    }

    public ArrayList<Selection> getSelections() {
        return this.selections;
    }

    public void clearSelections() {
        this.selections.clear();
    }

    public void processFile(String fileContent) {
        String[] lines = fileContent.split("\\r?\\n");  // Splits on \n or \r\n
        for (String line : lines) {
            // comment line
            if (line.startsWith("##")) {
                String regex = "ID=(.+),length=(\\d+)>";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(line);
                // assign reference length if match is found, otherwise exit
                if (matcher.find()) {
                    // assign name
                    this.setRefName(matcher.group(1));
                    // assign length
                    this.setRefLength(Integer.parseInt(matcher.group(2)));
                }
            }
            // header line with sample info
            else if (line.startsWith("#")) {
                String[] header = line.split("\t");
                createSamples(Arrays.copyOfRange(header, 9, header.length));
            }
            // call line
            else {
                int startCol = 9;
                String[] fields = line.split("\t");
                LinkedHashMap<String,String> genotypes = new LinkedHashMap<>();
                // first regex for type and length
                String infoRegex = "SVTYPE=(.+);SVLEN=(.+);END";
                Pattern infoPattern = Pattern.compile(infoRegex);
                Matcher infoMatcher = infoPattern.matcher(fields[7]);
                if (!infoMatcher.find()) {
                    System.err.println("Error: Could not find type or length in expected VCF format for call. Ignoring call.");
                }
                else {
                    Call currentCall = new Call(infoMatcher.group(1), Integer.parseInt(infoMatcher.group(2)), Integer.parseInt(fields[1]), genotypes);
                    calls.add(currentCall);
                    for (Sample sample : this.samples) {
                        String genotypeRegex = "(./.):";
                        Pattern genotypePattern = Pattern.compile(genotypeRegex);
                        Matcher genotypeMatcher = genotypePattern.matcher(fields[startCol]);
                        // assign reference length if match is found, otherwise exit
                        if (genotypeMatcher.find()) {
                            genotypes.put(sample.getName(), genotypeMatcher.group(1));
                            if (Objects.equals(genotypeMatcher.group(1), "1/1") || Objects.equals(genotypeMatcher.group(1), "0/1")) {
                                sample.addCall(currentCall);
                            }
                        } else {
                            System.err.println("Error: Could not find genotype for a sample on line:" + line + " . Ignoring call.");
                        }
                        startCol++;
                    }
                }
            }
        }
    }

    public void createSamples(String[] sampleNames) {
        for (int i=0; i<sampleNames.length; i++) {
            Sample sample = new Sample(sampleNames[i]);
            this.samples.add(sample);
        }
    }
}