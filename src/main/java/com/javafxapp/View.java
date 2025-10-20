package com.javafxapp;

import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
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
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.stage.Screen;

import java.util.*;
import java.util.function.Supplier;

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

public class View {
    ArrayList<Sample> sampleOrder = new ArrayList<Sample>();
    // ----------- root and side pane ---------------
    VBox sidePane = new VBox(20);
    ArrayList<CheckBox> pinCheckboxes = new ArrayList<>();
    VBox comparators = new VBox();
    Button closeSidePaneButton = new Button("\u00D7");
    HBox closeButtonContainer = new HBox(closeSidePaneButton);
    Label regionSelectLabel = new Label("Provide a Region to Select");
    private final VBox layout = new VBox(3);
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
    private final Pane referenceWrapper = new Pane();
    private final Pane markerWrapper = new Pane();
    private Label l1 = new Label("");
    private Label l2 = new Label("");
    VBox labelsBox = new VBox(10, l1, l2);
    StackPane rectangleWithLabels = new StackPane(referenceWrapper, labelsBox);
    StackPane rectWithMarker = new StackPane(rectangleWithLabels, markerWrapper);
    // ---------- tickContainer ----------
    private final HBox tickContainer = new HBox();
    private final Pane spaceWrapper = new Pane();
    private final Pane ticksWrapper = new Pane();
    private EventHandler<MouseEvent> releaseSelectionHandler;
    private final int sampleSpaceWidth = 90;
    // ----------- dropdownChromContainer --------
    private final HBox dropdownChromContainer = new HBox(10);
    TextField regionField = new TextField();
    ComboBox<String> chromComboBox = new ComboBox<>();
    // ----------- controlContainer ---------
    private final HBox controlContainer = new HBox();
    Region controlContainerSpacer = new Region();
    Button zoomInButton = new Button("+");
    Button zoomOutButton = new Button("-");
    Button clearButton = new Button("Clear");
    Button processButton = new Button("Display Haplotypes");
    Button processBlocksButton = new Button("Display Variants in Unpinned");
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
        sidePane.setStyle("-fx-background-color: #e0e0e0;"
        + "-fx-background-radius: 5px;"
        + "-fx-border-color: #c0c0c0;"
        + "-fx-border-radius: 5px;"
        + "-fx-border-width: 1;");
        sidePane.setTranslateX(300);
        sidePane.setAlignment(Pos.TOP_CENTER);
        // close button
        sidePane.getChildren().add(closeButtonContainer);
        closeButtonContainer.setAlignment(Pos.TOP_RIGHT);
        // title
        Label title = new Label("Selection Options");
        String style = """
               -fx-font-size: 20px;
               -fx-text-fill: #555555;
               -fx-font-weight: bold;
               """;
        title.setStyle(style);
        regionSelectLabel.setStyle("""
                -fx-text-fill: #555555;
               -fx-font-weight: bold;
               """);

        sidePane.getChildren().add(title);
        // region
        sidePane.getChildren().add(regionSelectLabel);
        Separator separator1 = new Separator();
        sidePane.getChildren().add(separator1);
        // pin checkboxes
        sidePane.getChildren().add(comparators);
        Separator separator2 = new Separator();
        sidePane.getChildren().add(separator2);
        // plots
        sidePane.getChildren().add(processBlocksButton);
        sidePane.getChildren().add(processButton);


