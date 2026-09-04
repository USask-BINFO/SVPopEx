package com.javafxapp;

import java.io.File;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;


public class Controller {
    private Model model;
    private View view;
    // constructor
    public Controller(Model model, View view) {
        this.model = model;
        this.view = view;
        // listeners
        view.importListener(e -> {
            this.importFile();
        });
        view.gffImportListener(e -> {
            this.importGFF();
        });
        view.zoomInListener(e -> {
            Button source = (Button) e.getSource();
            String text = source.getText();
            this.updateZoom(text);
        });
        view.zoomOutListener(e -> {
            Button source = (Button) e.getSource();
            String text = source.getText();
            this.updateZoom(text);
        });
        view.clearSelectionsListener(e -> {
            this.clearSelections();
        });
        view.processSelectionsListener(e -> {
            this.processSelections();
        });
        view.processShowSameCallsListener(e -> {
            this.processShowSameCalls();
        });
        view.processShowDiffCallsListener(e -> {
            this.processShowDiffCalls();
        });
        view.shrinkTrackHeightListener(e -> {
            this.updateTrackHeight(-0.1);
        });
        view.growTrackHeightListener(e -> {
            this.updateTrackHeight(0.1);
        });
        view.releaseSelectionListener(e -> {
            this.updateReleaseSelection(e);
        });
        view.toggleSidePaneListener(e -> {
            // if opening is not successful, then close
            if (!view.openSidePane()) {
                view.closeSidePane();
            }
        });
        view.closeSidePaneListener(e -> {
            view.closeSidePane();
        });
        view.chromComboBoxListener(e -> {
            // to prevent triggering this as combo box is cleared while resetting
            if (!view.isResetting()) {
                String selectedChrom = view.chromComboBox.getValue();
                this.showChromosome(selectedChrom);
            }
        });
        view.processRegionButtonListener(e -> {
            this.processCustomRegion(view.getTextFieldRegion());
        });
        view.showMateButtonListener(e -> {
            String result = this.processShowMate();
            if (result.equals("null")) {
                // do nothing, some incorrect or error found
            }
            else {
                this.processCustomRegion(result);
            }
        });
    }

