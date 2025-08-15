package com.javafxapp;

import java.sql.Array;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import javafx.scene.paint.Color;

public class Model {
    private String refName;
    private int refLength;
    private ArrayList<Sample> samples = new ArrayList<>();
    HashMap<String, Color> sampleColors = new HashMap<>();
    private ArrayList<Call> calls = new ArrayList<>();
    private ArrayList<Selection> selections = new ArrayList<>();
    private double zoomLevel = 0.2;
    private final double baseLevel = 0.2;
    private ArrayList<Integer> increments = new ArrayList<>(Arrays.asList(100, 200, 500, 1000, 2000, 5000, 10000, 20000, 50000, 100000, 200000, 500000, 1000000, 2000000, 5000000, 10000000, 20000000, 50000000, 100000000));
    private int coordIncrementIndex = 3;
    private double trackHeightScale = 1;
    private final double baseFontSize = 12;
    private final int originalTrackHeight = 100;
    private Double baseCallPanelHeight;
    private HashMap<String, Boolean> comparators;
    private ArrayList<Sample> sampleOrderInView = new ArrayList<>();


    public void reset() {
        this.samples.clear();
        this.sampleColors.clear();
        this.calls.clear();
        this.selections.clear();
        // set zoom back to original
        this.zoomLevel = this.baseLevel;
        // clear comparators and sample order
        this.comparators = null;
        this.sampleOrderInView.clear();
    }

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

    public boolean isCallPanelHeightStored() {
        if (this.baseCallPanelHeight == null) {
            return false;
        }
        else {
            return true;
        }
    }

    public void setBaseCallPanelHeight(double height) {
        this.baseCallPanelHeight = height;
    }

    public HashMap<String, Color> getSampleColors() {
        return this.sampleColors;
    }

    public HashMap<String, Boolean> getComparators() {
        return this.comparators;
    }

