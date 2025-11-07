package com.javafxapp;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

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
            view.toggleSidePane();
        });
        view.closeSidePaneListener(e -> {
            view.toggleSidePane();
        });
        view.chromComboBoxListener(e -> {
            String selectedChrom = view.chromComboBox.getValue();
            this.showRegion(selectedChrom);
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
            model.setCurrentRegion(model.getRefChromosomes().get("<ALL>"));
            view.initReference(model.getRefChromosomes(), model.getRefTotalLength());
            model.setZoom(view.initZoomWG(model.getRefTotalLength()));
            view.showCoords(model.getCurrentRegion(), -1, model.getZoomLevel(), model.getRefChromosomes());
            view.updateMarkerOnViewportScaleOrZoom(model.getCurrentRegion(), model.getRefTotalLength(), model.getZoomLevel());
            view.initSidePane(model.getSamples(), model.getSampleColors(), model::getNumAnnotationsShown);
            view.initSamples(model.getSamples(), model.getRefTotalLength(), model.getZoomLevel(), model.getBaseFontSize(), model.getOriginalTrackHeight());
            view.showCalls(model.getRefChromosomes().get("<ALL>"), model.getSamples(), model.getZoomLevel(), model.getOriginalTrackHeight());
            view.enableControls();
            view.viewportWidthChange(e -> {
                this.processViewportWidthChange();
            });
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
        if (Objects.equals(model.getCurrentRegion().getName(), "<ALL>")) {
            // not meant to zoom in on <ALL> region so do nothing
        }
        else {
            double factor;
            if (Objects.equals(text, "+")) {
                factor = 1.3;
            }
            else if (Objects.equals(text, "-")) {
                factor = 0.7;
            }
            else {
                throw new IllegalArgumentException("Unexpected zoom button text " + text);
            }
            // update zoom level
            double level = model.updateZoomLevelByFactor(factor, model.getCurrentRegion(), view.getViewportWidth(), view.getVerticalSBWidth());
            // update coord increment
            model.updateCoordIncrement(view.getViewportWidth(), model.getCurrentRegion());
            // show coords
            view.showCoords(model.getCurrentRegion(), model.getTickSpacing(), model.getZoomLevel(), model.getRefChromosomes());
            // show calls
            view.showCalls(model.getCurrentRegion(), model.getSamples(), level, model.getOriginalTrackHeight());
            view.updateSelections(model.getSelections(), level);
            view.updateMarkerOnViewportScaleOrZoom(model.getCurrentRegion(), model.getRefTotalLength(), level);
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
        Selection selection = new Selection(view.getSelectionRectangle().getX(), e.getX(), model.getZoomLevel());
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

    public void showRegion(String selectedChrom) {
        // set region
        Chromosome region = model.getRefChromosomes().get(selectedChrom);
        model.setCurrentRegion(region);
        // update zoom level
        model.updateZoomLevelByRegion(region, view.getViewportWidth(), view.isVerticalSBVisible(), view.getVerticalSBWidth());
        // update coord increment
        model.updateCoordIncrement(view.getViewportWidth(), model.getCurrentRegion());
        // show coords
        view.showCoords(model.getCurrentRegion(), model.getTickSpacing(), model.getZoomLevel(), model.getRefChromosomes());
        // show calls
        view.showCalls(region, view.getSampleOrderInView(), model.getZoomLevel(), model.getOriginalTrackHeight());
        view.updateMarkerOnViewportScaleOrZoom(region, model.getRefTotalLength(), model.getZoomLevel());
    }

    public void processViewportWidthChange() {
        view.updateMarkerOnViewportScaleOrZoom(model.getCurrentRegion(), model.getRefTotalLength(), model.getZoomLevel());
    }

    public void processScrollChange(double newVal) {
        view.syncScroll(newVal, model.getCurrentRegion());
    }

    public void processMarkerDragged(MouseEvent e) {
        view.updateMarkerOnDrag(e, model.getCurrentRegion());
    }
}
