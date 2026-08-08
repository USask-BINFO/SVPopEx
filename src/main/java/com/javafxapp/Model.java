package com.javafxapp;

import java.sql.Array;
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
    private HashMap<Chromosome, HashMap<Integer, Tile>> allTiles = new HashMap<>();
    private long refTotalLength = 0;
    private ArrayList<Sample> samples = new ArrayList<>();
    private HashMap<String, Color> sampleColors = new HashMap<>();
    private ArrayList<Selection> selections = new ArrayList<>();
    private double zoomLevel = 0.2;
    private ArrayList<Integer> increments = new ArrayList<>(Arrays.asList(100, 200, 500, 1000, 2000, 5000, 10000, 20000, 50000, 100000, 200000, 500000, 1000000, 2000000, 5000000, 10000000, 20000000, 50000000));
    private int coordIncrementIndex = 3;
    private double trackHeightScale = 1;
    private final double baseFontSize = 12;
    private final int originalTrackHeight = 100;
    private int tileSize = 20000000;
    private Integer currentTileBufferStart = null;
    private Integer currentTileBufferEnd = null;
    private int tileBuffer = 2;
    private int AFTrackHeight = 88;
    private final Set<String> supportedSVTypes = Set.of("TRA", "BND", "INS", "DEL", "INV", "DUP");

    /**
     * Clears the relevant data structures in the Model such that a new VCF file can be chosen and the structures can be repopulated.
     */
    public void reset() {
        this.samples.clear();
        this.sampleColors.clear();
        this.selections.clear();
        setCurrentChrom(null);
    }

    /**
     * Gets the tile data structure
     * @return
     */
    public HashMap<Chromosome,HashMap<Integer,Tile>> getTiles() {
        return this.allTiles;
    }

    /**
     * Gets the current padding start tile
     * @return int value for the start tile
     */
    public int getBufferStartTile() {
        return this.currentTileBufferStart;
    }

    /**
     * Gets the current padding end tile
     * @return int value for the end tile
     */
    public int getBufferEndTile() {
        return this.currentTileBufferEnd;
    }

    /**
     * Checks whether the padding or view tiles need to be updated
     * @param newStartTile corresponds to the start of the tile for the region in view INCLUDES BUFFER
     * @return boolean whether new tiles need to be shown, true if yes, and false otherwise
     */
    public boolean updateCurrentTileStart(int newStartTile) {
        // if the variable hasn't been set yet, set it to new tile start
        if (this.currentTileBufferStart == null) {
            this.currentTileBufferStart = newStartTile;
            return true;
        }
        // both at min, do nothing
        if (this.currentTileBufferStart == 1 && newStartTile == 1) {
            // do nothing
            System.out.println("EXECUTED CASE 1");
            return false;
        }
        // currently already the same, do nothing
        else if (this.currentTileBufferStart == (newStartTile)-tileBuffer) {
            // do nothing
            System.out.println("EXECUTE CASE 2");
            return false;
        }
        // otherwise different, update
        else {
            // if either are less than 1, set to 1
            if (newStartTile < 1 || newStartTile-tileBuffer < 1) {
                System.out.println("CASE 3 - UPDATING BUFFER TO 1st TILE");
                this.currentTileBufferStart = 1;
                return true;
            }
            // in any other case, set current to newStart-buffer
            else {
                System.out.println("CASE 4 - UPDATING BUFFER TO " + (newStartTile-tileBuffer));
                this.currentTileBufferStart = newStartTile-tileBuffer;
                return true;
            }
        }
    }

    /**
     * Checks whether the current end tile needs to be updated
     * @param newEndTile corresponds to the end tile for the region in view INCLUDES BUFFER
     * @return true if end tile needs to be updated, false otherwise
     */
    public boolean updateCurrentTileEnd(int newEndTile) {
        // find max end for the chromosome
        int maxEndTile = getStartInterval((int) currentChrom.getLength());
        // if end is not set yet, set it
        if (this.currentTileBufferEnd == null) {
            this.currentTileBufferEnd = newEndTile;
            return true;
        }
        // already equal
        if (this.currentTileBufferEnd == (newEndTile+tileBuffer)) {
            // do nothing
            System.out.println("CASE 1 END");
            return false;
        }
        // greater than end and needs to be updated to max
        else if (newEndTile+tileBuffer > maxEndTile && currentTileBufferEnd != maxEndTile) {
            System.out.println("CASE 2 END");
            this.currentTileBufferEnd = maxEndTile;
            return true;
        }
        // if buffer should be greater than end, but buffer is already at max, do nothing
        else if (newEndTile+tileBuffer > maxEndTile && currentTileBufferEnd == maxEndTile) {
            System.out.println("CASE 3 END");
            return false;
        }
        else {
            System.out.println("CASE 4 END");
            this.currentTileBufferEnd = newEndTile+tileBuffer;
            return true;
        }
    }

    /**
     * Gets the current chromosome in view
     * @return Chromosome object in view/active
     */
    public Chromosome getCurrentChrom() {
        return this.currentChrom;
    }

    /**
     * Sets the current chromosome that is in view
     * @param chrom Chrom that is currently in view/active
     */
    public void setCurrentChrom(Chromosome chrom) {
        this.currentChrom = chrom;
    }

    /**
     * Reads file content into a string
     * @param file File object to read
     * @return String of file content
     * @throws java.io.IOException if an I/O error occurs reading the stream
     */
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
        // extra 0.5 adds half a sample to bottom allowing all samples to fit fully and prevent scrolling
        double scale = ((panelHeight - SBHeight - AFTrackHeight) / (samples.size()+0.5)) / this.originalTrackHeight;
        System.out.println("SCALE IS " + scale);
        return scale - 1;
    }

    /**
     * Gets the zoom level
     * @return double value of the zoom level
     */
    public double getZoomLevel() {
        return this.zoomLevel;
    }

    /**
     * Updates the zoom level by a factor
     * @param factor the factor the current zoom level will be multiplied by
     * @param chrom Chromosome object in view
     * @param viewportWidth width of scrollpane
     * @param verticalSBWidth width of vertical scrollbar for scrollpane
     * @param start
     * @param oldProportion
     * @return
     */
    public Pair<String, Double> updateZoomLevelByFactor(double factor, Chromosome chrom, double viewportWidth, double verticalSBWidth, double start, double oldProportion) {
        double testZoomLevel = this.zoomLevel * factor;
        // test what the content width would be
        double contentWidth = chrom.getLength() * testZoomLevel;
        double proportionVisible = viewportWidth / contentWidth;
        double selectedZoom;

        // test the new proportion, start and center
        int newProportion = (int) this.getGenomicProportion(viewportWidth, chrom, testZoomLevel);
        int intStart = (int) start;
        int centerStart = intStart + (int) oldProportion/2;
        int end = (int) (start + oldProportion);

        // if protruding on both ends
        if (proportionVisible > 1) {
            // zoom level should correspond to whole chromosome
            selectedZoom = (viewportWidth + verticalSBWidth) / chrom.getLength();
            // update and return
            this.zoomLevel = selectedZoom;
            return new Pair("ABSOLUTE CENTER", selectedZoom);
        }
        // otherwise the zoom level can be the tested level, update
        else {
            selectedZoom = testZoomLevel;
            this.zoomLevel = selectedZoom;
        }

        // check if right edge is greater than end
        if (centerStart + (int) (newProportion/2) >= chrom.getLength()) {
            return new Pair("RIGHT", selectedZoom);
        }
        // check if left edge is less than start
        else if (centerStart - (int) (newProportion/2) <= 1) {
            return new Pair("LEFT", selectedZoom);
        }
        // otherwise should be centered
        else {
            return new Pair("CENTER", selectedZoom);
        }
    }

    /**
     * Set the zoom level
     * @param level double value to set the zoom level
     */
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

    /**
     * Get the sample colors
     * @return Map with Sample name and associated Color
     */
    public HashMap<String, Color> getSampleColors() {
        return this.sampleColors;
    }

    /**
     * Get reference chromosomes
     * @return LinkedMap with String name and Chromosome object
     */
    public LinkedHashMap<String,Chromosome> getRefChromosomes() {
        return this.refChromosomes;
    }

    /**
     * Get the total length of the reference (All chromosomes)
     * @return long value that corresponds to the total length of all chromosomes
     */
    public long getRefTotalLength() {
        return this.refTotalLength;
    }

    /**
     * Get the base font size for Sample and annotation labels
     * @return double value for base font size for Sample and annotation labels
     */
    public double getBaseFontSize() {
        return this.baseFontSize;
    }

    /**
     * Get the default height of SV tracks
     * @return int for the default (hard-coded) initial height of SV tracks
     */
    public int getOriginalTrackHeight() {
        return this.originalTrackHeight;
    }

    /**
     * Add a new selection to structure
     * @param selection Selection object to add to the selections structure
     */
    public void addSelection(Selection selection) {
        this.selections.add(selection);
    }

    /**
     * Find the different calls from pinned samples
     * @param checkboxes ArrayList of checkbox items for samples
     * @return ArrayList of calls that are different from pinned samples
     */
    public ArrayList<Call> getDiffCallsFromPinned(ArrayList<CheckBox> checkboxes) {
        // create result structures
        ArrayList<Sample> checkedSamples = new ArrayList<>();
        ArrayList<Call> result = new ArrayList<>();
        // loop through sample checkboxes and add ones that are checked
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
            // loop through each selection
            for (Selection selection : selections) {
                // get coordinates for selection and starting and ending tiles
                double selectionStart = selection.getGenomicStart();
                double selectionEnd = selection.getGenomicEnd();
                int startInterval = getStartInterval((int) selectionStart);
                int endInterval = getEndInterval((int) selectionEnd);
                // create structure to hold IDs for pinned calls
                ArrayList<String> pinnedCallIds = new ArrayList<>();
                // loop through each tile
                for (int i = startInterval; i <= endInterval; i++) {
                    // loop through pinned samples
                    for (Sample checkedSample : checkedSamples) {
                        // loop through the calls
                        for (Call currentCall : allTiles.get(refChromosomes.get(selection.getChromosome())).get(i).getSampleCalls().get(checkedSample)) {
                            // if in region
                            if (currentCall.getStart() > selectionStart && currentCall.getEnd() < selectionEnd) {
                                // if this call has already been seen with another pinned sample, do nothing
                                if (pinnedCallIds.contains(currentCall.getId())) {
                                    // do nothing
                                }
                                // otherwise, add it to pinned calls
                                else {
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
                        // if sample is in checked samples, don't process
                        if (checkedSamples.contains(sample)) {
                            // do nothing
                        }
                        // otherwise an unchecked sample, process calls
                        else {
                            // loop through the calls
                            for (Call currentCall : allTiles.get(refChromosomes.get(selection.getChromosome())).get(i).getSampleCalls().get(sample)) {
                                // if in region
                                if (currentCall.getStart() > selectionStart && currentCall.getEnd() < selectionEnd) {
                                    // if this call has already been seen with another pinned sample, do nothing
                                    if (pinnedCallIds.contains(currentCall.getId())) {
                                        // do nothing
                                    }
                                    // otherwise it is different so add it to results
                                    else {
                                        // results already contain call, do nothing
                                        if (result.contains(currentCall)) {
                                            // do nothing
                                        }
                                        // otherwise add call to results
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
            }
            return result;
        }
    }

    /**
     * Find the same calls as pinned samples
     * @param checkboxes ArrayList of checkbox items for samples
     * @return ArrayList of calls that are the same as pinned
     */
    public ArrayList<Call> getSameCallsFromPinned(ArrayList<CheckBox> checkboxes) {
        // create result structures
        ArrayList<Sample> checkedSamples = new ArrayList<>();
        ArrayList<Call> result = new ArrayList<>();
        // loop through checkboxes and add to structure if checked
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
            // loop through selections
            for (Selection selection : selections) {
                // get start and end coordinates, and start and end tiles
                double selectionStart = selection.getGenomicStart();
                double selectionEnd = selection.getGenomicEnd();
                int startInterval = getStartInterval((int) selectionStart);
                int endInterval = getEndInterval((int) selectionEnd);
                // create structure to hold IDs for pinned calls
                ArrayList<String> pinnedCallIds = new ArrayList<>();
                // loop through each tile
                for (int i = startInterval; i <= endInterval; i++) {
                    // loop through pinned samples
                    for (Sample checkedSample : checkedSamples) {
                        // loop through the calls
                        for (Call currentCall : allTiles.get(refChromosomes.get(selection.getChromosome())).get(i).getSampleCalls().get(checkedSample)) {
                            // if in region
                            if (currentCall.getStart() > selectionStart && currentCall.getEnd() < selectionEnd) {
                                // if this call has already been seen with another pinned sample, do nothing
                                if (pinnedCallIds.contains(currentCall.getId())) {
                                    // do nothing
                                }
                                // otherwise add it to seen and add it to results if not added already
                                else {
                                    pinnedCallIds.add(currentCall.getId());
                                    // if already added to results, do nothing
                                    if (result.contains(currentCall)) {
                                        // do nothing
                                    }
                                    // otherwise add call to results
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

    /**
     * Processes the selection for Color by Haplotype
     * @param sampleOrder order of Samples in the view
     * @return Map of rectangles and their color to display the haplotype plot
     */
    public HashMap<Rectangle,Color> processHaplotypeSelections(ArrayList<Sample> sampleOrder) {
        /*
        Preconditions: Assumes that sample order may have been manipulated by pinning
        Postconditions: Does NOT do any reordering
         */
        // create result structure
        HashMap<Rectangle,Color> result = new HashMap<>();
        // if no selections are made, return empty hashmap
        if (this.selections.isEmpty()) {
            return result;
        }
        // if selections are made, process
        else {
            // loop through selections
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
                // loop through each SV call in chromosome
                for (Map.Entry<Integer,Tile> entry : this.allTiles.get(this.refChromosomes.get(selection.getChromosome())).entrySet()) {
                    for (Call call : entry.getValue().getTileCalls()) {
                        // include the call if it is within the selection region (doesn't have to be completely within)
                        if ((call.getStart() > selectionStart && call.getEnd() < selectionEnd ||
                                call.getStart() < selectionStart && call.getEnd() > selectionStart ||
                                call.getStart() < selectionEnd && call.getEnd() > selectionEnd) && Objects.equals(call.getChromosome(), selection.getChromosome())) {
                            // loop through each sample in view order
                            for (int i = 0; i < sampleOrder.size(); i++) {
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
                                        for (int j = 0; j < i; j++) {
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
                                        for (int j = 0; j < equiv.get(curName).size(); j++) {
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
                                                } else {
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
                }
                double calcStart = selection.getPixelStart() * zoomLevel / selection.getZoomLevel();
                double calcLength = selection.getPixelLength() * zoomLevel / selection.getZoomLevel();
                int curIndex = 0;
                for (Sample sample : sampleOrder) {
                    Rectangle newRect = new Rectangle(calcStart, (curIndex*(originalTrackHeight*trackHeightScale))+(this.AFTrackHeight), calcLength, (originalTrackHeight*trackHeightScale));
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

    /**
     * Returns a random color in RGB format
     * @return Color object of random RGB color
     */
    public static Color getRandomColor() {
        return Color.rgb(new Random().nextInt(256), new Random().nextInt(256), new Random().nextInt(256));
    }

    /**
     * Gets all samples
     * @return ArrayList of Sample objects
     */
    public ArrayList<Sample> getSamples() {
        return this.samples;
    }

    /**
     * Gets all Selections
     * @return ArrayList of Selection objects
     */
    public ArrayList<Selection> getSelections() {
        return this.selections;
    }

    /**
     * Clears the selections structure
     */
    public void clearSelections() {
        this.selections.clear();
    }

    /**
     * Updates the track height by the increment
     * @param increment double value to increment trackHeightScale by
     */
    public void updateTrackHeightScale(double increment) {
        // if less than 10 pixels too small
        if ((this.trackHeightScale + increment) < 0.1) {
            // do nothing, too small
        }
        // otherwise increase increment
        else {
            this.trackHeightScale += increment;
        }
    }

    /**
     * Checks if a String region is a valid region (outside of the bounds of chrom)
     * @param regionText String of region to check if valid
     * @return true if region is valid, false otherwise
     */
    public boolean checkIfValidRegion(String regionText) {
        String regex = "(.+):(\\d+)-(\\d+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(regionText);
        // if string is properly formatted
        if (matcher.find()) {
            // extract chrom, start and end
            String chrom = matcher.group(1);
            int start = Integer.parseInt(matcher.group(2));
            int end = Integer.parseInt(matcher.group(3));
            // check if chromosome exists
            if (refChromosomes.containsKey(chrom)) {
                // check if positions are in range
                if (start > 0 && end <= refChromosomes.get(chrom).getLength()) {
                    return true;
                }
                // if not in range, return false
                else {
                    return false;
                }
            }
            // if chromosome is not known, return false
            else {
                return false;
            }
        }
        // if not properly formatted, return false
        else {
            return false;
        }
    }

    /**
     * Gets the trackHeightScale
     * @return double value for trackHeightScale
     */
    public double getTrackHeightScale() {
        return this.trackHeightScale;
    }

    /**
     * Main function for processing the data within the VCF file
     * @param fileContent String of read VCF data
     */
    public void processFile(String fileContent) {
        // splits on \n or \r\n (carriage return or newline)
        String[] lines = fileContent.split("\\r?\\n");
        // loop through each line in the file
        for (String line : lines) {
            // meta-info line
            if (line.startsWith("##")) {
                // try to match contig lines for reference sequence
                String regex = "contig=<ID=(.+),length=(\\d+)>";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(line);
                // assign reference length if match is found, otherwise exit
                if (matcher.find()) {
                    // create new Chromosome, add to refChromosomes, and increment the total ref length
                    this.refChromosomes.put(matcher.group(1), new Chromosome(matcher.group(1), Integer.parseInt(matcher.group(2)), this.refTotalLength, this.tileSize));
                    this.refTotalLength += Integer.parseInt(matcher.group(2));
                    // add Chromosome to the tile data structure and add empty hashmap to store tiles
                    this.allTiles.put(this.refChromosomes.get(matcher.group(1)), new HashMap<>());
                }
            }
            // header line with sample info
            else if (line.startsWith("#")) {
                // add <ALL> to refChromosomes now that all meta-info lines have been processed and total ref length is known
                this.refChromosomes.put("<ALL>", new Chromosome("<ALL>", this.refTotalLength, 1, this.tileSize));
                String[] header = line.split("\t");
                // create samples
                this.createSamples(Arrays.copyOfRange(header, 9, header.length));
                // initializes tiles for each chromosome now that all meta-info lines have been processed
                for (Chromosome chrom : this.refChromosomes.values()) {
                    // ignore <ALL>
                    if (Objects.equals(chrom.getName(), "<ALL>")) {
                        // do nothing
                    }
                    else {
                        this.initTilesForChromosome(chrom);
                    }
                }
            }
            // call line
            else {
                int startCol = 9;
                String[] fields = line.split("\t");
                HashMap<String,String> genotypes = new HashMap<>();
                // regex for type and length, use ? to make non greedy and match as little as possible (to the first semi colon)
                String typeInfoRegex = "SVTYPE=(.+?);";
                String lengthInfoRegex = "SVLEN=(.+?);";
                Pattern typeInfoPattern = Pattern.compile(typeInfoRegex);
                Pattern lengthInfoPattern = Pattern.compile(lengthInfoRegex);
                Matcher typeInfoMatcher = typeInfoPattern.matcher(fields[7]);
                Matcher lengthInfoMatcher = lengthInfoPattern.matcher(fields[7]);
                // cannot match type
                if (!typeInfoMatcher.find()) {
                    System.err.println("Error: Could not find type in expected VCF format for call. Ignoring call.");
                }
                // cannot match length
                else if (!lengthInfoMatcher.find()) {
                    System.err.println("Error: Could not find length in expected VCF format for call. Ignoring call.");
                }
                // process call
                else {
                    // if not a supported type, continue
                    if (!supportedSVTypes.contains(typeInfoMatcher.group(1))) {
                        System.err.println("Error: SV type: " + typeInfoMatcher.group(1) + " is not supported. Ignoring call.");
                    }
                    // otherwise, process
                    else {
                        // make sure chrom was processed earlier
                        long absoluteStart = 0;
                        try {
                            absoluteStart = refChromosomes.get(fields[0]).getAbsoluteStart() + Integer.parseInt(fields[1]);
                        } catch (NullPointerException e) {
                            System.err.println("Could not identify Chromosome " + fields[0] + ". Ignoring call.");
                            continue;
                        }
                        // create new Call object
                        Call currentCall = new Call(typeInfoMatcher.group(1), Integer.parseInt(lengthInfoMatcher.group(1)), fields[0], fields[5], fields[6], Integer.parseInt(fields[1]), absoluteStart, fields[4], fields[2], genotypes);
                        // loop through samples and add genotypes
                        for (Sample sample : this.samples) {
                            // this regex captures polyploid and phased samples, matches:
                            // start of line, then not a colon and at least one character until a colon is reached
                            String genotypeRegex = "^([^:]+):";
                            Pattern genotypePattern = Pattern.compile(genotypeRegex);
                            Matcher genotypeMatcher = genotypePattern.matcher(fields[startCol]);
                            // assign reference length if match is found
                            System.out.println("LINE IS " + currentCall);
                            if (genotypeMatcher.find()) {
                                System.out.println("GENOTYPE FOR SAMPLE " + sample.getName() + " IS " + genotypeMatcher.group(1));
                                genotypes.put(sample.getName(), genotypeMatcher.group(1));
                                if (genotypeMatcher.group(1).contains("1")) {
                                    currentCall.addViewNodes(sample);
                                }
                            }
                            // otherwise print error
                            else {
                                System.err.println("Error: Could not find genotype for a sample on line:" + line + " . Ignoring call.");
                            }
                            startCol++;
                        }
                        // add call to appropriate tile
                        this.addToTiledCalls(currentCall);
                        // after all genotypes added, calculate allele frequency
                        currentCall.setAlleleFreq();
                    }
                }
            }
        }
    }

    /**
     * Takes a Chromosome object and initializes the tiles in the data structure [1,end] (inclusive). Also creates the
     * structure to hold calls for each Sample in each Tile.
     * @param chrom Chromosome to initialize tiles
     */
    public void initTilesForChromosome(Chromosome chrom) {
        // get end tile
        int end = this.getEndInterval((int) chrom.getLength());
        // loop through tile intervals for chromosome
        for (int i=1; i<= end; i++) {
            // create new tile and add to allTiles structure for that Chromosome
            allTiles.get(chrom).put(i, new Tile(i, (i*tileSize)-tileSize, (i*tileSize)-1));
            // add each Sample with an empty arraylist to hold calls for that Tile
            for (Sample sample : samples) {
                allTiles.get(chrom).get(i).getSampleCalls().put(sample, new ArrayList<>());
            }
        }
    }

    /**
     * Given Call is added to the Tile calls, as well as to Sample calls for that Tile
     * @param currentCall Call to add to Tile
     */
    public void addToTiledCalls(Call currentCall) {
        // find the appropriate tile
        Tile tile = this.findTileForCall(currentCall);
        // add to all calls for that tile
        tile.add(currentCall);
        // loop through samples and add if heterozygous or homozygous alternate
        for (Sample sample : samples) {
            if (currentCall.getGenotypes().get(sample.getName()).contains("1")) {
                tile.addSampleCall(sample, currentCall);
            }
        }
    }

    /**
     * The Tile the given Call belongs to is identified, based on chromosome and start position
     * @param call
     * @return
     */
    public Tile findTileForCall(Call call) {
        // find tile number
        int num = ((call.getStart()/tileSize)+1);
        // extract tile from chromosome
        Tile tile = allTiles.get(refChromosomes.get(call.getChromosome())).get(num);
        return tile;
    }

    /**
     * Processes and initializes the samples in the system
     * @param sampleNames String names of Samples
     */
    public void createSamples(String[] sampleNames) {
        for (int i=0; i<sampleNames.length; i++) {
            Sample sample = new Sample(sampleNames[i]);
            this.samples.add(sample);
            this.sampleColors.put(samples.get(i).getName(), this.getRandomColor());
        }
    }

    /**
     * Gets the appropriate tile based on a coordinate and the stored tile size
     * @param start start coordinate to determine tile for
     * @return int value of the tile
     */
    public int getStartInterval(int start) {
        return ((start - 1) / this.tileSize + 1);
    }

    /**
     * Gets the appropriate tile based on a coordinate and the stored tile size
     * @param end end coordinate to determine tile for
     * @return int value of the tile
     */
    public int getEndInterval(int end) {
        return (end - 1) / this.tileSize + 1;
    }

    /**
     * Updates the appropriate coord increment based on zoom level
     * @param viewportWidth width of the scrollpane viewport
     * @param chrom chromosome in view
     */
    public void updateCoordIncrement(double viewportWidth, Chromosome chrom) {
        // get genomic proportion in view
        double genomicProportion = getGenomicProportion(viewportWidth, chrom, this.zoomLevel);
        // if less than 100 MB
        if (genomicProportion < 100000000) {
            // ideal is 7 ticks in the view
            double ideal = genomicProportion / 7;
            // find the closest coord increment and update
            this.coordIncrementIndex = getClosestIntegerValue(ideal);
        }
        // last increment which corresponds to 100 MB
        else {
            this.coordIncrementIndex = this.increments.size() - 1;
        }
    }

    /**
     * Gets the genomic proportion (length) in view
     * @param viewportWidth width of the scrollpane viewport
     * @param chrom chromosome in view
     * @param zoomLevel current zoom level
     * @return the genomic length of the region in view
     */
    public double getGenomicProportion(double viewportWidth, Chromosome chrom, double zoomLevel) {
        // content width in pixels
        double contentWidth = chrom.getLength() * zoomLevel;
        // proportion of viewport width to entire content width
        double proportionVisible = viewportWidth / contentWidth;
        // get length of genomic proportion in view
        return proportionVisible * chrom.getLength();
    }

    /**
     * Loop through the coordinate increments and find the one that closely matches such that 7 ticks are shown
     * in the screen
     * @param idealSpacing the space that would be between 7 ticks
     * @return the index in the increment in the arraylist that is most ideal for 7 ticks (has the least difference)
     */
    public int getClosestIntegerValue(double idealSpacing) {
        int closestIndex = 0;
        // compare against the first increment
        double minDiff = Math.abs(this.increments.getFirst() - idealSpacing);
        // loop through all increments, THIS IS 1 BECAUSE FIRST ELEMENT ALREADY CHECKED
        for (int i = 1; i < this.increments.size(); i++) {
            // get the difference
            double diff = Math.abs(this.increments.get(i) - idealSpacing);
            // if difference is less than observed yet, then update
            if (diff < minDiff) {
                minDiff = diff;
                closestIndex = i;
            }
        }
        // return the closest increment for that spacing
        return closestIndex;
    }

    /**
     * Gets the current tick spacing being used
     * @return integer that is the increment between ticks in the coordinate system
     */
    public int getTickSpacing() {
        return increments.get(coordIncrementIndex);
    }

    /**
     * Gets the hard-coded height of the allele frequency track
     * @return integer value that is the hard-coded pixel height of the allele frequency track
     */
    public int getAFTrackHeight() {
        return this.AFTrackHeight;
    }
}
