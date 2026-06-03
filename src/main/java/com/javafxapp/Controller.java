package com.javafxapp;

import java.io.File;
import java.text.DecimalFormat;
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
            String selectedChrom = view.chromComboBox.getValue();
            this.showChromosome(selectedChrom);
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
            System.out.println("PROCESSING FILE...");
            model.processFile(fileContent);
            System.out.println("INIT AND DRAW REFERENCE...");
            model.setCurrentChrom(model.getRefChromosomes().get("<ALL>"));
            view.initReference(model.getRefChromosomes(), model.getRefTotalLength());
            view.drawReference(model.getRefChromosomes(), "<ALL>");
            System.out.println("SETTING ZOOM AND SHOWING MARKER AND COORDS...");
            model.setZoom(view.initZoomWG(model.getRefTotalLength()));
            view.showCoords(model.getCurrentChrom(), -1, model.getZoomLevel(), model.getRefChromosomes());
            view.updateMarker(model.getCurrentChrom(), model.getZoomLevel(), model.getCurrentChrom().getLength(), 0);
            System.out.println("SHOW SIDE PANE...");
            view.initSidePane(model.getSamples(), model.getSampleColors());
            System.out.println("SHOW SAMPLES....");
            view.initSamples(model.getSamples(), model.getSampleColors(), model.getRefTotalLength(), model.getZoomLevel(), model.getBaseFontSize(), model.getOriginalTrackHeight(), model.getAFTrackHeight());

            System.out.println("ENABLE CONTROLS...");
            view.enableControls();
            // triggers showChromosome()
            view.setChromComboBoxValue("<ALL>");
            this.updateTrackHeight(model.fitAllSamplesIncrement(view.getCallsPanelHeight(), view.getHorizontalSBHeight()));
            //            view.viewportWidthChange(e -> {