        // ---------- MENU --------------
        fileMenu.getItems().add(importVCFItem);
        menuBar.getMenus().add(fileMenu);
        // ---------- DROPDOWN CHROM CONTAINER ----
        dropdownChromContainer.setAlignment(Pos.CENTER);
        regionField.setPromptText("Enter region:");
        dropdownChromContainer.getChildren().addAll(chromComboBox, regionField);
        // initally disable dropdown and text field
        regionField.setDisable(true);
        chromComboBox.setDisable(true);
        regionField.setMinWidth(180);
        regionField.setMaxWidth(180);
        chromComboBox.setMinWidth(180);
        chromComboBox.setMaxWidth(180);
        // ---------- CONTROL PANEL -----
        // push buttons right
        HBox.setHgrow(controlContainerSpacer, Priority.ALWAYS);
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
        zoomInButton.setMinSize(35, 25);
        zoomInButton.setMaxSize(35, 25);
        zoomOutButton.setMinSize(35, 25);
        zoomOutButton.setMaxSize(35, 25);
        clearButton.setMinSize(50,25);
        processButton.setMinSize(50,25);
        processBlocksButton.setMinSize(75,25);
        shrinkTrackHeightButton.setMinSize(75,25);
        shrinkTrackHeightButton.setMaxSize(75,25);
        growTrackHeightButton.setMinSize(80,25);
        growTrackHeightButton.setMaxSize(80,25);
        sidePaneButton.setMinSize(120,25);
        sidePaneButton.setMaxSize(120,25);
        closeSidePaneButton.setMinSize(25,25);
        closeSidePaneButton.setMaxSize(25,25);
        String circularStyle = """
    -fx-background-radius: 10px;
    -fx-border-radius: 10px;
    -fx-text-fill: #555555;
    -fx-font-size: 11px;
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
        controlContainer.getChildren().add(controlContainerSpacer);
        controlContainer.getChildren().add(zoomInButton);
        controlContainer.getChildren().add(zoomOutButton);
        controlContainer.getChildren().add(clearButton);
        controlContainer.getChildren().add(shrinkTrackHeightButton);
        controlContainer.getChildren().add(growTrackHeightButton);
        controlContainer.getChildren().add(sidePaneButton);
        // ---------- REF PANEL ---------
        referenceContainer.setStyle("-fx-background-color: white;");
        // reference rectangle
        this.referenceWrapper.setPrefHeight(50);
        this.referenceContainer.getChildren().add(rectWithMarker);
        // marker
        markerWrapper.getChildren().add(marker);
        layout.getChildren().addAll(menuBar, dropdownChromContainer, controlContainer, referenceContainer, tickContainer, callsContentContainer);
        // ---------- TICK PANEL ---------
        spaceWrapper.setMinWidth(this.sampleSpaceWidth);
        spaceWrapper.setPrefWidth(this.sampleSpaceWidth);
        spaceWrapper.setMaxWidth(this.sampleSpaceWidth);
        tickContainer.getChildren().add(spaceWrapper);
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
        this.callsPanel.setMinWidth(Screen.getPrimary().getBounds().getWidth() - sampleSpaceWidth);
        this.callsPanel.setPrefWidth(Screen.getPrimary().getBounds().getWidth() - sampleSpaceWidth);
        this.callsPanel.setMaxWidth(Screen.getPrimary().getBounds().getWidth() - sampleSpaceWidth);
        selectionWrapper.setPickOnBounds(false);
        this.callsPanel.hvalueProperty().addListener((obs, oldVal, newVal) -> {
            this.syncScroll(newVal);
        });
        this.selectionContainer.getChildren().add(selectionWrapper);
        this.callsPanel.setFitToHeight(true);
    }

    /**
     * Enable all buttons in controlContainer and sidePane. Side Pane translation also done here after layout is complete
     **/
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
        this.regionField.setDisable(false);
        this.chromComboBox.setDisable(false);
        double bottomOfTickContainerY = tickContainer.getLayoutY() + tickContainer.getLayoutBounds().getHeight();
        sidePane.setTranslateY(bottomOfTickContainerY);
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

    public void initSidePane(ArrayList<Sample> samples, HashMap<String, Color> sampleColors, Supplier<Integer> numAnnotations) {
        HashMap<String,Boolean> result = new HashMap<>();
        comparators.setPadding(new Insets(0, 0, 0, 20));
        Label pinLabel = new Label("Pin Samples to Top:");
        String style = """
                -fx-text-fill: #555555;
                -fx-font-weight: bold;
                """;
        pinLabel.setStyle(style);
        comparators.getChildren().add(pinLabel);
        int index = 0;
        for (Sample sample : samples) {
            Label label = new Label(sample.getName());
            CheckBox checkBox = new CheckBox();
            HBox hBox = new HBox(10);
            hBox.getChildren().addAll(label, checkBox);
            pinCheckboxes.add(checkBox);
            comparators.getChildren().add(hBox);
            int OGIndex = index;
            checkBox.setOnAction(event -> {
                if (checkBox.isSelected()) {
                    moveSample(sample, OGIndex, "TOP", numAnnotations.get());
                    toggleSampleColorStrip(sample, sampleColors);
                }
                else {
                    moveSample(sample, OGIndex, "BOTTOM", numAnnotations.get());
                    toggleSampleColorStrip(sample, sampleColors);

                }
            });
            index++;
        }
    }

    public void initReference(LinkedHashMap<String, Integer> refContigs, int totalRefLength) {
        referenceWrapper.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
//        referenceWrapper.setBorder(new Border(new BorderStroke(
//                Color.BLACK,
//                BorderStrokeStyle.SOLID,
//                CornerRadii.EMPTY,
//                new BorderWidths(1)
//        )));
        labelsBox.setStyle("-fx-alignment: center;");
        marker.setFill(Color.ORANGERED);
        marker.setOpacity(0.5);
        marker.setOnMouseDragged(event -> updateHighLevelView(event));
        // fill chrom combo box
        double currentX = 0;
        // add <ALL> as a chrom dropdown option and set as default
        chromComboBox.getItems().add("<ALL>");
        chromComboBox.setValue("<ALL>");
        // add each chromosome from VCF
        for (Map.Entry<String, Integer> refContig : refContigs.entrySet()) {
            // add to chromComboBox dropdown
            chromComboBox.getItems().add(refContig.getKey());
            // get percentage of total reference
            double percent = ((float) refContig.getValue() / totalRefLength);
            // add rectangle to referenceWrapper
            Rectangle rect = new Rectangle();
            rect.setX(currentX);
            rect.setStroke(Color.DARKGRAY);
            rect.setFill(Color.WHITE);
            rect.setArcWidth(14);
            rect.setArcHeight(14);
            rect.setHeight(referenceWrapper.getHeight());
            rect.setWidth(referenceWrapper.getWidth() * percent);
            referenceWrapper.getChildren().add(rect);
            currentX += rect.getWidth();
        }
        // handle chromComboBox selection
        chromComboBox.setOnAction(e -> {
            String selected = chromComboBox.getValue();
            System.out.println("SELECTED: " + selected);
        });
//        this.l1.setText(refName);
//        this.l2.setText(String.valueOf(refLength) + " bp");
    }

    public void initSamples(ArrayList<Sample> samples, int refLength, double zoomLevel, double baseFontSize, int originalTrackHeight) {
        /*
        Post-conditions: Samples added to sampleOrder ArrayList
         */
        // add additional track for allele frequency
        this.createNewAnnotationTrack(refLength, zoomLevel, "AF", baseFontSize, 100, "AF");
        for (Sample sample : samples) {
            sampleOrder.add(sample);
            this.createNewCallTrack(refLength, zoomLevel, sample.getName(), baseFontSize, originalTrackHeight);
        }
        this.callsPanel.setPannable(true);   // Optional: enables mouse drag scrolling
    }

    public void createNewAnnotationTrack(int refLength, double zoomLevel, String trackName, double baseFontSize, int height, String key) {
        // types
        // GENEREPEAT
        // AF
        // PILEUP
        Pane callsWrapper = new Pane();
        callsWrapper.setPrefWidth(refLength * zoomLevel);
        // force height of track (or else it will collapse if there is no content)
        callsWrapper.setMinHeight(height);
        callsWrapper.setMaxHeight(height);
        if (Objects.equals(key, "AF")) {
            Line topLine = new Line();
            Line bottomLine = new Line();

            // Make them dotted (dashed)
            topLine.getStrokeDashArray().addAll(5.0, 5.0);
            bottomLine.getStrokeDashArray().addAll(5.0, 5.0);

            // Set color
            topLine.setStroke(Color.BLACK);
            bottomLine.setStroke(Color.BLACK);
            LinearGradient lg = new LinearGradient(
                    0, 0, 0, 1,      // startX, startY, endX, endY
                    true,            // proportional
                    CycleMethod.NO_CYCLE,
                    new Stop(0.0, Color.RED),    // red from 0%...
                    new Stop(0.05, Color.RED),   // ...to 5%
                    new Stop(0.06, Color.GREEN),  // green at center
                    new Stop(0.5, Color.GREEN),  // green at center
                    new Stop(0.95, Color.RED),   // red starts again at 95%
                    new Stop(1.0, Color.RED)     // red to bottom
            );
            BackgroundFill bgFill = new BackgroundFill(lg, CornerRadii.EMPTY, Insets.EMPTY);
            //callsWrapper.setBackground(new Background(bgFill));
        }
        else {
            // do nothing
        }
        // create labelWrapper Pane to hold sample name
        StackPane labelWrapper = new StackPane();
        labelWrapper.setMinWidth(this.sampleSpaceWidth);
        labelWrapper.setMaxWidth(this.sampleSpaceWidth);
        // create sample label
        Label sampleLabel = new Label(trackName);
        sampleLabel.setFont(Font.font("System", baseFontSize));
        // create infoContainer to hold sample info
        HBox labelContainer = new HBox();
        // create visualContainer to hold sample info and color rectangle
        VBox infoContainer = new VBox();
        infoContainer.setMinHeight(height);
        infoContainer.setMaxHeight(height);
        infoContainer.getChildren().addAll(labelContainer);
        infoContainer.setAlignment(Pos.CENTER);
        // add sample label to infoContainer
        labelWrapper.getChildren().add(sampleLabel);
        labelContainer.getChildren().add(labelWrapper);
        this.samplesInfoContainer.getChildren().add(infoContainer);
        // add sampContainer to samplesContainer
        this.samplesContainer.getChildren().add(callsWrapper);
    }

    public void createNewCallTrack(int refLength, double zoomLevel, String sampleName, double baseFontSize, int height) {
        // create callsWrapper to hold sample calls
        Pane callsWrapper = new Pane();
        callsWrapper.setPrefWidth(refLength * zoomLevel);
        // force height of track (or else it will collapse if there is no content)
        callsWrapper.setMinHeight(height);
        callsWrapper.setMaxHeight(height);
        // create labelWrapper Pane to hold sample name
        StackPane labelWrapper = new StackPane();
        labelWrapper.setMinWidth(this.sampleSpaceWidth);
        labelWrapper.setMaxWidth(this.sampleSpaceWidth);
        // create sample label
        Label sampleLabel = new Label(sampleName);
        sampleLabel.setFont(Font.font("System", baseFontSize));
        // create infoContainer to hold sample info
        HBox labelContainer = new HBox();
        // create visualContainer to hold sample info and color rectangle
        VBox infoContainer = new VBox();
        infoContainer.setMinHeight(height);
        infoContainer.setMaxHeight(height);
        Rectangle colorRect = new Rectangle(40, 3, Color.TRANSPARENT);
        infoContainer.getChildren().addAll(labelContainer, colorRect);
        infoContainer.setAlignment(Pos.CENTER);
        // add sample label to infoContainer
        labelWrapper.getChildren().add(sampleLabel);
        labelContainer.getChildren().add(labelWrapper);
        this.samplesInfoContainer.getChildren().add(infoContainer);
        // add sampContainer to samplesContainer
        this.samplesContainer.getChildren().add(callsWrapper);
        // set IDs
        infoContainer.setId(sampleName);
        callsWrapper.setId(sampleName);
    }

    public void showCalls(ArrayList<Sample> samples, double zoomLevel, int refLength, int originalTrackHeight) {
        /**
         * Pre-conditions/assumptions: Gets call pane for each sample by looking up the ID
         */
        this.samplePanel.setMinWidth(refLength * zoomLevel);
        this.samplePanel.setMaxWidth(refLength * zoomLevel);

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
                Rectangle callRect = new Rectangle(currentCall.getStart()*zoomLevel, 1, currentCall.getLength()*zoomLevel, originalTrackHeight-2);
                String callId = sample.getName() + "-" + j;
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

    public double initZoomAndCoords(int refLength, int tickSpacing) {
        this.ticksWrapper.getChildren().clear();
        // calculate zoom level such that whole genome is in view
        double zoomLevel = callsPanel.getViewportBounds().getWidth() / refLength;
        System.out.println("THE BASE LEVEL IS:" + zoomLevel);
        // display ticks
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
        updateMarkerOnViewportScaleOrZoom(refLength, zoomLevel);
        return zoomLevel;
    }

    public void updateMarkerOnViewportScaleOrZoom(int refLength, double zoomLevel) {
        // set width
        double contentWidth = refLength * zoomLevel;
        callsPanel.layout();
        double viewportWidth = callsPanel.getViewportBounds().getWidth();
        double proportionVisible = viewportWidth / contentWidth;
        System.out.println("PROPORTION VISIBLE" + proportionVisible);
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

    public void showPlot(HashMap<Rectangle,Color> results) {
        Set<Rectangle> keys = results.keySet();
        for (Rectangle rectangle : keys) {
            rectangle.setOpacity(0.6);
            rectangle.setFill(results.get(rectangle));
            this.selectionWrapper.getChildren().add(rectangle);
        }
    }

    public ArrayList<CheckBox> getPinCheckboxes() {
        return this.pinCheckboxes;
    }


    public void moveSample(Sample sample, int OGIndex, String setting, int numAnnotations) {
        int numChecked = 0;
        // past refers to below here
        int pastChecked = 0;
        int newIndex;

        // loop through the checkboxes to see how many are currently checked (pinned to top)
        for (int i=0; i<pinCheckboxes.size(); i++) {
            if (pinCheckboxes.get(i).isSelected()) {
                numChecked++;
                if (i > OGIndex) {
                    pastChecked++;
                }
            }
            else {

            }
        }

        // move sample based on setting
        if (Objects.equals(setting, "TOP")) {
            // minus 1 because of the checkbox just clicked for this sample
            newIndex = numChecked - 1;
        }
        else {
            if (OGIndex == 0) {
                // past all checked samples
                newIndex = numChecked;
            }
            else if (OGIndex == pinCheckboxes.size()-1) {
                // end
                newIndex = OGIndex;
            }
            else {
                newIndex = OGIndex + pastChecked;
            }
        }

        // get current nodes
        Pane calls = (Pane) this.samplesContainer.lookup("#" + sample.getName());
        VBox container = (VBox) this.samplesInfoContainer.lookup("#" + sample.getName());
        // remove
        this.samplesContainer.getChildren().remove(calls);
        this.samplesInfoContainer.getChildren().remove(container);
        this.sampleOrder.remove(sample);
        // insert at new location
        this.samplesContainer.getChildren().add(newIndex+numAnnotations, calls);
        this.samplesInfoContainer.getChildren().add(newIndex+numAnnotations, container);
        this.sampleOrder.add(newIndex, sample);
    }

    public ArrayList<Sample> getSampleOrderInView() {
        return this.sampleOrder;
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
        double contentWidth = refLength * zoomLevel;
        double viewportWidth = callsPanel.getViewportBounds().getWidth();
        double proportionVisible = viewportWidth / contentWidth;
        if (proportionVisible >= 1) {
            // Create and show an information alert
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setHeaderText(null);  // Optional: no header text
            alert.setContentText("This is a popup alert message!");
            alert.showAndWait();
        }
        else {
            this.showCalls(samples, zoomLevel, refLength, originalTrackHeight);
            this.initZoomAndCoords(refLength, tickSpacing);
            this.updateSelections(selections, zoomLevel, baseLevel);
            // update marker width
            updateMarkerOnViewportScaleOrZoom(refLength, zoomLevel);
        }
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
