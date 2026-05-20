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
    private long refTotalLength;
    private ArrayList<Sample> samples = new ArrayList<>();
    private HashMap<String, Color> sampleColors = new HashMap<>();
    private ArrayList<Call> calls = new ArrayList<>();
    private ArrayList<Selection> selections = new ArrayList<>();
    private double zoomLevel = 0.2;
    private ArrayList<Integer> increments = new ArrayList<>(Arrays.asList(100, 200, 500, 1000, 2000, 5000, 10000, 20000, 50000, 100000, 200000, 500000, 1000000, 2000000, 5000000, 10000000, 20000000, 50000000));
    private int coordIncrementIndex = 3;
    private double trackHeightScale = 1;
    private final double baseFontSize = 12;
    private final int originalTrackHeight = 100;
    // AF is shown by default
    private int numAnnotationsShown = 1;
    private int tileSize = 10000000;
    private int currentTileStart = 0;
    private int currentTileEnd = 0;
    private int tileBuffer = 2;
    private final Set<String> supportedSVTypes = Set.of("TRA", "BND", "INS", "DEL", "INV", "DUP");


    public void reset() {
        this.samples.clear();
        this.sampleColors.clear();
        this.calls.clear();
        this.selections.clear();
        setCurrentChrom(null);
    }

    /**
     *
     * @param newStartTile corresponds to the start of the tile for the region in view
     * @return whether new tiles need to be shown
     */
    public boolean updateCurrentTileStart(int newStartTile) {
        // currently range includes less than 0, and new region includes less than 0, do nothing
        if (this.currentTileStart <= 0 && newStartTile-tileBuffer <= 0) {
            // do nothing
            return false;
        }
        // currently already the same, do nothing
        else if (this.currentTileStart == (newStartTile-tileBuffer)) {
            // do nothing
            return false;
        }
        // otherwise different, update
        else {
            if (newStartTile-tileBuffer < 1) {
                this.currentTileStart = 1;
            }
            else {
                this.currentTileStart = newStartTile-tileBuffer;
            }
        }
        return true;
    }

    public boolean updateCurrentTileEnd(int newEndTile) {
        if (this.currentTileEnd == (newEndTile+tileBuffer)) {
            // do nothing
            return false;
        }
        else {
            this.currentTileEnd = newEndTile+tileBuffer;
            return true;
        }
    }

    public int getCurrentTileStart() {
        return this.currentTileStart;
    }

    public int getCurrentTileEnd() {
        return this.currentTileEnd;
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

    /**
     * Finds the increment that is needed to display all samples vertically within the call panel scrollpane.
     * @param panelHeight height of the callsPanel scrollpane
     * @param SBHeight height of the callsPanel horizontal scrollbar
     * @return the increment that needs to be applied to the scale factor
     */
    public double fitAllSamplesIncrement(double panelHeight, double SBHeight) {
        double scale = ((panelHeight - SBHeight) / (samples.size()+1)) / this.originalTrackHeight;
        System.out.println("SCALE IS " + scale);
        return scale - 1;
    }

    public double getZoomLevel() {
        return this.zoomLevel;
    }

    public Pair<String, Double> updateZoomLevelByFactor(double factor, Chromosome chrom, double viewportWidth, double verticalSBWidth, double start, double oldProportion) {
        double testZoomLevel = this.zoomLevel * factor;
        // test what the content width would be
        double contentWidth = chrom.getLength() * testZoomLevel;
        double proportionVisible = viewportWidth / contentWidth;
        double selectedZoom;

        int newProportion = (int) this.getGenomicProportion(viewportWidth, chrom, testZoomLevel);
        int intStart = (int) start;
        int centerStart = intStart + (int) oldProportion/2;
        int end = (int) (start + oldProportion);

        // protruding on both ends
        if (proportionVisible > 1) {
            selectedZoom = (viewportWidth + verticalSBWidth) / chrom.getLength();
            this.zoomLevel = selectedZoom;
            return new Pair("ABSOLUTE CENTER", selectedZoom);
        }
        else {
            selectedZoom = testZoomLevel;
            this.zoomLevel = selectedZoom;
        }

        // right edge is greater than end
        if (centerStart + (int) (newProportion/2) >= chrom.getLength()) {
            return new Pair("RIGHT", selectedZoom);
        }
        else if (centerStart - (int) (newProportion/2) <= 1) {
            return new Pair("LEFT", selectedZoom);
        }
        else {
            return new Pair("CENTER", selectedZoom);
        }
    }

    public void setZoom(double level) {
        this.zoomLevel = level;
    }

    /**
     * Updates attribute zoomLevel to show one chromosome
     * @param length length of chromosome/region to show
     * @param viewportWidth double value of current scrollpane viewport width
     * @param isSBVisible boolean value for whether the scrollpane vertical scrollbar is currently visible
     * @param SBwidth double value of the width of the scrollpane vertical scrollbar
     */
    public void updateZoomLevelByRegion(long length, double viewportWidth, boolean isSBVisible, double SBwidth) {
        // if scrollbar is visible, update zoomLevel taking into account the viewport width needs to include the scrollbar width
        if (isSBVisible) {
            this.zoomLevel = (viewportWidth + SBwidth) / length;
        }
        // otherwise the entire viewport width is showing and calculate normally
        else {
            this.zoomLevel = viewportWidth / length;
        }
    }

    public int getNumAnnotationsShown() {
        return this.numAnnotationsShown;
    }

    public HashMap<String, Color> getSampleColors() {
        return this.sampleColors;
    }

    public LinkedHashMap<String,Chromosome> getRefChromosomes() {
        return this.refChromosomes;
    }

    public long getRefTotalLength() {
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

    public ArrayList<Call> getDiffCallsFromPinned(ArrayList<CheckBox> checkboxes) {
        ArrayList<Sample> checkedSamples = new ArrayList<>();
        ArrayList<Call> result = new ArrayList<>();
        for (int i=0; i<checkboxes.size(); i++) {
            if (checkboxes.get(i).isSelected()) {
                checkedSamples.add(this.samples.get(i));
            }
        }
        // if no selections are made or no samples are checked, return empty result
        if (this.selections.isEmpty() || checkedSamples.isEmpty()) {
            return result;
        }
        // otherwise, process
        else {
            for (Selection selection : selections) {
                double selectionStart = selection.getGenomicStart();
                double selectionEnd = selection.getGenomicEnd();
                int startInterval = getStartInterval((int) selectionStart);
                int endInterval = getEndInterval((int) selectionEnd);
                // loop through each tile
                for (int i = startInterval; i <= endInterval; i++) {
                    ArrayList<String> pinnedCallIds = new ArrayList<>();
                    // loop through calls for pinned samples
                    for (Sample checkedSample : checkedSamples) {
                        // if no tile interval for that sample then move to the next
                        if (checkedSample.getTiledCalls().get(getCurrentChrom().getName()).get(i) == null) {
                            continue;
                        }
                        // otherwise, loop through the calls
                        for (Call currentCall : checkedSample.getTiledCalls().get(getCurrentChrom().getName()).get(i)) {
                            // if in region
                            if (currentCall.getStart() > selectionStart && currentCall.getEnd() < selectionEnd) {
                                // if this call has already been seen with another pinned sample, do nothing
                                if (pinnedCallIds.contains(currentCall.getId())) {
                                    // do nothing
                                } else {
                                    pinnedCallIds.add(currentCall.getId());
                                }
                            }
                            // outside of region
                            else {
                                // do nothing
                            }
                        }
                    }
                    // loop through calls for non pinned samples
                    for (Sample sample : this.samples) {
                        // if in checked samples, don't process
                        if (checkedSamples.contains(sample)) {
                            // do nothing
                        }
                        // otherwise an unchecked sample, process calls
                        else {
                            // if no tile interval for that sample then move to the next
                            if (sample.getTiledCalls().get(getCurrentChrom().getName()).get(i) == null) {
                                continue;
                            }
                            // otherwise, loop through the calls
                            for (Call currentCall : sample.getTiledCalls().get(getCurrentChrom().getName()).get(i)) {
                                System.out.println("ID IS " + currentCall.getId() + " at location " + currentCall.getStart());
                                // if in region
                                if (currentCall.getStart() > selectionStart && currentCall.getEnd() < selectionEnd) {
                                    // if this call has already been seen with another pinned sample, do nothing
                                    if (pinnedCallIds.contains(currentCall.getId())) {
                                        System.out.println("IT IS IN PINNED IDS");
                                        // do nothing
                                    }
                                    // otherwise it is different so add it to results
                                    else {
                                        // add it only if results doesn't contain it
                                        if (result.contains(currentCall)) {
                                            System.out.println("RESULTS ALREADY CONTAIN THE ID");
                                            // do nothing
                                        }
                                        else {
                                            System.out.println("ADDED CALL TO RESULTS");
                                            result.add(currentCall);
                                        }
                                    }
                                }
                                // outside of region
                                else {
                                    // do nothing
                                }
                            }
                        }
                    }
                }
            }
            return result;
        }
    }


    public ArrayList<Call> getSameCallsFromPinned(ArrayList<CheckBox> checkboxes) {
        ArrayList<Sample> checkedSamples = new ArrayList<>();
        ArrayList<Call> result = new ArrayList<>();
        for (int i = 0; i < checkboxes.size(); i++) {
            if (checkboxes.get(i).isSelected()) {
                checkedSamples.add(this.samples.get(i));
            }
        }
        // if no selections are made or no samples are checked, return empty result
        if (this.selections.isEmpty() || checkedSamples.isEmpty()) {
            return result;
        }
        // otherwise, process
        else {
            for (Selection selection : selections) {
                double selectionStart = selection.getGenomicStart();
                double selectionEnd = selection.getGenomicEnd();
                int startInterval = getStartInterval((int) selectionStart);
                int endInterval = getEndInterval((int) selectionEnd);
                // loop through each tile
                for (int i = startInterval; i <= endInterval; i++) {
                    ArrayList<String> pinnedCallIds = new ArrayList<>();
                    // loop through calls for pinned samples
                    for (Sample checkedSample : checkedSamples) {
                        // if no tile interval for that sample then move to the next
                        if (checkedSample.getTiledCalls().get(getCurrentChrom().getName()).get(i) == null) {
                            continue;
                        }
                        // otherwise, loop through the calls
                        for (Call currentCall : checkedSample.getTiledCalls().get(getCurrentChrom().getName()).get(i)) {
                            // if in region
                            if (currentCall.getStart() > selectionStart && currentCall.getEnd() < selectionEnd) {
                                // if this call has already been seen with another pinned sample, do nothing
                                if (pinnedCallIds.contains(currentCall.getId())) {
                                    // do nothing
                                }
                                // otherwise add it to seen and add it to results if not added already
                                else {
                                    pinnedCallIds.add(currentCall.getId());
                                    if (result.contains(currentCall)) {
                                        // do nothing
                                    }
                                    else {
                                        result.add(currentCall);
                                    }
                                }
                            }
                            // outside of region
                            else {
                                // do nothing
                            }
                        }
                    }
                }
            }
            return result;
        }
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
                    if ((call.getStart() > selectionStart && call.getEnd() < selectionEnd ||
                            call.getStart() < selectionStart && call.getEnd() > selectionStart ||
                            call.getStart() < selectionEnd && call.getEnd() > selectionEnd) && Objects.equals(call.getChromosome(), selection.getChromosome())) {
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
                                        // if same presence/absence, add equivalence for sample
                                        // current contains 1 and comparative also contains 1
                                        if (curGT.contains("1")) {
                                            if (call.getGenotypes().get(sampleOrder.get(j).getName()).contains("1")) {
                                                equiv.get(curName).add(sampleOrder.get(j).getName());
                                            }
                                        }
                                        // current does not contain 1 and comparative also does not contain 1
                                        else {
                                            if (!call.getGenotypes().get(sampleOrder.get(j).getName()).contains("1")) {
                                                equiv.get(curName).add(sampleOrder.get(j).getName());
                                            }

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
                                        if (curGT.contains("1")) {
                                            // SAME do nothing
                                            if (call.getGenotypes().get(equiv.get(curName).get(j)).contains("1")) {
                                                // do nothing
                                            }
                                            // different, add to a list to remove the sample from equivalences
                                            else {
                                                removeNames.add(equiv.get(curName).get(j));
                                            }
                                        }
                                        // does not contain 1
                                        else {
                                            // also does not contain 1 (same), do nothing
                                            if (!call.getGenotypes().get(equiv.get(curName).get(j)).contains("1")) {
                                                // do nothing
                                            }
                                            else {
                                                removeNames.add(equiv.get(curName).get(j));
                                            }
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
            //this.trackHeightScale = Math.round(this.trackHeightScale * 10) / 10.0;
        }
    }

    public boolean checkIfValidRegion(String regionText) {
        String regex = "(.+):(\\d+)-(\\d+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(regionText);
        if (matcher.find()) {
            String chrom = matcher.group(1);
            int start = Integer.parseInt(matcher.group(2));
            int end = Integer.parseInt(matcher.group(3));
            // check if chromosome exists
            if (refChromosomes.containsKey(chrom)) {
                // check if positions are in range
                if (start > 0 && end <= refChromosomes.get(chrom).getLength()) {
                    return true;
                }
                else {
                    return false;
                }
            }
            else {
                return false;
            }
        }
        else {
            return false;
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
                    this.refChromosomes.put(matcher.group(1), new Chromosome(matcher.group(1), Integer.parseInt(matcher.group(2)), this.refTotalLength+1, this.tileSize));
                    this.refTotalLength += Integer.parseInt(matcher.group(2));
                }
            }
            // header line with sample info
            else if (line.startsWith("#")) {
                // add <ALL> to refChromosomes now that all header lines have been processed and total ref length is known
                this.refChromosomes.put("<ALL>", new Chromosome("<ALL>", this.refTotalLength, 1, this.tileSize));
                String[] header = line.split("\t");
                createSamples(Arrays.copyOfRange(header, 9, header.length), this.refChromosomes);
            }
            // call line
            else {
                int startCol = 9;
                String[] fields = line.split("\t");
                HashMap<String,String> genotypes = new HashMap<>();
                // first regex for type and length, use ? to make non greedy and match as little as possible (to the first semi colon)
                String typeInfoRegex = "SVTYPE=(.+?);";
                String lengthInfoRegex = "SVLEN=(.+?);";
                Pattern typeInfoPattern = Pattern.compile(typeInfoRegex);
                Pattern lengthInfoPattern = Pattern.compile(lengthInfoRegex);
                Matcher typeInfoMatcher = typeInfoPattern.matcher(fields[7]);
                Matcher lengthInfoMatcher = lengthInfoPattern.matcher(fields[7]);
                if (!typeInfoMatcher.find()) {
                    System.err.println("Error: Could not find type in expected VCF format for call. Ignoring call.");
                }
                else if (!lengthInfoMatcher.find()) {
                    System.err.println("Error: Could not find length in expected VCF format for call. Ignoring call.");
                }
                else {
                    // if not a supported type, continue
                    if (!supportedSVTypes.contains(typeInfoMatcher.group(1))) {
                        System.err.println("Error: SV type: " + typeInfoMatcher.group(1) + " is not supported. Ignoring call.");
                    }
                    // otherwise, do nothing
                    else {
                        // do nothing
                    }
                    // make sure chrom was processed earlier
                    long absoluteStart = 0;
                    try {
                        absoluteStart = refChromosomes.get(fields[0]).getAbsoluteStart() + Integer.parseInt(fields[1]);
                    }
                    catch (NullPointerException e) {
                        //System.err.println("Could not identify Chromosome " + fields[0] + ". Ignoring call.");
                        continue;
                    }
                    Call currentCall = new Call(typeInfoMatcher.group(1), Integer.parseInt(lengthInfoMatcher.group(1)), fields[0], fields[5], fields[6], Integer.parseInt(fields[1]), absoluteStart, fields[4], fields[2], genotypes);
                    for (Sample sample : this.samples) {
                        String genotypeRegex = "(./.):";
                        Pattern genotypePattern = Pattern.compile(genotypeRegex);
                        Matcher genotypeMatcher = genotypePattern.matcher(fields[startCol]);
                        // assign reference length if match is found, otherwise exit
                        if (genotypeMatcher.find()) {
                            genotypes.put(sample.getName(), genotypeMatcher.group(1));
                            // if has the variant, add
                            if (Objects.equals(genotypeMatcher.group(1), "0/1") || Objects.equals(genotypeMatcher.group(1), "1/1")) {
                                // add call for chromosome (in VCF)
                                sample.addCall(fields[0], currentCall);
                                sample.addToTiledCalls(fields[0], currentCall, this.tileSize);
                                // add call to <ALL>
                                sample.addCall("<ALL>", currentCall);
                            }
                        } else {
                            System.err.println("Error: Could not find genotype for a sample on line:" + line + " . Ignoring call.");
                        }
                        startCol++;
                    }
                    // after all calls added, calculate allele frequency
                    currentCall.setAlleleFreq();
                    // add to structures
                    calls.add(currentCall);
                    refChromosomes.get(fields[0]).addTiledCall(currentCall, this.tileSize);
                    refChromosomes.get("<ALL>").addCall(currentCall);
                    refChromosomes.get(fields[0]).addCall(currentCall);
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

    public int getStartInterval(int start) {
        return (start - 1) / this.tileSize + 1;
    }

    public int getEndInterval(int end) {
        return (end - 1) / this.tileSize + 1;
    }

    public void updateCoordIncrement(double viewportWidth, Chromosome chrom) {
        double genomicProportion = getGenomicProportion(viewportWidth, chrom, this.zoomLevel);
        if (genomicProportion < 100000000) {
            double ideal = genomicProportion / 7;
            System.out.println("IDEAL IS " + ideal);
            this.coordIncrementIndex = getClosestIntegerValue(ideal, increments);
            System.out.println("THIS COORD INCREMENT IS " + increments.get(coordIncrementIndex));
        }
        // last increment which corresponds to 100 MB
        else {
            this.coordIncrementIndex = this.increments.size() - 1;
        }
    }

    public double getGenomicProportion(double viewportWidth, Chromosome chrom, double zoomLevel) {
        // content width in pixels
        double contentWidth = chrom.getLength() * zoomLevel;
        // proportion of viewport width to entire content width
        double proportionVisible = viewportWidth / contentWidth;
        // get length of genomic proportion in view
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
