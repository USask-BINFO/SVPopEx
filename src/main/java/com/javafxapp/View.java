package com.javafxapp;

import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;

// naming conventions
// Stage -> ...Stage
// HBox/VBox -> ...Container
// MenuBar -> ...Bar
// Menu -> ...Menu
// MenuItem -> ...Item
// ScrollPane/StackPane -> ...Panel
// Button -> ...Button
// Pane -> ...Wrapper
// EventHandler -> ...Handler
class Delta {
    double x;
    double y;
}
public class View {
    // ----------- root and side pane ---------------
    private Delta dragCoords = new Delta();
    VBox sidePane = new VBox(20);
    Separator separator = new Separator();
    Button closeSidePaneButton = new Button("\u00D7");
    HBox closeButtonContainer = new HBox(closeSidePaneButton);
    private final VBox layout = new VBox();
    StackPane root = new StackPane(layout, sidePane);
    private final Stage primaryStage;
    TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), sidePane);
    TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), sidePane);
    // ---------- menuBar -------------
    private final MenuBar menuBar = new MenuBar();
    private final Menu fileMenu = new Menu("File");
    private final MenuItem importVCFItem = new MenuItem("Import VCF");
    // ------- referenceContainer ----------
    private final VBox referenceContainer = new VBox(5);
    private Rectangle marker = new Rectangle(0,0,0,50);
    private final Pane referenceRect = new Pane();
    private final Pane markerWrapper = new Pane();
    private Label l1 = new Label("");
    private Label l2 = new Label("");
    VBox labelsBox = new VBox(10, l1, l2);
    StackPane rectangleWithLabels = new StackPane(referenceRect, labelsBox);
    StackPane rectWithMarker = new StackPane(rectangleWithLabels, markerWrapper);
    // ---------- tickContainer ----------
    private final HBox tickContainer = new HBox();
    private final Pane spaceWrapper1 = new Pane();
    private final Pane ticksWrapper = new Pane();
    private EventHandler<MouseEvent> releaseSelectionHandler;
    // ----------- controlContainer ---------
    private final HBox controlContainer = new HBox();
    Button zoomInButton = new Button("+");
    Button zoomOutButton = new Button("-");
    Button clearButton = new Button("Clear");
    Button processButton = new Button("Process");
    Button processBlocksButton = new Button("Process Blocks");
    Button shrinkTrackHeightButton = new Button("- Track");
    Button growTrackHeightButton = new Button("+ Track");
    Button sidePaneButton = new Button("Selection Options");
    // ---------- callsPanel ----------
    Tooltip callInfoTooltip = new Tooltip();
    private final HBox callsContentContainer = new HBox();
    private final VBox samplesInfoContainer = new VBox();
    private final VBox samplesContainer = new VBox(0);
    private final HBox selectionContainer = new HBox();
    private final Pane selectionWrapper = new Pane();
    private final StackPane samplePanel = new StackPane(samplesContainer, selectionContainer);
    private final ScrollPane callsPanel = new ScrollPane(samplePanel);


    public View(Stage primaryStage) {
        // ---------- ROOT AND SIDE PANE ---------
        this.primaryStage = primaryStage;
        root.setAlignment(sidePane, Pos.CENTER_RIGHT);
        sidePane.setMinWidth(300);
        sidePane.setMaxWidth(300);
        sidePane.setStyle("-fx-background-color: #d8d5cf;");
        sidePane.setTranslateX(300);
        sidePane.setAlignment(Pos.TOP_CENTER);
        // close button
        sidePane.getChildren().add(closeButtonContainer);
        closeButtonContainer.setAlignment(Pos.TOP_RIGHT);
        // title
        Label title = new Label("Selection Options");
        title.setStyle("-fx-font-size: 24px;");
        sidePane.getChildren().add(title);
        // process button
        sidePane.getChildren().add(processButton);
        sidePane.getChildren().add(separator);


        // ---------- MENU --------------
        fileMenu.getItems().add(importVCFItem);
        menuBar.getMenus().add(fileMenu);
        // ---------- CONTROL PANEL -----
        // initially set buttons to disabled until file is loaded
        zoomInButton.setDisable(true);
        zoomOutButton.setDisable(true);
        processButton.setDisable(true);
        processBlocksButton.setDisable(true);
        clearButton.setDisable(true);
        shrinkTrackHeightButton.setDisable(true);
        growTrackHeightButton.setDisable(true);
        sidePaneButton.setDisable(true);
        closeSidePaneButton.setDisable(true);
        zoomInButton.setMinSize(35, 35);
        zoomInButton.setMaxSize(35, 35);
        zoomOutButton.setMinSize(35, 35);
        zoomOutButton.setMaxSize(35, 35);
        clearButton.setMinSize(50,35);
        processButton.setMinSize(50,35);
        processBlocksButton.setMinSize(75,35);
        shrinkTrackHeightButton.setMinSize(75,35);
        shrinkTrackHeightButton.setMaxSize(75,35);
        growTrackHeightButton.setMinSize(80,35);
        growTrackHeightButton.setMaxSize(80,35);
        sidePaneButton.setMinSize(190,35);
        sidePaneButton.setMaxSize(190,35);
        closeSidePaneButton.setMinSize(25,25);
        closeSidePaneButton.setMaxSize(25,25);
        String circularStyle = """
    -fx-background-radius: 10px;
    -fx-border-radius: 10px;
    -fx-text-fill: #555555;
    -fx-font-size: 16px;
    -fx-font-weight: bold;
    -fx-cursor: hand;
    -fx-focus-color: transparent;
    -fx-faint-focus-color: transparent;
""";
        // button focus off
        zoomInButton.setFocusTraversable(false);
        zoomOutButton.setFocusTraversable(false);
        clearButton.setFocusTraversable(false);
        processButton.setFocusTraversable(false);
        processBlocksButton.setFocusTraversable(false);
        shrinkTrackHeightButton.setFocusTraversable(false);
        growTrackHeightButton.setFocusTraversable(false);
        sidePaneButton.setFocusTraversable(false);
        closeSidePaneButton.setFocusTraversable(false);
        // button style
        zoomInButton.setStyle(circularStyle);
        zoomOutButton.setStyle(circularStyle);
        clearButton.setStyle(circularStyle);
        processButton.setStyle(circularStyle);
        processBlocksButton.setStyle(circularStyle);
        shrinkTrackHeightButton.setStyle(circularStyle);
        growTrackHeightButton.setStyle(circularStyle);
        sidePaneButton.setStyle(circularStyle);
        closeSidePaneButton.setStyle(circularStyle);
        closeSidePaneButton.setStyle("-fx-font-size: 12px;");
        // add button
        controlContainer.getChildren().add(zoomInButton);
        controlContainer.getChildren().add(zoomOutButton);
        controlContainer.getChildren().add(clearButton);
        controlContainer.getChildren().add(shrinkTrackHeightButton);
        controlContainer.getChildren().add(growTrackHeightButton);
        controlContainer.getChildren().add(sidePaneButton);
        // ---------- REF PANEL ---------
        referenceContainer.setStyle("-fx-background-color: white;");
        layout.getChildren().addAll(menuBar, controlContainer, referenceContainer, tickContainer, callsContentContainer);
        // reference rectangle
        this.referenceRect.setPrefHeight(50);
        referenceRect.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
        referenceRect.setBorder(new Border(new BorderStroke(
                Color.BLACK,
                BorderStrokeStyle.SOLID,
                CornerRadii.EMPTY,
                new BorderWidths(1)
        )));
        labelsBox.setStyle("-fx-alignment: center;");
        this.referenceContainer.getChildren().add(rectWithMarker);
        // marker
        markerWrapper.getChildren().add(marker);
        marker.setFill(Color.ORANGERED);
        marker.setOpacity(0.5);
        marker.setOnMouseDragged(event -> updateHighLevelView(event));
        // ---------- TICK PANEL ---------
        spaceWrapper1.setMinWidth(90);
        spaceWrapper1.setPrefWidth(90);
        spaceWrapper1.setMaxWidth(90);
        tickContainer.getChildren().add(spaceWrapper1);
        tickContainer.getChildren().add(ticksWrapper);
        ticksWrapper.setOnMouseEntered(e -> ticksWrapper.setCursor(Cursor.HAND));
        ticksWrapper.setOnMouseExited(e -> ticksWrapper.setCursor(Cursor.DEFAULT));
        this.ticksWrapper.setOnMousePressed(e -> {
            this.ticksPressed(e);
        });
        this.ticksWrapper.setOnMouseDragged(e -> {
            this.ticksDragged(e);
        });
        this.ticksWrapper.setOnMouseReleased(e -> {
            this.ticksReleased(e);
        });
        // ---------- CALLS PANEL -------
        this.callsContentContainer.getChildren().add(samplesInfoContainer);
        this.callsContentContainer.getChildren().add(callsPanel);
        this.callsPanel.setStyle("-fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
        selectionWrapper.setPickOnBounds(false);
        this.callsPanel.hvalueProperty().addListener((obs, oldVal, newVal) -> {
            this.syncScroll(newVal);
        });
        this.selectionContainer.getChildren().add(selectionWrapper);
    }

    public void enableControls() {
        this.zoomInButton.setDisable(false);
        this.zoomOutButton.setDisable(false);
        this.processButton.setDisable(false);
        this.processBlocksButton.setDisable(false);
        this.clearButton.setDisable(false);
        this.shrinkTrackHeightButton.setDisable(false);
        this.growTrackHeightButton.setDisable(false);
        this.sidePaneButton.setDisable(false);
        this.closeSidePaneButton.setDisable(false);
    }

    // sync scrollpane scroll with marker and coordinate ticks
    public void syncScroll(Number newVal) {
        // oldVal = old scroll position (between 0 and 1)
        // newVal = new scroll position (between 0 and 1)
        // getContent() gets node scrollpane is scrolling, getboundsinlocal gets actual width and height of node, so maxX is the maximum distance that can be scrolled
        double maxX = callsPanel.getContent().getBoundsInLocal().getWidth()
                - callsPanel.getViewportBounds().getWidth();
        double translateX = -newVal.doubleValue() * maxX;
        ticksWrapper.setTranslateX(translateX);
        double max = markerWrapper.getWidth() - marker.getWidth();
        double scrollX = newVal.doubleValue() * max;
        marker.setLayoutX(scrollX);
    }

    public void ticksPressed(MouseEvent e) {
        double startX = e.getX();
        Rectangle tickRect = new Rectangle(startX, 0, 0, ticksWrapper.getHeight());
        tickRect.setId("selectionRect");
        tickRect.setVisible(true);
        tickRect.setFill(Color.GRAY);
        tickRect.setOpacity(0.3);
        ticksWrapper.getChildren().add(tickRect);
        Rectangle selectRect = new Rectangle(startX, 0, 0, selectionWrapper.getHeight());
        selectRect.setId("selectionRect");
        selectRect.setVisible(true);
        selectRect.setFill(Color.GRAY);
        selectRect.setOpacity(0.3);
        selectionWrapper.getChildren().add(selectRect);
    }

    public void ticksDragged(MouseEvent e) {
        Rectangle tickRect = (Rectangle) this.ticksWrapper.lookup("#selectionRect");
        Rectangle selectRect = (Rectangle) this.selectionWrapper.lookup("#selectionRect");
        double width = e.getX() - tickRect.getX();
        // only update selection in forward direction
        if (width > 0) {
            tickRect.setWidth(width);
            selectRect.setWidth(width);
        } else {
            // do nothing!
        }
    }

    public void ticksReleased(MouseEvent e) {
        Rectangle tickRect = (Rectangle) this.ticksWrapper.lookup("#selectionRect");
        Rectangle selectRect = (Rectangle) this.selectionWrapper.lookup("#selectionRect");
        double width = e.getX() - tickRect.getX();
        // only update selection in forward direction
        if (width > 0) {
            // update width
            tickRect.setWidth(width);
            selectRect.setWidth(width);
            // CHANGE : this might not be best place to have this/handle this under the width > 0
            if (this.releaseSelectionHandler != null) {
                releaseSelectionHandler.handle(e);
            }
        } else {
            // this rectangle isn't displayed, so remove
            this.ticksWrapper.getChildren().remove(tickRect);
            this.selectionWrapper.getChildren().remove(selectRect);
        }
    }

    public void reset() {
        this.ticksWrapper.getChildren().clear();
        this.selectionWrapper.getChildren().clear();
        this.samplesContainer.getChildren().clear();
        this.zoomInButton.setDisable(true);
        this.zoomOutButton.setDisable(true);
        this.processButton.setDisable(true);
        this.processBlocksButton.setDisable(true);
        this.clearButton.setDisable(true);
        this.shrinkTrackHeightButton.setDisable(true);
        this.growTrackHeightButton.setDisable(true);
        this.sidePaneButton.setDisable(true);
        this.closeSidePaneButton.setDisable(true);
    }

    public Scene getScene() {
        return new Scene(root);
    }

    public Stage getPrimaryStage() {
        return this.primaryStage;
    }

    public void initSidePane(ArrayList<Sample> samples, HashMap<String, Color> sampleColors) {
        ArrayList<CheckBox> checkboxes = new ArrayList<>();
        HashMap<String,Boolean> result = new HashMap<>();
        VBox comparators = new VBox();
        comparators.setPadding(new Insets(0, 0, 0, 20));
        comparators.getChildren().add(new Label("Pin and Highlight:"));
        int index = 0;
        for (Sample sample : samples) {
            Label label = new Label(sample.getName());
            CheckBox checkBox = new CheckBox();
            HBox hBox = new HBox(10);
            hBox.getChildren().addAll(label, checkBox);
            checkboxes.add(checkBox);
            comparators.getChildren().add(hBox);
            int finalIndex = index;
            checkBox.setOnAction(event -> {
                if (checkBox.isSelected()) {
                    sendSampleToTop(sample, checkboxes);
                    toggleSampleColorStrip(sample, sampleColors);
                }
                else {
                    sendSampleToOGLocation(sample, finalIndex, checkboxes);
                    toggleSampleColorStrip(sample, sampleColors);

                }
            });
            index++;
        }
        sidePane.getChildren().add(comparators);
        // process blocks button
        sidePane.getChildren().add(processBlocksButton);
//            if (buttonResult.isPresent() && buttonResult.get() == ButtonType.OK) {
//                int index = 0;
//                for (CheckBox checkbox : checkboxes) {
//                    result.put(samples.get(index).getName(),checkbox.isSelected());
//                    index++;
//                }
//            }
//            else {
//                return null;
//            }
//            return result;
    }

    public void initReference(int refLength, String refName) {
        this.l1.setText(refName);
        this.l2.setText(String.valueOf(refLength) + " bp");
    }

    public void initSamples(ArrayList<Sample> samples, int refLength, double zoomLevel, double baseFontSize, int originalTrackHeight) {
        for (Sample sample : samples) {
            // create callsWrapper to hold sample calls
            Pane callsWrapper = new Pane();
            callsWrapper.setMinWidth(refLength * zoomLevel);
            callsWrapper.setPrefWidth(refLength * zoomLevel);
            callsWrapper.setMaxWidth(refLength * zoomLevel);
            // create dragWrapper to hold drag lines
            StackPane dragWrapper = new StackPane();
            dragWrapper.setMinWidth(20);
            dragWrapper.setMaxWidth(20);
            // create drag lines
            VBox lines = new VBox(2); // 5 is spacing between lines
            lines.setAlignment(Pos.CENTER);
            for (int l=0; l<3; l++) {
                Line line = new Line(0, 0, 5, 0); // x1, y1, x2, y2
                line.setStrokeWidth(1);
                line.setStroke(Color.web("#888888")); // visible stroke color
                lines.getChildren().add(line);
            }
            dragWrapper.getChildren().add(lines);
            // set changes on drag
            dragWrapper.setOnMouseEntered(e -> dragWrapper.setCursor(Cursor.HAND));
            dragWrapper.setOnMouseExited(e -> dragWrapper.setCursor(Cursor.DEFAULT));
            // create labelWrapper Pane to hold sample name
            StackPane labelWrapper = new StackPane();
            labelWrapper.setMinWidth(70);
            labelWrapper.setMaxWidth(70);
            // create sample label
            Label sampleLabel = new Label(sample.getName());
            sampleLabel.setFont(Font.font("System", baseFontSize));
            // create infoContainer to hold sample info
            HBox labelContainer = new HBox();
            // create visualContainer to hold sample info and color rectangle
            VBox infoContainer = new VBox();
            infoContainer.setMinHeight(originalTrackHeight);
            infoContainer.setMaxHeight(originalTrackHeight);
            Rectangle colorRect = new Rectangle(40, 3, Color.TRANSPARENT);
            infoContainer.getChildren().addAll(labelContainer, colorRect);
            infoContainer.setAlignment(Pos.CENTER);
            // add drag lines, and sample label to infoContainer
            labelWrapper.getChildren().add(sampleLabel);
            labelContainer.getChildren().add(dragWrapper);
            labelContainer.getChildren().add(labelWrapper);
            this.samplesInfoContainer.getChildren().add(infoContainer);
            // add sampContainer to samplesContainer
            this.samplesContainer.getChildren().add(callsWrapper);
            // set IDs
            infoContainer.setId(sample.getName());
            callsWrapper.setId(sample.getName());
        }
        this.callsPanel.setPannable(true);   // Optional: enables mouse drag scrolling
    }

    public void showCalls(ArrayList<Sample> samples, double zoomLevel, int refLength, int originalTrackHeight) {
        /**
         * Pre-conditions/assumptions: Gets call pane for each sample by looking up the ID
         */
        // loops through each sample and gets the sample pane to update, access order does not matter
        for (Sample sample : samples) {
            Pane currentCalls = (Pane) this.samplesContainer.lookup("#" + sample.getName());
            currentCalls.getChildren().clear();
            currentCalls.setMinWidth(refLength * zoomLevel);
            currentCalls.setPrefWidth(refLength * zoomLevel);
            currentCalls.setMaxWidth(refLength * zoomLevel);
            // loop through each call
            for (int j=0; j<sample.calls.size(); j++) {
                // get the current Call and set its id for the call and rectangle
                Call currentCall = sample.calls.get(j);
                Rectangle callRect = new Rectangle(currentCall.getStart()*zoomLevel, 0, currentCall.getLength()*zoomLevel, originalTrackHeight);
                String callId = sample.getName() + "-" + j;
                currentCall.setCallRectId(callId);
                callRect.setId(callId);
                // styling
                callRect.setOpacity(1);
                callRect.setStrokeWidth(2);
                callRect.setArcWidth(5);   // horizontal roundness
                callRect.setArcHeight(5);
                if (Objects.equals(currentCall.getType(), "DUP")) {
                    callRect.setStroke(Color.rgb(40, 70, 160));
                    callRect.setOpacity(0.5);
                    callRect.setFill(Color.rgb(65, 105, 225));
                }
                else if (Objects.equals(currentCall.getType(), "INV")) {
                    callRect.setStroke(Color.rgb(200, 140, 0));
                    callRect.setOpacity(0.5);
                    callRect.setFill(Color.rgb(255, 195, 0 ));
                }
                else if (Objects.equals(currentCall.getType(), "DEL")) {
                    callRect.setStroke(Color.rgb(120, 30, 2));
                    callRect.setOpacity(0.5);
                    callRect.setFill(Color.rgb(164, 42, 4));
                }
                else if (Objects.equals(currentCall.getType(), "INS")) {
                    callRect.setStroke(Color.rgb(100, 140, 80));
                    callRect.setOpacity(0.5);
                    callRect.setFill(Color.rgb(147, 197, 114));
                }
                else {
                    System.out.println(currentCall.getType());
                    callRect.setStroke(Color.BLACK);
                    callRect.setOpacity(0.7);
                    callRect.setFill(Color.BLACK);
                }
                currentCalls.getChildren().add(callRect);
            }
        }
    }

    public void showCoords(int refLength, double zoomLevel, int tickSpacing) {
        this.ticksWrapper.getChildren().clear();
        for (int x = 0; x <= refLength; x += tickSpacing) {
            // coordinate
            Text text = new Text(String.valueOf(x));
            double textWidth = text.getLayoutBounds().getWidth();
            text.setX((x*zoomLevel) - textWidth / 2);
            text.setY(20);
            // tick
            Line tick = new Line(x*zoomLevel, 0, x*zoomLevel, 5);
            // add to pane
            this.ticksWrapper.getChildren().add(tick);
            this.ticksWrapper.getChildren().add(text);
        }
    }

    public void updateMarkerOnViewportScaleOrZoom(int refLength, double zoomLevel) {
        // set width
        double contentWidth = refLength * zoomLevel;
        callsPanel.layout();
        double viewportWidth = callsPanel.getViewportBounds().getWidth();
        double proportionVisible = viewportWidth / contentWidth;
        double markerWidth = markerWrapper.getWidth() * proportionVisible;
        marker.setWidth(markerWidth);
    }

    public double getBaseCallPanelHeight() {
        return this.callsPanel.getLayoutBounds().getHeight();
    }

    public void updateTrackHeight(double val) {
        Scale scale = new Scale();
        // pivot at top edge, so it grows and shrinks from top
        scale.setPivotY(0);
        scale.setY(val);
        // remove old transforms
        this.callsPanel.getTransforms().clear();
        this.callsPanel.getTransforms().add(scale);
    }

    public void redrawSampleInfoAfterScale(ArrayList<Sample> samples, double baseFontSize, double trackHeightScale, int originalTrackHeight) {
        /**
         * Pre-conditions/assumptions: Gets info pane for each sample by looking up the ID
         */
        for (Sample sample : samples) {
            VBox container = (VBox) samplesInfoContainer.lookup("#" + sample.getName());
            container.setMinHeight(originalTrackHeight * trackHeightScale);
            container.setMaxHeight(originalTrackHeight * trackHeightScale);
            HBox infoContainer = (HBox) container.getChildren().get(0);
            Pane labelWrapper = (Pane) infoContainer.getChildren().get(1);
            Label sampleLabel = (Label) labelWrapper.getChildren().getFirst();
            sampleLabel.setFont(Font.font(sampleLabel.getFont().getFamily(), baseFontSize));
        }
    }

    public double getViewportWidth() {
        return callsPanel.getViewportBounds().getWidth();
    }

    public Rectangle getSelectionRectangle() {
        return (Rectangle) this.ticksWrapper.lookup("#selectionRect");
    }

    public void clearActiveSelection() {
        Rectangle tickRect = (Rectangle) this.ticksWrapper.lookup("#selectionRect");
        Rectangle selectRect = (Rectangle) this.selectionWrapper.lookup("#selectionRect");
        tickRect.setId(null);
        selectRect.setId(null);
    }

    public void clearAllSelections() {
        this.selectionWrapper.getChildren().clear();
        this.ticksWrapper.getChildren().removeIf(node -> node instanceof Rectangle);
    }

    public void updateSelections(ArrayList<Selection> selections, double zoomLevel, double baseLevel) {
        this.selectionWrapper.getChildren().clear();
        this.ticksWrapper.getChildren().removeIf(node -> node instanceof Rectangle);
        // add back each selection considering the zoom
        for (int i=0; i<selections.size(); i++) {
            double start = (selections.get(i).getStart() * zoomLevel) / selections.get(i).getZoomLevel();
            double length = (selections.get(i).getLength() * zoomLevel) / selections.get(i).getZoomLevel();
            Rectangle rect = new Rectangle(start, 0, length, selectionWrapper.getHeight());
            rect.setFill(Color.GRAY);
            rect.setOpacity(0.3);
            this.selectionWrapper.getChildren().add(rect);
            Rectangle tickRect = new Rectangle(start, 0, length, ticksWrapper.getHeight());
            tickRect.setFill(Color.GRAY);
            tickRect.setOpacity(0.3);
            this.ticksWrapper.getChildren().add(tickRect);
        }
    }

    /**
     * Removes nodes with class "mosaic"
     */
    public void clearMosaic() {
        Set<Node> mosaicNodes = this.selectionWrapper.lookupAll(".mosaic");
        for (Node node : mosaicNodes) {
            this.selectionWrapper.getChildren().remove(node);
        }
    }

    public void toggleSampleColorStrip(Sample sample, HashMap<String, Color> sampleColors) {
        VBox visualContainer = (VBox) this.samplesInfoContainer.lookup("#" + sample.getName());
        Rectangle rectangle = (Rectangle) visualContainer.getChildren().get(1);
        if (rectangle.getFill() == sampleColors.get(sample.getName())) {
            rectangle.setFill(Color.TRANSPARENT);
        }
        else {
            rectangle.setFill(sampleColors.get(sample.getName()));
        }
    }

    public void showPlot(HashMap<Selection, HashMap<String,Color>> results, ArrayList<Sample> samples, double zoomLevel) {
        clearMosaic();
        ArrayList<Selection> keys = new ArrayList<Selection>(results.keySet());
        // for each selection
        for (int i=0; i<results.size(); i++) {
            double selectionStart = keys.get(i).getStart();
            double selectionLength = keys.get(i).getLength();
            double calcStart = selectionStart * zoomLevel / keys.get(i).getZoomLevel();
            double calcLength = selectionLength * zoomLevel / keys.get(i).getZoomLevel();
            for (int j=0; j<samples.size(); j++) {
                Rectangle rect = new Rectangle(calcStart, j*100, calcLength, 100);
                rect.getStyleClass().add("mosaic");
                rect.setOpacity(0.6);
                rect.setFill(results.get(keys.get(i)).get(samples.get(j).getName()));
                this.selectionWrapper.getChildren().add(rect);
            }
        }
    }

    public void sendSampleToTop(Sample sample, ArrayList<CheckBox> checkBoxes) {
        int numChecked = 0;
        // loop through the checkboxes to see how many are currently checked (pinned to top)
        for (int i=0; i<checkBoxes.size(); i++) {
            if (checkBoxes.get(i).isSelected()) {
                numChecked++;
            }
            else {

            }
        }
        // minus 1 because of the checkbox just clicked for this sample
        int newIndex = numChecked - 1;
        // get current nodes
        Pane calls = (Pane) this.samplesContainer.lookup("#" + sample.getName());
        VBox container = (VBox) this.samplesInfoContainer.lookup("#" + sample.getName());
        // remove
        this.samplesContainer.getChildren().remove(calls);
        this.samplesInfoContainer.getChildren().remove(container);
        // insert at new location
        this.samplesContainer.getChildren().add(newIndex, calls);
        this.samplesInfoContainer.getChildren().add(newIndex, container);
    }

    public void sendSampleToOGLocation(Sample sample, int index, ArrayList<CheckBox> checkBoxes) {
        int numChecked = 0;
        int pastChecked = 0;
        // loop through the checkboxes to see how many are currently still checked (pinned to top)
        for (int i=0; i<checkBoxes.size(); i++) {
            if (checkBoxes.get(i).isSelected()) {
                numChecked++;
                if (i > index) {
                    pastChecked++;
                }
            }
            else {

            }
        }
        //
        int newIndex;
        if (index == 0) {
            // past all checked samples
            newIndex = numChecked;
        }
        else if (index == checkBoxes.size()-1) {
            // end
            newIndex = index;
        }
        else {
            newIndex = index + pastChecked;
        }
        // get current nodes
        Pane calls = (Pane) this.samplesContainer.lookup("#" + sample.getName());
        VBox container = (VBox) this.samplesInfoContainer.lookup("#" + sample.getName());
        // remove
        this.samplesContainer.getChildren().remove(calls);
        this.samplesInfoContainer.getChildren().remove(container);
        // insert at new location
        this.samplesContainer.getChildren().add(newIndex, calls);
        this.samplesInfoContainer.getChildren().add(newIndex, container);
    }

    public void toggleSidePane() {
        if (this.sidePane.getTranslateX() > 0) {
            slideIn.setToX(0);
            slideIn.play();
        } else {
            slideOut.setToX(300);
            slideOut.play();
        }
    }

    public void updateZoom(ArrayList<Sample> samples, double zoomLevel, int refLength, ArrayList<Selection> selections, double baseLevel, int tickSpacing, int originalTrackHeight) {
        this.showCalls(samples, zoomLevel, refLength, originalTrackHeight);
        this.showCoords(refLength, zoomLevel, tickSpacing);
        this.updateSelections(selections, zoomLevel, baseLevel);
        // update marker width
        updateMarkerOnViewportScaleOrZoom(refLength, zoomLevel);
    }

    public void updateHighLevelView(MouseEvent event) {
        /**
         * Deal with moving
         */
        double mouseX = event.getSceneX();

        // Convert scene X to local X of the markerPane
        double localX = markerWrapper.sceneToLocal(mouseX, 0).getX();

        // Clamp within the bounds of the markerPane
        double clampedX = Math.max(0, Math.min(localX, markerWrapper.getWidth() - marker.getWidth()));

        marker.setLayoutX(clampedX);

        // Optionally, print percentage across the width
        double percent = clampedX / markerWrapper.getWidth();

        System.out.printf("Marker at X: %.2f (%.1f%%)%n", clampedX, percent * 100);
        this.callsPanel.setHvalue(percent);
    }


    public void importListener(EventHandler<ActionEvent> handler) {
        importVCFItem.setOnAction(handler);
    }
    public void zoomInListener(EventHandler<ActionEvent> handler) {
        zoomInButton.setOnAction(handler);
    }
    public void zoomOutListener(EventHandler<ActionEvent> handler) {
        zoomOutButton.setOnAction(handler);
    }
    public void clearSelectionsListener(EventHandler<ActionEvent> handler) {
        clearButton.setOnAction(handler);
    }
    public void processSelectionsListener(EventHandler<ActionEvent> handler) {
        processButton.setOnAction(handler);
    }
    public void processBlocksSelectionsListener(EventHandler<ActionEvent> handler) {
        processBlocksButton.setOnAction(handler);
    }
    public void shrinkTrackHeightListener(EventHandler<ActionEvent> handler) {
        shrinkTrackHeightButton.setOnAction(handler);
    }
    public void growTrackHeightListener(EventHandler<ActionEvent> handler) {
        growTrackHeightButton.setOnAction(handler);
    }
    public void releaseSelectionListener(EventHandler<MouseEvent> handler) {
        this.releaseSelectionHandler = handler;
    }
    public void toggleSidePaneListener(EventHandler<ActionEvent> handler) {
        sidePaneButton.setOnAction(handler);
    }
    public void closeSidePaneListener(EventHandler<ActionEvent> handler) {
        closeSidePaneButton.setOnAction(handler);
    }
    public void viewportWidthChange(EventHandler<ActionEvent> handler) {
        callsPanel.viewportBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
            if (oldBounds == null || newBounds == null || oldBounds.getWidth() != newBounds.getWidth()) {
                handler.handle(new ActionEvent(this, null));
            }
        });
    }
}


