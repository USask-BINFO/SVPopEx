package com.javafxapp;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollBar;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Model {
    private Chromosome currentChrom = null;
    private LinkedHashMap<String,Chromosome> refChromosomes = new LinkedHashMap<>();
    private int refTotalLength;
    private ArrayList<Sample> samples = new ArrayList<>();
    HashMap<String, Color> sampleColors = new HashMap<>();
    private ArrayList<Call> calls = new ArrayList<>();
    private ArrayList<Selection> selections = new ArrayList<>();
    private double zoomLevel = 0.2;
    private ArrayList<Integer> increments = new ArrayList<>(Arrays.asList(100, 200, 500, 1000, 2000, 5000, 10000, 20000, 50000, 100000, 200000, 500000, 1000000, 2000000, 5000000, 10000000, 20000000, 50000000));
    private int coordIncrementIndex = 3;
    private double trackHeightScale = 1;
    private final double baseFontSize = 12;
    private final int originalTrackHeight = 100;
    private Double baseCallPanelHeight;
    // AF is shown by default
    private int numAnnotationsShown = 1;


    public void reset() {
        this.samples.clear();
        this.sampleColors.clear();
        this.calls.clear();
        this.selections.clear();
        setCurrentChrom(null);
    }

    public Chromosome getCurrentChrom() {
        return this.currentChrom;
    }

    public void setCurrentChrom(Chromosome chrom) {
        this.currentChrom = chrom;
    }

    public String loadFile(java.io.File file) throws java.io.IOException {
        return new String(java.nio.file.Files.readAllBytes(file.toPath()));
    }

    public double getZoomLevel() {
        System.out.println("CURRENT ZOOM LEVEL IS " + this.zoomLevel);
        return this.zoomLevel;
    }

    public double updateZoomLevelByFactor(double factor, Chromosome chrom, double viewportWidth, double verticalSBWidth) {
        double testZoomLevel = this.zoomLevel * factor;
        // test what the content width would be
        double contentWidth = chrom.getLength() * testZoomLevel;
        double proportionVisible = viewportWidth / contentWidth;
        double selectedZoom;
        if (proportionVisible > 1) {
            selectedZoom = (viewportWidth + verticalSBWidth) / chrom.getLength();
        }
        else {
            selectedZoom = testZoomLevel;
        }
        this.zoomLevel = selectedZoom;
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

    public void setZoom(double level) {
        this.zoomLevel = level;
    }

    /**
     * Updates attribute zoomLevel to show one chromosome
     * @param chrom Chromosome to show
     * @param viewportWidth double value of current scrollpane viewport width
     * @param isSBVisible boolean value for whether the scrollpane vertical scrollbar is currently visible
     * @param SBwidth double value of the width of the scrollpane vertical scrollbar
     */
    public void updateZoomLevelByChrom(Chromosome chrom, double viewportWidth, boolean isSBVisible, double SBwidth) {
        // if scrollbar is visible, update zoomLevel taking into account the viewport width needs to include the scrollbar width
        if (isSBVisible) {
            this.zoomLevel = (viewportWidth + SBwidth) / chrom.getLength();
        }
        // otherwise the entire viewport width is showing and calculate normally
        else {
            this.zoomLevel = viewportWidth / chrom.getLength();
        }
    }

    public int getNumAnnotationsShown() {
        return this.numAnnotationsShown;
    }

    public void setBaseCallPanelHeight(double height) {
        this.baseCallPanelHeight = height;
    }

    public HashMap<String, Color> getSampleColors() {
        return this.sampleColors;
    }

    public double getBaseCallPanelHeight() {
        return this.baseCallPanelHeight;
    }

    public LinkedHashMap<String,Chromosome> getRefChromosomes() {
        return this.refChromosomes;
    }

    public int getRefTotalLength() {
        return this.refTotalLength;
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

    public HashMap<Rectangle,Color> processPinnedSelections(ArrayList<Sample> sampleOrder, ArrayList<CheckBox> checkboxes) {
        ArrayList<Sample> checkedSamples = new ArrayList<>();
        HashMap<Rectangle, Color> result = new HashMap<>();
        for (int i=0; i<checkboxes.size(); i++) {
            if (checkboxes.get(i).isSelected()) {
                checkedSamples.add(this.samples.get(i));
            }
        }
        // if no selections are made or no samples are checked, return empty hashmap
        if (this.selections.isEmpty() || checkedSamples.isEmpty()) {
            return result;
        }
        // if selections are made, process
        else {
            for (int i=0; i<sampleOrder.size(); i++) {
                System.out.println(sampleOrder.get(i).getName());
            }
            ArrayList<String> seenCallIds = new ArrayList<>();
            for (Selection selection : selections) {
                double selectionStart = selection.getGenomicStart();
                double selectionEnd = selection.getGenomicEnd();
                // loop through samples
                for (Sample checkedSample : checkedSamples) {
                    System.out.println("___________" + checkedSample.getName());
                    // loop through sample calls
                    for (Call call : checkedSample.getCalls()) {
                        double calcStart = call.getStart() * zoomLevel;
                        double calcLength = call.getLength() * zoomLevel;
                        // if entire call is in selection area, then loop through genotypes
                        if (call.getStart() > selectionStart && call.getEnd() < selectionEnd) {
                            // if this call has not been processed
                            if (!seenCallIds.contains(call.getId())) {
                                // add call id to seen
                                seenCallIds.add(call.getId());
                                Boolean pastCurrent = false;
                                // loop through samples and check the genotype if applicable
                                for (int i=0; i<sampleOrder.size(); i++) {
                                    // dealing with a sample past the current checked sample
                                    if (pastCurrent) {
                                        if (Objects.equals(call.genotypes.get(sampleOrder.get(i).getName()), "1/1") || Objects.equals(call.genotypes.get(sampleOrder.get(i).getName()), "0/1")) {
                                            Rectangle newRect = new Rectangle(calcStart, i*100, calcLength, 100);
                                            result.put(newRect, sampleColors.get(checkedSample.getName()));
                                        }
                                    }
                                    // if current sample is the current checked sample
                                    else if (sampleOrder.get(i) == checkedSample) {
                                        // change atCurrent to true
                                        pastCurrent = true;
                                        // create rectangle for itself
                                        Rectangle newRect = new Rectangle(calcStart, i*100, calcLength, 100);
                                        result.put(newRect, sampleColors.get(checkedSample.getName()));
                                    }
                                    // sample is before the current checked sample, do nothing because these are already processed
                                    else {
                                        // do nothing
                                    }

                                }

                            }
                            // call has already been processed, do nothing
                            else {
                                // do nothing
                            }
                        }
                        // call outside of selection area
                        else {
                            // do nothing
                        }
                    }

                }
            }
        }
        return result;
    }

    public HashMap<Rectangle,Color> processHaplotypeSelections(ArrayList<Sample> sampleOrder) {
        /*
        Preconditions: Assumes that sample order may have been manipulated by pinning
        Postconditions: Does NOT do any reordering
         */
        HashMap<Rectangle,Color> result = new HashMap<>();
        // if no selections are made, return empty hashmap
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
                int index = 0;
                for (Sample sample : sampleOrder) {
                    // top sample - equivalence is itself and it is locked
                    if (index == 0) {
                        equiv.put(sample.getName(), new ArrayList<>());
                        equiv.get(sample.getName()).add(sample.getName());
                        locked.put(sample.getName(), Boolean.TRUE);
                    }
                    // other samples - equivalence is null (ArrayList is empty) and it is not locked
                    else {
                        equiv.put(sample.getName(), new ArrayList<>());
                        locked.put(sample.getName(), Boolean.FALSE);
                    }
                    index++;
                }
                // loop through each SV call
                for (Call call : calls) {
                    // include the call if it is within the selection region (doesn't have to be completely within)
                    if (call.getStart() > selectionStart && call.getEnd() < selectionEnd ||
                            call.getStart() < selectionStart && call.getEnd() > selectionStart ||
                            call.getStart() < selectionEnd && call.getEnd() > selectionEnd) {
                        System.out.println(call);
                        // loop through each sample in view order
                        for (int i=0; i<sampleOrder.size(); i++) {
                            String curName = sampleOrder.get(i).getName();
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
                                        if (Objects.equals(call.getGenotypes().get(sampleOrder.get(j).getName()), curGT)) {
                                            equiv.get(curName).add(sampleOrder.get(j).getName());
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
                double calcStart = selection.getStart() * zoomLevel / selection.getZoomLevel();
                double calcLength = selection.getLength() * zoomLevel / selection.getZoomLevel();
                int curIndex = numAnnotationsShown;
                for (Sample sample : sampleOrder) {
                    Rectangle newRect = new Rectangle(calcStart, curIndex*100, calcLength, 100);
                    Color color = this.sampleColors.get(equiv.get(sample.getName()).getFirst());
                    result.put(newRect, color);
                    curIndex++;
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
        int startCoordinate = 1;
        for (String line : lines) {
            // comment line
            if (line.startsWith("##")) {
                String regex = "contig=<ID=(.+),length=(\\d+)>";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(line);
                // assign reference length if match is found, otherwise exit
                if (matcher.find()) {
                    // assign name
                    this.refChromosomes.put(matcher.group(1), new Chromosome(matcher.group(1), Integer.parseInt(matcher.group(2)), this.refTotalLength+1));
                    this.refTotalLength += Integer.parseInt(matcher.group(2));
                }
            }
            // header line with sample info
            else if (line.startsWith("#")) {
                // add <ALL> to refChromosomes now that all header lines have been processed and total ref length is known
                this.refChromosomes.put("<ALL>", new Chromosome("<ALL>", this.refTotalLength, 1));
                String[] header = line.split("\t");
                createSamples(Arrays.copyOfRange(header, 9, header.length), this.refChromosomes);
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
                    int absoluteStart = refChromosomes.get(fields[0]).getAbsoluteStart() + Integer.parseInt(fields[1]);
                    Call currentCall = new Call(infoMatcher.group(1), Integer.parseInt(infoMatcher.group(2)), fields[0], Integer.parseInt(fields[1]), absoluteStart, fields[2], genotypes);
                    calls.add(currentCall);
                    System.out.println(currentCall);
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
                                    // add call for chromosome (in VCF)
                                    sample.addCall(fields[0], currentCall);
                                    // add call to <ALL>
                                    sample.addCall("<ALL>", currentCall);
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

    public void createSamples(String[] sampleNames, LinkedHashMap<String, Chromosome> refContigs) {
        for (int i=0; i<sampleNames.length; i++) {
            Sample sample = new Sample(sampleNames[i], refContigs);
            this.samples.add(sample);
            this.sampleColors.put(samples.get(i).getName(), this.getRandomColor());
        }
    }

    public void updateCoordIncrement(double viewportWidth, Chromosome chrom) {
        double genomicProportion = getGenomicProportion(viewportWidth, chrom);
        if (genomicProportion < 100000000) {
            double ideal = genomicProportion / 7;
            this.coordIncrementIndex = getClosestIntegerValue(ideal, increments);
        }
        // last increment which corresponds to 100 MB
        else {
            this.coordIncrementIndex = this.increments.size() - 1;
        }
    }

    public double getGenomicProportion(double viewportWidth, Chromosome chrom) {
        double contentWidth = chrom.getLength() * zoomLevel;
        double proportionVisible = viewportWidth / contentWidth;
        return proportionVisible * chrom.getLength();
    }

    public int getClosestIntegerValue(double idealSpacing, ArrayList<Integer> definedIncrements) {
        int closestIndex = 0;
        double minDiff = Math.abs(definedIncrements.getFirst() - idealSpacing);

        for (int i = 1; i < definedIncrements.size(); i++) {
            double diff = Math.abs(definedIncrements.get(i) - idealSpacing);
            if (diff < minDiff) {
                minDiff = diff;
                closestIndex = i;
            }
        }

        return closestIndex;
    }

    public int getTickSpacing() {
        return increments.get(coordIncrementIndex);
    }
}