    /**
     * Function that is called immediately after GFF file is selected; processes file and shows annotations in new track
     */
    public void importGFF() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open GFF3 file");
        File file = chooser.showOpenDialog(view.getPrimaryStage());
        if (file != null) {
            // try to read in file
            String fileContent = "";
            try {
                // get model to read file
                fileContent = model.loadFile(file);
            }
            catch (Exception ex) {
                System.err.println("Error: Could not load selected file. Exiting.");
                System.exit(1);
            }
            String trackID = model.processGFFFile(fileContent);
            view.addNewFeatureID(trackID);
            view.createNewAnnotationTrack(model.getRefTotalLength(), model.getZoomLevel(), "Genes", model.getBaseFontSize(), model.getFeatureTrackHeight(), "GENES", trackID);
            // do this to trigger correctly scaling the tracks with the new track
            this.updateTrackHeight(0);
            view.showAnnotations(trackID, model.getRefChromosomes(), model.getCurrentChrom(), model.getZoomLevel(), model.getTiles(), -1, -1);
        }

    }

    /**
     * Function that is called immediately after VCF file is selected; processes VCF file and shows visualization
     */
    public void importFile() {
        // create and show the open file dialog. returns int indicating how user closed dialog
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open File");
        File file = chooser.showOpenDialog(view.getPrimaryStage());
        // user clicked file to open
        if (file != null) {
            // try to read in file
            String fileContent = "";
            try {
                // get model to read file
                fileContent = model.loadFile(file);
            }
            // could not read in file
            catch (Exception ex) {
                System.err.println("Error: Could not load selected file. Exiting.");
                System.exit(1);
            }
            // SEQUENCE FOR SHOWING VCF DATA
            // reset model and view
            this.model.reset();
            this.view.reset();
            // process VCF file
            System.out.println("PROCESSING FILE...");
            model.processFile(fileContent);
            // set current chromosome to <ALL> and initialize and draw reference
            System.out.println("INIT AND DRAW REFERENCE...");
            model.setCurrentChrom(model.getRefChromosomes().get("<ALL>"));
            view.initReference(model.getRefChromosomes(), model.getRefTotalLength());
            view.drawReference(model.getRefChromosomes(), "<ALL>");
            // set zoom and show marker and coordinate system
            System.out.println("SETTING ZOOM AND SHOWING MARKER AND COORDS...");
            model.setZoom(view.initZoomWG(model.getRefTotalLength()));
            view.showCoords(model.getCurrentChrom(), -1, model.getZoomLevel(), model.getRefChromosomes());
            view.updateMarker(model.getCurrentChrom(), model.getZoomLevel(), model.getCurrentChrom().getLength(), 0);
            System.out.println("SHOW SIDE PANE...");
            // initialize side pane and samples
            view.initSidePane(model.getSamples(), model.getSampleColors());
            System.out.println("SHOW SAMPLES....");
            view.initSamples(model.getSamples(), model.getSampleColors(), model.getRefTotalLength(), model.getZoomLevel(), model.getBaseFontSize(), model.getOriginalTrackHeight(), model.getAFTrackHeight());
            // enable controls
            System.out.println("ENABLE CONTROLS...");
            view.enableControls();
            // set chromosome selector to <ALL> which triggers showChromosome()
            view.setChromComboBoxValue("<ALL>");
            // update track height to fit all samples
            this.updateTrackHeight(model.fitAllSamplesIncrement(view.getCallsPanelHeight(), view.getHorizontalSBHeight()));
            // set up listeners
            view.browserDragged((obs,oldVal, newVal) -> {
                if (model.getChangingChromInView()) {
                    // do nothing
                }
                else {
                    this.processScrollChange(oldVal.doubleValue(), newVal.doubleValue());
                }
            });
            view.markerDragged(e -> {
                this.processMarkerDragged(e);
            });
        }
        // user closed or cancelled file
        else {
            System.out.println("File was not chosen.");
        }
    }

    /**
     * Controls updating the zoom
     * @param text String text of the button that is pressed (either zoom in or out)
     */
    public void updateZoom(String text) {
        // if the chromosome is all, do nothing
        if (Objects.equals(model.getCurrentChrom().getName(), "<ALL>")) {
            // not meant to zoom in on <ALL> region so do nothing
        }
        // otherwise, process
        else {
            double factor;
            // set factor based on zoom in or out
            if (Objects.equals(text, "Zoom +")) {
                factor = 1.5;
            }
            else if (Objects.equals(text, "Zoom -")) {
                factor = 0.5;
            }
            else {
                throw new IllegalArgumentException("Unexpected zoom button text " + text);
            }
            // get proportion of the screen, start coordinate, and center coordinate
            double oldProportion = model.getGenomicProportion(view.getViewportWidth(), model.getCurrentChrom(), model.getZoomLevel());
            double start = view.getStartFromHVal(view.getHValue(), model.getCurrentChrom(), model.getZoomLevel());
            int intStart = (int) start;
            int centerStart = intStart + (int) oldProportion/2;

            // update zoom level
            Pair<String, Double> result = model.updateZoomLevelByFactor(factor, model.getCurrentChrom(), view.getViewportWidth(), view.getVerticalSBWidth(), start, oldProportion);
            double level = result.y;
            String anchor = result.x;

            // update coord increment
            model.updateCoordIncrement(view.getViewportWidth(), model.getCurrentChrom());
            // show coords
            view.showCoords(model.getCurrentChrom(), model.getTickSpacing(), model.getZoomLevel(), model.getRefChromosomes());
            // update selections
            view.updateSelections(model.getSelections(), level);

            // update newStart based on result
            // get new proportion based on new zoom level
            double newProportion = model.getGenomicProportion(view.getViewportWidth(), model.getCurrentChrom(), model.getZoomLevel());
            long newStart = centerStart - (int) (newProportion/2);
            long newEnd = centerStart + (int) (newProportion/2);
            if (Objects.equals(anchor, "ABSOLUTE CENTER")) {
                newStart = 1;
                newEnd = model.getCurrentChrom().getLength();
            }
            else if (Objects.equals(anchor, "CENTER")) {
                newStart = centerStart - (int) (newProportion/2);
            }
            else if (Objects.equals(anchor, "LEFT")) {
                newStart = 1;
                newEnd = (int) (1 + newProportion);
            }
            else if (Objects.equals(anchor, "RIGHT")) {
                newStart = model.getCurrentChrom().getLength() - (int) newProportion;
                newEnd = model.getCurrentChrom().getLength();
            }

            // show calls
            //System.out.println("UPDATED ZOOM SHOWING START: " + newStart + " and END : " + newEnd);
            boolean updateStart = model.updateCurrentTileStart(model.getInterval((int) newStart));
            boolean updateEnd = model.updateCurrentTileEnd(model.getInterval((int) newEnd));
            // show calls and allele frequency
            view.showSVCalls(model.getRefChromosomes(), model.getCurrentChrom(), model.getSamples(), level, model.getOriginalTrackHeight(), model.getTiles(), model.getBufferStartTile(), model.getBufferEndTile());
            view.showAlleleFreq(model.getRefChromosomes(), model.getCurrentChrom(), level, model.getAFTrackHeight(), model.getTiles(), model.getInterval((int) newStart), model.getInterval((int) newEnd));
            for (String annotationID : model.getAnnotationIDs()) {
                view.showAnnotations(annotationID, model.getRefChromosomes(), model.getCurrentChrom(), model.getZoomLevel(), model.getTiles(), model.getBufferStartTile(), model.getBufferEndTile());
            }
            // update scroll and marker based on newStart and offset (calculated from newStart)
            double offset = ((double) newStart / model.getCurrentChrom().getLength()) * view.getMarkerWrapperWidth();
            view.syncScroll(view.setScroll((int) newStart, model.getCurrentChrom(), model.getZoomLevel()));
            view.setScroll((int) newStart, model.getCurrentChrom(), model.getZoomLevel());
            view.updateMarker(model.getCurrentChrom(), level, model.getGenomicProportion(view.getViewportWidth(), model.getCurrentChrom(), model.getZoomLevel()), offset);
        }
    }

    /**
     * Clears all selections made
     */
    public void clearSelections() {
        // updates view and model
        view.clearAllSelections();
        model.clearSelections();
    }

    /**
     Controls processing the selections for Color by Haplotype
     **/
    public void processSelections() {
        view.showPlot(model.processHaplotypeSelections(view.getSampleOrderInView()));
    }

    /**
     * Controls showing the same calls as pinned
     */
    public void processShowSameCalls() {
        // want to show the same calls, so we are going to hide the different ones
        view.hideCalls(view.getSampleOrderInView(), model.getDiffCallsFromPinned(view.getPinCheckboxes()));
    }

    /**
     Controls showing diff calls from pinned
     **/
    public void processShowDiffCalls() {
        view.hideCalls(view.getSampleOrderInView(), model.getSameCallsFromPinned(view.getPinCheckboxes()));
    }

    /**
     * Controls update when a selection is made
     * @param e the mouse event from releasing the mouse
     */
    public void updateReleaseSelection(MouseEvent e) {
        // create new selection
        Selection selection = new Selection(view.getSelectionRectangle().getX(), e.getX(), model.getCurrentChrom().getName(), model.getZoomLevel());
        // update model and view
        model.addSelection(selection);
        view.clearActiveSelection();
    }

    /**
     * Controls updating the track height scale
     * @param increment double value to increment height by
     */
    public void updateTrackHeight(double increment) {
        // update in model
        model.updateTrackHeightScale(increment);
        // scale tracks in view and redraw sample info
        view.updateTrackHeight(model.getSamples(), model.getTrackHeightScale());
        view.redrawSampleInfoAfterScale(model.getSamples(), model.getBaseFontSize(), model.getTrackHeightScale(), model.getOriginalTrackHeight());
    }

    /**
     * Controls updates after chromosome is selected in dropdown box
     * @param selectedChrom the String name of the selected Chromosome
     */
    public void showChromosome(String selectedChrom) {
        model.changingChromosome(true);
        // set new chromosome
        Chromosome chrom = model.getRefChromosomes().get(selectedChrom);
        model.setCurrentChrom(chrom);
        // update zoom level
        model.updateZoomLevelByRegion(chrom.getLength(), view.getViewportWidth(), view.isVerticalSBVisible(), view.getVerticalSBWidth());
        // update coord increment
        model.updateCoordIncrement(view.getViewportWidth(), model.getCurrentChrom());
        // show coords
        double newVal = 0.0;
        view.syncScroll(newVal);
        view.showCoords(model.getCurrentChrom(), model.getTickSpacing(), model.getZoomLevel(), model.getRefChromosomes());
        // show calls
        view.showSVCalls(model.getRefChromosomes(), chrom, model.getSamples(), model.getZoomLevel(), model.getOriginalTrackHeight(), model.getTiles(), -1, -1);
        for (String annotationID : model.getAnnotationIDs()) {
            view.showAnnotations(annotationID, model.getRefChromosomes(), model.getCurrentChrom(), model.getZoomLevel(), model.getTiles(), -1, -1);
        }
        // show allele frequency
        view.showAlleleFreq(model.getRefChromosomes(), chrom, model.getZoomLevel(), model.getAFTrackHeight(), model.getTiles(), -1, -1);
        // show reference and marker
        view.drawReference(model.getRefChromosomes(), model.getCurrentChrom().getName());
        view.updateMarker(chrom, model.getZoomLevel(), chrom.getLength(), 0);
        model.changingChromosome(false);

    }

    /**
     * Controls showing the mate region when the 'show mate' button is clicked in the side pane
     * @return custom region to show for mate region, uses the same genomic proportion as when the button is clicked
     */
    public String processShowMate() {
        // get genomic proportion of screen
        long length = (int) model.getGenomicProportion(view.getViewportWidth(), model.getCurrentChrom(), model.getZoomLevel());
        // get alternate and extract mate region between brackets
        String alternate = "";
        if (view.getLiveComponent() instanceof Call liveCall) {
            alternate = liveCall.getAlternate();
        }
        Pattern pattern = Pattern.compile("[\\[\\]](.+)[\\[\\]]");
        Matcher altInfo = pattern.matcher(alternate);
        // if alternate region extracted
        if (altInfo.find()) {
            // find region
            Pattern regionPattern = Pattern.compile("(.+):(.+)");
            Matcher region = regionPattern.matcher(altInfo.group(1));
            // if region in correct format
            if (region.find()) {
                // extract chromosome and coordinate
                String chrom = region.group(1);
                long coords = Integer.parseInt(region.group(2));
                // get start and end of screen by adding proportion
                long start = coords - length;
                long end = coords + length;

                // make sure start is in range
                if (start < 1) {
                    start = 1;
                }
                else {
                    // do nothing
                }

                // make sure end is in range
                if (end > model.getRefChromosomes().get(chrom).getLength()) {
                    end = model.getRefChromosomes().get(chrom).getLength();
                }
                else {
                    // do nothing
                }
                // return custom region
                return chrom + ":" + start + "-" + end;
            }
        }
        // otherwise return null
        return "null";
    }

    /**
     * Controls updating when the browser is scrolled (the hvalue property of scrollpane changes)
     * @param oldVal previous hvalue
     * @param newVal new hvalue after scroll
     */
    public void processScrollChange(double oldVal, double newVal) {
        view.syncScroll(newVal);
        // get genomic proportion, start and end value in view
        double proportion = model.getGenomicProportion(view.getViewportWidth(), model.getCurrentChrom(), model.getZoomLevel());
        double start = view.getStartFromHVal(newVal, model.getCurrentChrom(), model.getZoomLevel());
        double end = start + proportion;
        // check if need to update tiles
        boolean updateStart = model.updateCurrentTileStart(model.getInterval((int) start));
        boolean updateEnd = model.updateCurrentTileEnd(model.getInterval((int) end));
        // if yes, then update tiles and show new SV calls and allele frequency
        if (updateStart || updateEnd) {
            view.showSVCalls(model.getRefChromosomes(), model.getCurrentChrom(), model.getSamples(), model.getZoomLevel(), model.getOriginalTrackHeight(), model.getTiles(), model.getBufferStartTile(), model.getBufferEndTile());
            for (String annotationID : model.getAnnotationIDs()) {
                view.showAnnotations(annotationID, model.getRefChromosomes(), model.getCurrentChrom(), model.getZoomLevel(), model.getTiles(), model.getBufferStartTile(), model.getBufferEndTile());
            }
            view.showAlleleFreq(model.getRefChromosomes(), model.getCurrentChrom(), model.getZoomLevel(), model.getAFTrackHeight(), model.getTiles(), model.getInterval((int) start), model.getInterval((int) end));
        }
    }

    /**
     * Controls updating the marker after drag
     * @param e the Mouse event from dragging the marker
     */
    public void processMarkerDragged(MouseEvent e) {
        view.updateMarkerOnDrag(e);
    }

    /**
     * Controls processing input in the custom text region input.
     * @param regionText text the user entered as a region to navigate to
     */
    public void processCustomRegion(String regionText) {
        // if region entered is empty, reset text field
        if (Objects.equals(regionText, "")) {
            view.clearRegionField();
        }
        // otherwise
        else {
            // check if input is valid
            if (model.checkIfValidRegion(regionText)) {
                String regex = "(.+):(\\d+)-(\\d+)";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(regionText);
                // if correctly formatted expression found
                if (matcher.find()) {
                    // extract the chromosome, start, end, and calculate length
                    String selectedChrom = matcher.group(1);
                    int start = Integer.parseInt(matcher.group(2));
                    int end = Integer.parseInt(matcher.group(3));
                    int length = end-start;
                    // set new chromosome
                    Chromosome chrom = model.getRefChromosomes().get(selectedChrom);
                    model.setCurrentChrom(chrom);
                    view.chromComboBox.setValue(selectedChrom);
                    // update zoom level
                    model.updateZoomLevelByRegion(length, view.getViewportWidth(), view.isVerticalSBVisible(), view.getVerticalSBWidth());
                    // update coord increment
                    model.updateCoordIncrement(view.getViewportWidth(), model.getCurrentChrom());
                    // show coords
                    view.showCoords(model.getCurrentChrom(), model.getTickSpacing(), model.getZoomLevel(), model.getRefChromosomes());
                    // show calls
                    view.showSVCalls(model.getRefChromosomes(), chrom, model.getSamples(), model.getZoomLevel(), model.getOriginalTrackHeight(), model.getTiles(), model.getInterval((int) start), model.getInterval((int) end));
                    for (String annotationID : model.getAnnotationIDs()) {
                        view.showAnnotations(annotationID, model.getRefChromosomes(), model.getCurrentChrom(), model.getZoomLevel(), model.getTiles(), model.getInterval((int) start), model.getInterval((int) end));
                    }
                    view.showAlleleFreq(model.getRefChromosomes(), chrom, model.getZoomLevel(), model.getAFTrackHeight(), model.getTiles(), model.getInterval((int) start), model.getInterval((int) end));
                    view.drawReference(model.getRefChromosomes(), chrom.getName());
                    double offset = ((double) start / chrom.getLength()) * view.getMarkerWrapperWidth();
                    view.setScroll(start, chrom, model.getZoomLevel());
                    view.updateMarker(chrom, model.getZoomLevel(), length, offset);
                }
                // otherwise show pop up error message
                else {
                    view.showInvalidRegionAlert(regionText);
                }
            }
            // otherwise show pop up error message
            else {
                view.showInvalidRegionAlert(regionText);
            }
        }
        // clear text field once processed
        view.clearRegionField();
    }
}
