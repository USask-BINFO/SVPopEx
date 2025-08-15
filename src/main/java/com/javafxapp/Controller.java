package com.javafxapp;

import java.io.File;
import java.util.ArrayList;

import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;


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
        view.openSidePaneListener(e -> {
            view.toggleSidePane();
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
            view.showConfigPopup(model.getSamples());
            view.initReference(model.getRefLength(), model.getRefName());
            view.initSamples(model.getSampleOrderInView(), model.getRefLength(), model.getZoomLevel(), model.getBaseFontSize(), model.getOriginalTrackHeight());
            view.showCoords(model.getRefLength(), model.getZoomLevel(), model.getTickSpacing());
            view.showCalls(model.getSampleOrderInView(), model.getZoomLevel(), model.getRefLength(), model.getOriginalTrackHeight());
            view.enableControls();
        }
        // user closed or cancelled file
        else {
            System.out.println("File was not chosen.");
            // do nothing
        }
    }

    public void updateZoomIn() {
        double level = model.updateZoomLevel(1.3);
        model.updateCoordIncrement(view.getViewportWidth());
        view.updateZoom(model.getSampleOrderInView(), level, model.getRefLength(), model.getSelections(), model.getBaseLevel(), model.getTickSpacing(), model.getOriginalTrackHeight());
    }

    public void updateZoomOut() {
        double level = model.updateZoomLevel(0.7);
        model.updateCoordIncrement(view.getViewportWidth());
        view.updateZoom(model.getSampleOrderInView(), level, model.getRefLength(), model.getSelections(), model.getBaseLevel(), model.getTickSpacing(), model.getOriginalTrackHeight());
    }

    public void clearSelections() {
        view.clearAllSelections();
        model.clearSelections();
    }

    public void processSelections() {
        view.showPlot(model.processSelections(), model.getSampleOrderInView(), model.getZoomLevel());
    }

    public void processBlockSelections() {
        if (model.getComparators() == null) {
            model.processConfig(this.view.showConfigPopup(model.getSamples()));
        }
        model.processBlockSelections();
        view.showSampleColorStrip(model.getComparators(), model.getSampleOrderInView(), model.getSampleColors());
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
        view.redrawSampleInfoAfterScale(model.getSampleOrderInView(), model.getBaseFontSize(), model.getTrackHeightScale(), model.getOriginalTrackHeight());
    }

    public void processViewportWidthChange() {
        view.updateMarkerOnViewportScaleOrZoom(model.getRefLength(), model.getZoomLevel());
    }
}