//                this.processViewportWidthChange();
//            });
            view.browserDragged((obs,oldVal, newVal) -> {
                if (view.isDragging()) {
                    this.processScrollChange(oldVal.doubleValue(), newVal.doubleValue());
                }
                else {
                    System.out.println("CALLED BROWSER DRAGGED (HVALUE CHANGED) BUT NOT DRAGGING");
                }
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
        System.out.println("----- TRIGGERING CONTROLLER.UPDATEZOOM() ----------");
        if (Objects.equals(model.getCurrentChrom().getName(), "<ALL>")) {
            // not meant to zoom in on <ALL> region so do nothing
        }
        else {
            double factor;
            if (Objects.equals(text, "Zoom +")) {
                factor = 1.5;
            }
            else if (Objects.equals(text, "Zoom -")) {
                factor = 0.5;
            }
            else {
                throw new IllegalArgumentException("Unexpected zoom button text " + text);
            }
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

            view.updateSelections(model.getSelections(), level);

            // update newStart based on result
            System.out.println("---------------ANCHOR IS " + anchor);
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
            System.out.println("UPDATED ZOOM SHOWING START: " + newStart + " and END : " + newEnd);
            view.showTileCalls(model.getCurrentChrom(), view.getSampleOrderInView(), level, model.getOriginalTrackHeight(), model.getStartInterval((int) newStart), model.getEndInterval((int) newEnd));
            view.showTileAlleleFreq(model.getCurrentChrom(), level, model.getAFTrackHeight(), model.getStartInterval((int) newStart), model.getEndInterval((int) newEnd));
            double offset = ((double) newStart / model.getCurrentChrom().getLength()) * view.getMarkerWrapperWidth();
            // update scroll and marker based on newStart and offset (calculated from newStart)
            view.syncScroll(view.setScroll((int) newStart, model.getCurrentChrom(), model.getZoomLevel()));
            view.setScroll((int) newStart, model.getCurrentChrom(), model.getZoomLevel());
            view.updateMarker(model.getCurrentChrom(), level, model.getGenomicProportion(view.getViewportWidth(), model.getCurrentChrom(), model.getZoomLevel()), offset);
        }
    }

    public void clearSelections() {
        view.clearAllSelections();
        model.clearSelections();
    }

    public void processSelections() {
        view.showPlot(model.processHaplotypeSelections(view.getSampleOrderInView()));
    }

    public void processShowSameCalls() {
        // want to show the same calls, so we are going to hide the different ones
        view.hideCalls(view.getSampleOrderInView(), model.getDiffCallsFromPinned(view.getPinCheckboxes()));
    }

    public void processShowDiffCalls() {
        view.hideCalls(view.getSampleOrderInView(), model.getSameCallsFromPinned(view.getPinCheckboxes()));
    }

    public void updateReleaseSelection(MouseEvent e) {
        Selection selection = new Selection(view.getSelectionRectangle().getX(), e.getX(), model.getCurrentChrom().getName(), model.getZoomLevel());
        model.addSelection(selection);
        view.clearActiveSelection();
    }

    public void updateTrackHeight(double increment) {
        model.updateTrackHeightScale(increment);
        view.updateTrackHeight(model.getSamples(), model.getTrackHeightScale());
        view.redrawSampleInfoAfterScale(model.getSamples(), model.getBaseFontSize(), model.getTrackHeightScale(), model.getOriginalTrackHeight());
    }

    public void showChromosome(String selectedChrom) {
        System.out.println("-------- TRIGGERING CONTROLLER.SHOWCHROMOSOME() ------------");
        // set new chromosome
        Chromosome chrom = model.getRefChromosomes().get(selectedChrom);
        model.setCurrentChrom(chrom);
        // update zoom level
        System.out.println("TRIGGER UPDATE ZOOM");
        model.updateZoomLevelByRegion(chrom.getLength(), view.getViewportWidth(), view.isVerticalSBVisible(), view.getVerticalSBWidth());
        System.out.println("DONE UPDATE ZOOM");
        // update coord increment
        model.updateCoordIncrement(view.getViewportWidth(), model.getCurrentChrom());
        // show coords
        view.syncScroll(0);
        view.showCoords(model.getCurrentChrom(), model.getTickSpacing(), model.getZoomLevel(), model.getRefChromosomes());
        // show calls
        if (Objects.equals(selectedChrom, "<ALL>")) {
            view.showChromosomeCalls(chrom, view.getSampleOrderInView(), model.getZoomLevel(), model.getOriginalTrackHeight());
        }
        else {
            view.showTileCalls(chrom, view.getSampleOrderInView(), model.getZoomLevel(), model.getOriginalTrackHeight(), model.getStartInterval(1), model.getEndInterval((int) chrom.getLength()));
        }
        view.showChromosomeAlleleFreq(chrom, model.getZoomLevel(), model.getAFTrackHeight());
        view.drawReference(model.getRefChromosomes(), model.getCurrentChrom().getName());
        view.updateMarker(chrom, model.getZoomLevel(), chrom.getLength(), 0);
    }

//    public void processViewportWidthChange() {
//        System.out.println("PROCESSING VIEWPORT WIDTH CHANGE");
//        //view.updateMarkerOnViewportScaleOrZoom(model.getCurrentChrom(), model.getRefTotalLength(), model.getZoomLevel());
//    }

    public String processShowMate() {
        long length = (int) model.getGenomicProportion(view.getViewportWidth(), model.getCurrentChrom(), model.getZoomLevel());
        String alternate = view.getLiveCall().getAlternate();
        Pattern pattern = Pattern.compile("[\\[\\]](.+)[\\[\\]]");
        Matcher altInfo = pattern.matcher(alternate);
        if (altInfo.find()) {
            Pattern regionPattern = Pattern.compile("(.+):(.+)");
            Matcher region = regionPattern.matcher(altInfo.group(1));
            if (region.find()) {
                String chrom = region.group(1);
                long coords = Integer.parseInt(region.group(2));
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

                return chrom + ":" + start + "-" + end;
            }
        }
        return "null";
    }

    public void processScrollChange(double oldVal, double newVal) {
        view.syncScroll(newVal);
        System.out.println(" --------- TRIGGER CONTROLLER.PROCESSSCROLLCHANGE() --------------");
        DecimalFormat df = new DecimalFormat("#,##0.################");
        double proportion = model.getGenomicProportion(view.getViewportWidth(), model.getCurrentChrom(), model.getZoomLevel());
        double start = view.getStartFromHVal(newVal, model.getCurrentChrom(), model.getZoomLevel());
        double end = start + proportion;
        System.out.println(model.updateCurrentTileStart(model.getStartInterval((int) start)));
        if (model.updateCurrentTileStart(model.getStartInterval((int) start)) || model.updateCurrentTileEnd(model.getEndInterval((int) end))) {
            view.showTileCalls(model.getCurrentChrom(), view.getSampleOrderInView(), model.getZoomLevel(), model.getOriginalTrackHeight(), model.getStartInterval((int) start), model.getEndInterval((int) end));
            view.showTileAlleleFreq(model.getCurrentChrom(), model.getZoomLevel(), model.getAFTrackHeight(), model.getStartInterval((int) start), model.getEndInterval((int) end));
        }
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
                    view.showTileCalls(chrom, view.getSampleOrderInView(), model.getZoomLevel(), model.getOriginalTrackHeight(), model.getStartInterval((int) start), model.getEndInterval((int) end));
                    view.showTileAlleleFreq(chrom, model.getZoomLevel(), model.getAFTrackHeight(), model.getStartInterval((int) start), model.getEndInterval((int) end));
                    view.drawReference(model.getRefChromosomes(), chrom.getName());
                    double offset = ((double) start / chrom.getLength()) * view.getMarkerWrapperWidth();
                    view.setScroll(start, chrom, model.getZoomLevel());
                    view.updateMarker(chrom, model.getZoomLevel(), length, offset);
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
