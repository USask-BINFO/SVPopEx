package com.javafxapp;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.event.ActionEvent;


public class Controller {
    private Model model;
    private View view;
    public Controller(Model model, View view) {
        this.model = model;
        this.view = view;
        view.importListener(e -> {
            this.importFile();
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
        view.processBlocksSelectionsListener(e -> {
            this.processBlockSelections();
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
            String selectedChrom = view.chromComboBox.getValue();
            this.showChromosome(selectedChrom);
        });
        view.processRegionButtonListener(e -> {
           this.processCustomRegion(view.getTextFieldRegion());
        });
    }

    public void importFile() {
        // create and show the open file dialog (view is the parent). returns int indicating how user closed dialog
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
            // DEBUG TO SEE WHICH NODE
//            view.getPrimaryStage().getScene().addEventFilter(MouseEvent.MOUSE_MOVED, e -> {
//                Node hovered = e.getPickResult().getIntersectedNode();
//                System.out.println("Mouse over: " + hovered);
//            });
            this.model.reset();
            this.view.reset();
            model.processFile(fileContent);
            model.setCurrentChrom(model.getRefChromosomes().get("<ALL>"));
            view.initReference(model.getRefChromosomes(), model.getRefTotalLength());
            view.drawReference(model.getRefChromosomes(), "<ALL>");
            model.setZoom(view.initZoomWG(model.getRefTotalLength()));
            view.showCoords(model.getCurrentChrom(), -1, model.getZoomLevel(), model.getRefChromosomes());
            view.updateMarkerWidth(model.getCurrentChrom(), model.getZoomLevel(), model.getCurrentChrom().getLength());
            view.updateMarkerPos(model.getCurrentChrom(), 0);
            view.initSidePane(model.getSamples(), model.getSampleColors(), model::getNumAnnotationsShown);
            view.initSamples(model.getSamples(), model.getRefTotalLength(), model.getZoomLevel(), model.getBaseFontSize(), model.getOriginalTrackHeight());
            view.showCalls(model.getRefChromosomes().get("<ALL>"), model.getSamples(), model.getZoomLevel(), model.getOriginalTrackHeight());
            view.enableControls();
//            view.viewportWidthChange(e -> {
//                this.processViewportWidthChange();
//            });
            view.scrollChange((obs,oldVal, newVal) -> {
               this.processScrollChange(newVal.doubleValue());
            });
            view.markerDragged(e -> {
                this.processMarkerDragged(e);
            });
        }
        // user closed or cancelled file
        else {
            System.out.println("File was not chosen.");
            // do nothing
        }
    }

    public void updateZoom(String text) {
        if (Objects.equals(model.getCurrentChrom().getName(), "<ALL>")) {
            // not meant to zoom in on <ALL> region so do nothing
        }
        else {
            double factor;
            if (Objects.equals(text, "+")) {
                factor = 1.5;
            }
            else if (Objects.equals(text, "-")) {
                factor = 0.5;
            }
            else {
                throw new IllegalArgumentException("Unexpected zoom button text " + text);
            }
            double oldProportion = model.getGenomicProportion(view.getViewportWidth(), model.getCurrentChrom(), model.getZoomLevel());
            double start = view.getStartFromHVal(view.getHValue(), model.getCurrentChrom());
            int intStart = (int) start;
            int centerStart = intStart + (int) oldProportion/2;
            int end = (int) (start + oldProportion);

            // update zoom level
            Pair<String, Double> result = model.updateZoomLevelByFactor(factor, model.getCurrentChrom(), view.getViewportWidth(), view.getVerticalSBWidth(), start, oldProportion);
            double level = result.y;
            String anchor = result.x;

            // update coord increment
            model.updateCoordIncrement(view.getViewportWidth(), model.getCurrentChrom());
            // show coords
            view.showCoords(model.getCurrentChrom(), model.getTickSpacing(), model.getZoomLevel(), model.getRefChromosomes());
            // show calls
            view.showCalls(model.getCurrentChrom(), view.getSampleOrderInView(), level, model.getOriginalTrackHeight());
            view.updateSelections(model.getSelections(), level);

            // update newStart based on result
            System.out.println("---------------ANCHOR IS " + anchor);
            double newProportion = model.getGenomicProportion(view.getViewportWidth(), model.getCurrentChrom(), model.getZoomLevel());
            int newStart = centerStart - (int) (newProportion/2);
            if (Objects.equals(anchor, "ABSOLUTE CENTER")) {
                newStart = 1;
            }
            else if (Objects.equals(anchor, "CENTER")) {
                newStart = centerStart - (int) (newProportion/2);
            }
            else if (Objects.equals(anchor, "LEFT")) {
                newStart = 1;
            }
            else if (Objects.equals(anchor, "RIGHT")) {
                newStart = model.getCurrentChrom().getLength() - (int) newProportion;
            }
            double offset = ((double) newStart / model.getCurrentChrom().getLength()) * view.getMarkerWrapperWidth();

            // update scroll and marker based on newStart and offset (calculated from newStart)
            view.setScroll(newStart, model.getCurrentChrom(), model.getZoomLevel());
            view.updateMarkerWidth(model.getCurrentChrom(), level, model.getGenomicProportion(view.getViewportWidth(), model.getCurrentChrom(), model.getZoomLevel()));
            view.updateMarkerPos(model.getCurrentChrom(), offset);
        }
    }

    public void clearSelections() {
        view.clearAllSelections();
        model.clearSelections();
    }

    public void processSelections() {
        view.showPlot(model.processHaplotypeSelections(view.getSampleOrderInView()));
    }

    public void processBlockSelections() {
        view.showPlot(model.processPinnedSelections(view.getSampleOrderInView(), view.getPinCheckboxes()));
    }

    public void updateReleaseSelection(MouseEvent e) {
        Selection selection = new Selection(view.getSelectionRectangle().getX(), e.getX(), model.getCurrentChrom().getName(), model.getZoomLevel());
        model.addSelection(selection);
        view.clearActiveSelection();
    }

    public void updateTrackHeight(double increment) {
        if (!model.isCallPanelHeightStored()) {
            model.setBaseCallPanelHeight(view.getBaseCallPanelHeight());
        }
        model.updateTrackHeightScale(increment);
        view.updateTrackHeight(model.getTrackHeightScale());
        view.redrawSampleInfoAfterScale(model.getSamples(), model.getBaseFontSize(), model.getTrackHeightScale(), model.getOriginalTrackHeight());
    }

    public void showChromosome(String selectedChrom) {
        // set new chromosome
        Chromosome chrom = model.getRefChromosomes().get(selectedChrom);
        model.setCurrentChrom(chrom);
        // update zoom level
        model.updateZoomLevelByRegion(chrom.getLength(), view.getViewportWidth(), view.isVerticalSBVisible(), view.getVerticalSBWidth());
        // update coord increment
        model.updateCoordIncrement(view.getViewportWidth(), model.getCurrentChrom());
        // show coords
        view.showCoords(model.getCurrentChrom(), model.getTickSpacing(), model.getZoomLevel(), model.getRefChromosomes());
        // show calls
        view.showCalls(chrom, view.getSampleOrderInView(), model.getZoomLevel(), model.getOriginalTrackHeight());
        view.drawReference(model.getRefChromosomes(), model.getCurrentChrom().getName());
        view.updateMarkerWidth(chrom, model.getZoomLevel(), chrom.getLength());
        view.updateMarkerPos(chrom, 0);
    }

//    public void processViewportWidthChange() {
//        System.out.println("PROCESSING VIEWPORT WIDTH CHANGE");
//        //view.updateMarkerOnViewportScaleOrZoom(model.getCurrentChrom(), model.getRefTotalLength(), model.getZoomLevel());
//    }

    public void processScrollChange(double newVal) {
        System.out.println("PROCESSING SCROLL CHANGE");
        System.out.println("PROCESSING SCROLL CHANGE CURRENT CHROM IS " + model.getCurrentChrom().getName());
        System.out.println("NEW VAL IN PROCESS SCROLL CHANGE " + newVal);
        view.syncScroll(newVal);
    }

    public void processMarkerDragged(MouseEvent e) {
        view.updateMarkerOnDrag(e);
    }

    public void processCustomRegion(String regionText) {
        System.out.println("PROCESSING REGION");
        // if region entered is empty, reset text field
        if (Objects.equals(regionText, "")) {
            view.clearRegionField();
            System.out.println("REGION IS EMPTY"); 
        }
        // otherwise
        else {
            // check if input is valid
            if (model.checkIfValidRegion(regionText)) {
                String regex = "(.+):(\\d+)-(\\d+)";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(regionText);
                if (matcher.find()) {
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
                    view.showCalls(chrom, view.getSampleOrderInView(), model.getZoomLevel(), model.getOriginalTrackHeight());
                    view.drawReference(model.getRefChromosomes(), chrom.getName());
                    double offset = ((double) start / chrom.getLength()) * view.getMarkerWrapperWidth();
                    view.setScroll(start, chrom, model.getZoomLevel());
                    view.updateMarkerWidth(chrom, model.getZoomLevel(), length);
                    view.updateMarkerPos(chrom, offset);
                }
                else {
                    view.showInvalidRegionAlert(regionText);
                }
            }
            else {
                view.showInvalidRegionAlert(regionText);
            }
        }
        System.out.println(regionText);
        view.clearRegionField();
    }
}