    public double getBaseCallPanelHeight() {
        return this.baseCallPanelHeight;
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

    public double getBaseFontSize() {
        return this.baseFontSize;
    }

    public int getOriginalTrackHeight() {
        return this.originalTrackHeight;
    }

    public void addSelection(Selection selection) {
        this.selections.add(selection);
        for (int i=0; i<selections.size(); i++) {
            System.out.println(selections.get(i).toString());
        }
    }

    public HashMap<Block, Color> processBlockSelections() {
        HashMap<Block, Color> result = new HashMap<>();
        // if no selections are made, return empty linkedhashmap
        if (this.selections.isEmpty() || this.comparators == null) {
            return result;
        }
//        // if selections are made, process
//        else {
//            int index = 0;
//            for (Boolean comparator : comparators) {
//                if (comparator == true) {
//                }
//                else {
//                    // do nothing
//                }
//            }
//            Selection selection = selections.getLast();

        //}
        return result;
    }

    public HashMap<Selection, HashMap<String,Color>> processSelections() {
        /*

        Preconditions: Assumes that sample order has been manipulated before calling, if at all.
        Postconditions: Does NOT do any reordering
         */
        HashMap<Selection, HashMap<String,Color>> result = new HashMap<>();
        // if no selections are made, return empty linkedhashmap
        if (this.selections.isEmpty()) {
            return result;
        }
        // if selections are made, process
        else {
            for (Selection selection : selections) {
                // get selection start and end (genomic coords), make equivalence and locked structures
                // equivalence holds the 'upper' equivalent sample currently
                // locked tells if the signature is already unique and can be determined, true for determined
                double selectionStart = selection.getGenomicStart();
                double selectionEnd = selection.getGenomicEnd();
                HashMap<String, ArrayList<String>> equiv = new HashMap<>();
                HashMap<String, Boolean> locked = new HashMap<>();
                // for each sample, set equivalence and locked
                for (int i=0; i<sampleOrderInView.size(); i++) {
                    // top sample - equivalence is itself and it is locked
                    if (i == 0) {
                        equiv.put(sampleOrderInView.get(i).getName(), new ArrayList<String>());
                        equiv.get(sampleOrderInView.get(i).getName()).add(sampleOrderInView.get(i).getName());
                        locked.put(sampleOrderInView.get(i).getName(), Boolean.TRUE);
                    }
                    // other samples - equivalence is null (ArrayList is empty) and it is not locked
                    else {
                        equiv.put(sampleOrderInView.get(i).getName(), new ArrayList<String>());
                        locked.put(sampleOrderInView.get(i).getName(), Boolean.FALSE);
                    }
                }
                // loop through each SV call
                for (Call call : calls) {
                    // include the call if it is within the selection region (doesn't have to be completely within)
                    if (call.getStart() > selectionStart && call.getEnd() < selectionEnd ||
                    call.getStart() < selectionStart && call.getEnd() > selectionStart ||
                    call.getStart() < selectionEnd && call.getEnd() > selectionEnd) {
                        System.out.println(call.toString());
                        // loop through each sample
                        for (int i=0; i<sampleOrderInView.size(); i++) {
                            String curName = sampleOrderInView.get(i).getName();
                            String curGT = call.getGenotypes().get(curName);
                            // if the sample is locked do nothing
                            if (locked.get(curName) == Boolean.TRUE) {
                                // do nothing
                            }
                            // otherwise check the appropriate samples
                            else {
                                // if it has no equivalence yet, loop through all samples above and check
                                if (equiv.get(curName).isEmpty()) {
                                    for (int j=0; j<i; j++) {
                                        // if same genotype, add equivalence for sample
                                        if (Objects.equals(call.getGenotypes().get(sampleOrderInView.get(j).getName()), curGT)) {
                                            equiv.get(curName).add(sampleOrderInView.get(j).getName());
                                        }
                                    }
                                    // if no equivalence found, add itself and lock
                                    if (equiv.get(curName).isEmpty()) {
                                        equiv.get(curName).add(curName);
                                        locked.put(curName, Boolean.TRUE);
                                    }
                                }
                                // if it has equivalences, loop through all equivalent samples and make sure still equivalent
                                else {
                                    ArrayList<String> removeNames = new ArrayList<String>();
                                    // loop through equivalent samples
                                    for (int j=0; j<equiv.get(curName).size(); j++) {
                                        // if same genotype do nothing
                                        if (Objects.equals(call.getGenotypes().get(equiv.get(curName).get(j)), curGT)) {
                                            // do nothing
                                        }
                                        // otherwise, add to a list to remove the sample from equivalences
                                        else {
                                            removeNames.add(equiv.get(curName).get(j));
                                        }
                                    }
                                    // remove no longer equivalent samples
                                    Iterator<String> iterator = equiv.get(curName).iterator();
                                    while (iterator.hasNext()) {
                                        String s = iterator.next();
                                        if (removeNames.contains(s)) {
                                            iterator.remove();  // Safe removal during iteration
                                        }
                                    }
                                    // if its empty, no equivalence with any above sample, set equivalence to itself and lock
                                    if (equiv.get(curName).isEmpty()) {
                                        equiv.get(curName).add(curName);
                                        locked.put(curName, Boolean.TRUE);
                                    }
                                }
                            }
                        }
                    }
                }
                result.put(selection, new HashMap<>());
                for (int i=0; i<sampleOrderInView.size(); i++) {
                    result.get(selection).put(sampleOrderInView.get(i).getName(), this.sampleColors.get(equiv.get(sampleOrderInView.get(i).getName()).getFirst()));
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

    public void updateTrackHeightScale(double increment) {
        if ((this.trackHeightScale + increment) < 0.2) {
            // do nothing, too small
        }
        else {
            this.trackHeightScale += increment;
            // round to 1 decimal place
            this.trackHeightScale = Math.round(this.trackHeightScale * 10) / 10.0;
        }
    }

    public double getTrackHeightScale() {
        return this.trackHeightScale;
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
                HashMap<String,String> genotypes = new HashMap<>();
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
                            // if missing, add as reference
                            if (Objects.equals(genotypeMatcher.group(1), "./.")) {
                                genotypes.put(sample.getName(), "0/0");
                            }
                            // otherwise add as itself
                            else {
                                genotypes.put(sample.getName(), genotypeMatcher.group(1));
                                // if has the variant, add
                                if (Objects.equals(genotypeMatcher.group(1), "0/1") || Objects.equals(genotypeMatcher.group(1), "1/1")) {
                                    sample.addCall(currentCall);
                                }
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
            this.sampleOrderInView.add(sample);
            this.sampleColors.put(samples.get(i).getName(), this.getRandomColor());
        }
    }

    public ArrayList<Sample> getSampleOrderInView() {
        return this.sampleOrderInView;
    }

    public void processConfig(HashMap<String, Boolean> comparators) {
        this.comparators = comparators;
    }

    public void updateCoordIncrement(double viewportWidth) {
        int tickSpacing = increments.get(coordIncrementIndex);
        //double rawStep = viewportWidth/
        int lowerThreshold = 150;
        int upperThreshold = 350;
        // distance from first to second tick because first tick will be at 0
        double tickDist = tickSpacing*zoomLevel;
        // increase increment
        if (tickDist < lowerThreshold) {
            if (coordIncrementIndex+1 == increments.size()) {
                // do nothing, already at largest
            }
            else {
                coordIncrementIndex++;
            }
        }
        // lower increment
        else if (tickDist > upperThreshold) {
            if (coordIncrementIndex == 0) {
                // do nothing, already at lowest
            }
            else {
                coordIncrementIndex--;
            }
        }
        else {
            // do nothing, tick increments stay the same
        }
    }

    public int getTickSpacing() {
        return increments.get(coordIncrementIndex);
    }
}