package com.javafxapp;

import java.io.File;

import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;


public class Controller {
    private Model model;
    private View view;
    public Controller(Model model, View view) {
        this.model = model;
        this.view = view;
        view.importListener(e -> {
            this.model.reset();
            this.view.reset();
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
        view.releaseSelectionListener(e -> {
            this.updateReleaseSelection(e);
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
            model.processFile(fileContent);
            view.initReference(model.getRefLength(), model.getRefName());
            view.initSamples(model.getSamples(), model.getRefLength(), model.getZoomLevel());
            view.showCoords(model.getRefLength(), model.getZoomLevel());
            view.showCalls(model.getSamples(), model.getZoomLevel(), model.getRefLength());
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
        view.updateZoom(model.getSamples(), level, model.getRefLength(), model.getSelections(), model.getBaseLevel());
    }

    public void updateZoomOut() {
        double level = model.updateZoomLevel(0.7);
        view.updateZoom(model.getSamples(), level, model.getRefLength(), model.getSelections(), model.getBaseLevel());
    }

    public void clearSelections() {
        view.clearAllSelections();
        model.clearSelections();
    }

    public void processSelections() {
        view.showPlot(model.processSelections(), model.getSamples(), model.getZoomLevel());
    }

    public void updateReleaseSelection(MouseEvent e) {
        Selection selection = new Selection(view.getSelectionRectangle().getX(), e.getX(), model.getZoomLevel());
        model.addSelection(selection);
        view.clearActiveSelection();
    }
}

