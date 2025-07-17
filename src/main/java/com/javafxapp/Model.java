package com.javafxapp;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

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

    public ArrayList<Sample> getSamples() {
        return this.samples;
    }

    public ArrayList<Selection> getSelections() {
        return this.selections;
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