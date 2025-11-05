package com.javafxapp;

import java.io.File;
import java.util.ArrayList;

import javafx.scene.Node;
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
            this.updateZoomIn();
        });
        view.zoomOutListener(e -> {
            this.updateZoomOut();
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
        view.viewportWidthChange(e -> {
            this.processViewportWidthChange();
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
            model.setZoom(view.initZoomAndCoordsWG(model.getCurrentRegion(), model.getRefTotalLength(), model.getTickSpacing()));
            view.initSidePane(model.getSamples(), model.getSampleColors(), model::getNumAnnotationsShown);
            view.initReference(model.getRefChromosomes(), model.getRefTotalLength());
            view.initSamples(model.getSamples(), model.getRefTotalLength(), model.getZoomLevel(), model.getBaseFontSize(), model.getOriginalTrackHeight());
            view.showCalls(model.getRefChromosomes().get("<ALL>"), model.getSamples(), model.getZoomLevel(), model.getOriginalTrackHeight());
            view.enableControls();
        }
        // user closed or cancelled file
        else {
            System.out.println("File was not chosen.");
            // do nothing
        }
    }

    public void updateZoomIn() {
        System.out.println("***************** IN *********************");
        double level = model.updateZoomLevelByFactor(1.3, model.getCurrentRegion(), view.getViewportWidth(), view.getVerticalSBWidth());
        model.updateCoordIncrement(view.getViewportWidth());
        view.updateZoom(model.getCurrentRegion(), model.getSamples(), level, model.getRefTotalLength(), model.getSelections(), model.getTickSpacing(), model.getOriginalTrackHeight());
    }

    public void updateZoomOut() {
        System.out.println("***************** OUT *********************");
        double level = model.updateZoomLevelByFactor(0.7, model.getCurrentRegion(), view.getViewportWidth(), view.getVerticalSBWidth());
        model.updateCoordIncrement(view.getViewportWidth());
        view.updateZoom(model.getCurrentRegion(), model.getSamples(), level, model.getRefTotalLength(), model.getSelections(), model.getTickSpacing(), model.getOriginalTrackHeight());
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
        Chromosome region = model.getRefChromosomes().get(selectedChrom);
        model.setCurrentRegion(region);
        System.out.println("CURRENT REGION IS " + model.getCurrentRegion().getName());
        model.updateZoomLevelByRegion(region, view.getViewportWidth());
        view.showRegion(region, model.getZoomLevel(), model.getRefTotalLength(), model.getOriginalTrackHeight());
    }

    public void processViewportWidthChange() {
        view.updateMarkerOnViewportScaleOrZoom(model.getCurrentRegion(), model.getRefTotalLength(), model.getZoomLevel());
    }
}
