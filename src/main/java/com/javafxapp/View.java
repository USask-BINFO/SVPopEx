package com.javafxapp;

import javafx.animation.TranslateTransition;
import javafx.beans.value.ChangeListener;
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
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.stage.Screen;


import java.sql.SQLOutput;
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
    VBox sidePaneContainer = new VBox(0);
    VBox selectionOptionsSideContainer = new VBox(20);
    VBox callInfoSideContainer = new VBox(7);
    Call liveCall = null;
    StackPane sidePaneSwapPanel = new StackPane(callInfoSideContainer, selectionOptionsSideContainer);
    ArrayList<CheckBox> pinCheckboxes = new ArrayList<>();
    HBox mateContainer = new HBox();
    Button showMateButton = new Button("Show");
    VBox comparators = new VBox();
    Button closeSidePaneButton = new Button("\u00D7");
    HBox closeButtonContainer = new HBox(closeSidePaneButton);
    Label regionSelectLabel = new Label("Provide a Region to Select");
    private final VBox layout = new VBox(3);
    StackPane root = new StackPane(layout, sidePaneContainer);
    private final Stage primaryStage;
    TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), sidePaneContainer);
    TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), sidePaneContainer);
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
    Button processRegionButton = new Button("Go");
    // ----------- controlContainer ---------
    private final HBox controlContainer = new HBox();
    Region controlContainerSpacer = new Region();
    Button zoomInButton = new Button("+");
    Button zoomOutButton = new Button("-");
    Button clearButton = new Button("Clear");
    Button processButton = new Button("Color by Haplotype");
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
        // ---------- STYLING --------------------
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
        // ---------- ROOT AND SIDE PANE ---------
        String regularStyle = """
                -fx-text-fill: #555555;
                -fx-font-weight: bold;
                """;
        this.primaryStage = primaryStage;
        root.setAlignment(sidePaneContainer, Pos.CENTER_RIGHT);
        sidePaneContainer.setMinWidth(300);
        sidePaneContainer.setMaxWidth(300);
        sidePaneContainer.setStyle("-fx-background-color: #e0e0e0;"
        + "-fx-background-radius: 5px;"
        + "-fx-border-color: #c0c0c0;"
        + "-fx-border-radius: 5px;"
        + "-fx-border-width: 1;");
        sidePaneContainer.setTranslateX(300);
        // close button
        sidePaneContainer.getChildren().add(closeButtonContainer);
        sidePaneContainer.getChildren().add(sidePaneSwapPanel);

        closeButtonContainer.setAlignment(Pos.TOP_RIGHT);
        // title
        Label title = new Label("Selection Options");
        String style = """
               -fx-font-size: 20px;
               -fx-text-fill: #555555;
               -fx-font-weight: bold;
               """;
        title.setStyle(style);
        regionSelectLabel.setStyle(regularStyle);
        Region spacer1 = new Region();
        spacer1.setMinHeight(20);
        Region spacer2 = new Region();
        spacer2.setMinHeight(20);
        selectionOptionsSideContainer.setAlignment(Pos.TOP_CENTER);

        selectionOptionsSideContainer.getChildren().add(spacer1);
        selectionOptionsSideContainer.getChildren().add(title);
        // region
        selectionOptionsSideContainer.getChildren().add(regionSelectLabel);
        Separator separator1 = new Separator();
        selectionOptionsSideContainer.getChildren().add(separator1);
        // pin checkboxes
        selectionOptionsSideContainer.getChildren().add(comparators);
        Separator separator2 = new Separator();
        selectionOptionsSideContainer.getChildren().add(separator2);
        // plots
        selectionOptionsSideContainer.getChildren().add(processBlocksButton);
        selectionOptionsSideContainer.getChildren().add(processButton);


        callInfoSideContainer.setAlignment(Pos.TOP_LEFT);
        callInfoSideContainer.setPadding(new Insets(0, 0, 0, 15));
        callInfoSideContainer.getChildren().add(spacer2);
        Label callTitle = new Label("Call Information");
        callTitle.setStyle(style);
        callInfoSideContainer.getChildren().add(callTitle);
        callTitle.setWrapText(true);
        callInfoSideContainer.getChildren().add(new Separator());

        // create labels with ids to be filled with info when call is selected
        Label typeFill = new Label("");
        typeFill.setId("type");
        Label chromFill = new Label("");
        chromFill.setId("chrom");
        Label posFill = new Label("");
        posFill.setId("pos");
        Label idFill = new Label("");
        idFill.setId("id");

        this.mateContainer.setId("bndContainer");
        this.mateContainer.setAlignment(Pos.CENTER_LEFT);
        Label mateFill = new Label("");
        mateFill.setId("mate");

        Label genotypesFill = new Label("");
        genotypesFill.setId("genotypes");
        // styling to set genotypes to the right
        genotypesFill.setPadding(new Insets(0, 0, 0, 15));

        Label typeLabel = new Label("TYPE: ");
        typeLabel.setStyle(regularStyle);
        Label chromLabel = new Label("CHROM: ");
        chromLabel.setStyle(regularStyle);
        Label posLabel = new Label("POS: ");
        posLabel.setStyle(regularStyle);
        Label idLabel = new Label("ID: ");
        idLabel.setStyle(regularStyle);
        Label mateLabel = new Label("MATE: ");
        mateLabel.setStyle(regularStyle);
        Label genotypesLabel = new Label("GENOTYPES: ");
        genotypesLabel.setStyle(regularStyle);

        callInfoSideContainer.getChildren().add(new HBox(typeLabel, typeFill));
        callInfoSideContainer.getChildren().add(new HBox(chromLabel, chromFill));
        callInfoSideContainer.getChildren().add(new HBox(posLabel, posFill));
        callInfoSideContainer.getChildren().add(new HBox(idLabel, idFill));
        mateContainer.getChildren().addAll(mateLabel, mateFill, showMateButton);
        callInfoSideContainer.getChildren().add(mateContainer);
        callInfoSideContainer.getChildren().add(genotypesLabel);
        callInfoSideContainer.getChildren().add(genotypesFill);

        // upon creation the selection container is visible
        selectionOptionsSideContainer.setVisible(true);
        selectionOptionsSideContainer.setManaged(true);
        // make sure call info container is not visible
        callInfoSideContainer.setVisible(false);
        callInfoSideContainer.setManaged(false);


        // ---------- MENU --------------
        fileMenu.getItems().add(importVCFItem);
        menuBar.getMenus().add(fileMenu);
        // ---------- DROPDOWN CHROM CONTAINER ----
        dropdownChromContainer.setAlignment(Pos.CENTER);
        regionField.setPromptText("Show region: chrom1:0-100");
        regionField.setStyle("""
        -fx-focus-color: transparent;
        -fx-faint-focus-color: transparent;
        -fx-border-color: #AAAAAA;
        -fx-border-radius: 3px;
        -fx-border-width: 1px;
        """);
        processRegionButton.setDisable(true);
        processRegionButton.setFocusTraversable(false);
        processRegionButton.setStyle("""
        -fx-focus-color: transparent;
        -fx-faint-focus-color: transparent;
        -fx-border-color: #AAAAAA;
        -fx-border-radius: 3px;
        -fx-border-width: 1px;
        -fx-cursor: hand;
        """);
        dropdownChromContainer.getChildren().addAll(chromComboBox, regionField, processRegionButton);
        chromComboBox.setStyle("""
        -fx-focus-color: transparent;
        -fx-faint-focus-color: transparent;
        -fx-border-color: #AAAAAA;
        -fx-border-radius: 3px;
        -fx-border-width: 1px;
        -fx-cursor: hand;
        """);
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
        showMateButton.setDisable(true);
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
        showMateButton.setFocusTraversable(false);
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
        marker.setOnMouseEntered(e -> marker.setCursor(Cursor.HAND));
        marker.setOnMouseExited(e -> marker.setCursor(Cursor.DEFAULT));
        layout.getChildren().addAll(menuBar, dropdownChromContainer, controlContainer, referenceContainer, tickContainer, callsContentContainer);
        // ---------- TICK PANEL ---------
        spaceWrapper.setMinWidth(this.sampleSpaceWidth);
        spaceWrapper.setPrefWidth(this.sampleSpaceWidth);
        spaceWrapper.setMaxWidth(this.sampleSpaceWidth);
        tickContainer.getChildren().add(spaceWrapper);
        this.ticksWrapper.setPrefHeight(40);
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
        this.selectionContainer.setPickOnBounds(false);
        this.selectionContainer.getChildren().add(selectionWrapper);
        this.callsPanel.setFitToHeight(true);
    }

    // *************************************************************** MAIN CONTROL FUNCTIONS ************************************************************************
    /**
     * Enables all buttons in controlContainer and sidePane. Side Pane translation also done here after layout is complete
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
        this.processRegionButton.setDisable(false);
        this.showMateButton.setDisable(false);
        double bottomOfTickContainerY = tickContainer.getLayoutY() + tickContainer.getLayoutBounds().getHeight();
        sidePaneContainer.setTranslateY(bottomOfTickContainerY);
    }

    /**
     * Resets View so new file can be uploaded.
     */
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
        this.processRegionButton.setDisable(true);
        this.showMateButton.setDisable(true);
    }

    public Scene getScene() {
        return new Scene(root);
    }

    public Stage getPrimaryStage() {
        return this.primaryStage;
    }

    // *************************************************************** MAIN INITIALIZATION FUNCTIONS ************************************************************************

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
                    toggleSampleLock(sample);
                }
                else {
                    moveSample(sample, OGIndex, "BOTTOM", numAnnotations.get());
                    toggleSampleLock(sample);

                }
            });
            index++;
        }
    }

    /**
     *
     * @param refContigs
     * @param totalRefLength
     *
     * Post Conditions: chromComboBox default set to < ALL >
     */
    public void initReference(LinkedHashMap<String, Chromosome> refContigs, int totalRefLength) {
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
        // fill chrom combo box
        double currentX = 0;
        // add <ALL> as a chrom dropdown option and set as default
        chromComboBox.getItems().add("<ALL>");
        double totalWidth = 0;
        for (Map.Entry<String, Chromosome> refContig : refContigs.entrySet()) {
            if (Objects.equals(refContig.getKey(), "<ALL>")) {
                // do nothing
            }
            else {
                // add to chromComboBox dropdown
                chromComboBox.getItems().add(refContig.getKey());
                // get percentage of total reference
                double percent = ((float) refContig.getValue().getLength() / totalRefLength);
                double chromWidth = referenceWrapper.getWidth() * percent;
                refContig.getValue().setPixelAbsoluteOffset(currentX);
                refContig.getValue().setPixelWidth(chromWidth);
                totalWidth += chromWidth;
                currentX += chromWidth;
            }
        }
        refContigs.get("<ALL>").setPixelAbsoluteOffset(0);
        refContigs.get("<ALL>").setPixelWidth(totalWidth);
    }

    public void setChromComboBoxValue(String value) {
        chromComboBox.setValue(value);
    }

    public void drawReference(LinkedHashMap<String, Chromosome> refContigs, String chrom) {
        // reset reference wrapper and labels
        referenceWrapper.getChildren().clear();
        l1.setText("");
        l2.setText("");
        // draw all chromosomes
        if (Objects.equals(chrom, "<ALL>")) {
            // add each chromosome from VCF to create chromosome representation
            double currentX = 0;
            for (Map.Entry<String, Chromosome> refContig : refContigs.entrySet()) {
                if (Objects.equals(refContig.getKey(), "<ALL>")) {
                    // do nothing
                }
                else {
                    // add rectangle to referenceWrapper
                    Rectangle rect = new Rectangle();
                    rect.setX(currentX);
                    rect.setStroke(Color.DARKGRAY);
                    rect.setFill(Color.WHITE);
                    rect.setArcWidth(14);
                    rect.setArcHeight(14);
                    rect.setHeight(referenceWrapper.getHeight());
                    rect.setWidth(refContig.getValue().getPixelWidth());
                    referenceWrapper.getChildren().add(rect);
                    currentX += rect.getWidth();
                }
            }
        }
        // draw a specific chromosome
        else {
            // add rectangle to referenceWrapper
            Rectangle rect = new Rectangle();
            rect.setX(0);
            rect.setStroke(Color.DARKGRAY);
            rect.setFill(Color.WHITE);
            rect.setArcWidth(14);
            rect.setArcHeight(14);
            rect.setHeight(referenceWrapper.getHeight());
            rect.setWidth(markerWrapper.getWidth());
            referenceWrapper.getChildren().add(rect);
            this.l1.setText(chrom);
            String formattedLength = String.format("%,d", refContigs.get(chrom).getLength());
            this.l2.setText(formattedLength + " bp");
        }
    }

    public void initSamples(ArrayList<Sample> samples, HashMap<String, Color> sampleColors, int refLength, double zoomLevel, double baseFontSize, int originalTrackHeight) {
        /*
        Post-conditions: Samples added to sampleOrder ArrayList
         */
        // add additional track for allele frequency
        this.createNewAnnotationTrack(refLength, zoomLevel, "Allele Freq.", baseFontSize, 100, "AF");
        for (Sample sample : samples) {
            sampleOrder.add(sample);
            this.createNewCallTrack(sampleColors, refLength, zoomLevel, sample.getName(), baseFontSize, originalTrackHeight);
        }
        this.callsPanel.setPannable(true);   // Optional: enables mouse drag scrolling
    }

    public void showChromosomeAlleleFreq(Chromosome chromosome, double zoomLevel, int originalTrackHeight) {
        Pane freqPane = (Pane) this.samplesContainer.lookup("#" + "AlleleFreq");
        freqPane.getChildren().clear();
        freqPane.setMinWidth(chromosome.getLength() * zoomLevel);
        freqPane.setPrefWidth(chromosome.getLength() * zoomLevel);
        freqPane.setMaxWidth(chromosome.getLength() * zoomLevel);
        for (int i=0; i<chromosome.getAllCalls().size(); i++) {
            Call currentCall = chromosome.getAllCalls().get(i);
            double currentFreq = chromosome.getAllCalls().get(i).getAlleleFreq();
            Circle circle;
            if (Objects.equals(chromosome.getName(), "<ALL>")) {
                circle = new Circle(currentCall.getAbsoluteStart()*zoomLevel, originalTrackHeight * currentFreq, 1);
            }
            else {
                circle = new Circle(currentCall.getStart()*zoomLevel, originalTrackHeight * currentFreq, 1);
            }
            circle.setFill(Color.BLACK);
            circle.setStroke(Color.BLACK);
            freqPane.getChildren().add(circle);
        }
    }

    public void showTileAlleleFreq(Chromosome chromosome, double zoomLevel, int originalTrackHeight, int startInterval, int endInterval) {
        Pane freqPane = (Pane) this.samplesContainer.lookup("#" + "AlleleFreq");
        freqPane.getChildren().clear();
        freqPane.setMinWidth(chromosome.getLength() * zoomLevel);
        freqPane.setPrefWidth(chromosome.getLength() * zoomLevel);
        freqPane.setMaxWidth(chromosome.getLength() * zoomLevel);
        // loop through each tile
        for (int i=startInterval; i<endInterval+1; i++) {
            // for each call
            for (Call currentCall : chromosome.getTiledCallStarts().get(i)) {
                double currentFreq = currentCall.getAlleleFreq();
                Circle circle = new Circle(currentCall.getStart()*zoomLevel, originalTrackHeight * currentFreq, 1);
                circle.setFill(Color.BLACK);
                circle.setStroke(Color.BLACK);
                freqPane.getChildren().add(circle);
            }
        }
    }

    public void showChromosomeCalls(Chromosome chromosome, ArrayList<Sample> samples, double zoomLevel, int originalTrackHeight) {
        /**
         * Pre-conditions/assumptions: Gets call pane for each sample by looking up the ID
         */

        this.samplePanel.setMinWidth(chromosome.getLength() * zoomLevel);
        this.samplePanel.setMaxWidth(chromosome.getLength() * zoomLevel);

        // loops through each sample and gets the sample pane to update, access order does not matter
        for (Sample sample : samples) {
            Pane currentCalls = (Pane) this.samplesContainer.lookup("#" + sample.getName());
            currentCalls.getChildren().clear();
            currentCalls.setMinWidth(chromosome.getLength() * zoomLevel);
            currentCalls.setPrefWidth(chromosome.getLength() * zoomLevel);
            currentCalls.setMaxWidth(chromosome.getLength() * zoomLevel);
            // loop through each call
            for (int j=0; j<sample.getChromosomeCalls(chromosome.getName()).size(); j++) {
                // get the current Call and set its id for the call and rectangle
                Call currentCall = sample.getChromosomeCalls(chromosome.getName()).get(j);
                Rectangle callRect;
                if (Objects.equals(chromosome.getName(), "<ALL>")) {
                    callRect = new Rectangle(currentCall.getAbsoluteStart()*zoomLevel, 1, currentCall.getLength()*zoomLevel, originalTrackHeight-2);
                }
                else {
                    callRect = new Rectangle(currentCall.getStart()*zoomLevel, 1, currentCall.getLength()*zoomLevel, originalTrackHeight-2);
                }
                // styling
                callRect.setOpacity(1);
                callRect.setStrokeWidth(2);
                callRect.setArcWidth(5);   // horizontal roundness
                callRect.setArcHeight(5);
                if (Objects.equals(currentCall.getType(), "DUP")) {
                    callRect.setStroke(Color.rgb(40, 70, 160));
                    callRect.setOpacity(0.5);
                    callRect.setFill(Color.rgb(65, 105, 225));
                    double percentageHeight = originalTrackHeight * 0.15;
                    double percentageLength = currentCall.getLength()*zoomLevel * 0.1;
                    double endX = currentCall.getStart()*zoomLevel + currentCall.getLength()*zoomLevel - percentageLength;
                    Line lineDup1 = new Line(currentCall.getStart()*zoomLevel + percentageLength, percentageHeight, endX, percentageHeight);
                    Line lineDup2 = new Line(currentCall.getStart()*zoomLevel + percentageLength, originalTrackHeight - percentageHeight, currentCall.getStart()*zoomLevel + currentCall.getLength()*zoomLevel - percentageLength, originalTrackHeight - percentageHeight);
                    Line lineDup3 = new Line(currentCall.getStart()*zoomLevel + percentageLength, percentageHeight, currentCall.getStart()*zoomLevel + percentageLength, originalTrackHeight - percentageHeight);
                    Line lineDup4 = new Line(currentCall.getStart()*zoomLevel + currentCall.getLength()*zoomLevel - percentageLength, percentageHeight, currentCall.getStart()*zoomLevel + currentCall.getLength()*zoomLevel - percentageLength, originalTrackHeight - percentageHeight);
                    lineDup1.setStroke(Color.rgb(40, 70, 160));
                    lineDup2.setStroke(Color.rgb(40, 70, 160));
                    lineDup3.setStroke(Color.rgb(40, 70, 160));
                    lineDup4.setStroke(Color.rgb(40, 70, 160));
                    currentCalls.getChildren().add(lineDup1);
                    currentCalls.getChildren().add(lineDup2);
                    currentCalls.getChildren().add(lineDup3);
                    currentCalls.getChildren().add(lineDup4);
                }
                else if (Objects.equals(currentCall.getType(), "INV")) {
                    callRect.setStroke(Color.rgb(200, 140, 0));
                    callRect.setOpacity(0.5);
                    callRect.setFill(Color.rgb(255, 195, 0 ));
                    Line lineInv1 = new Line(currentCall.getStart()*zoomLevel, 1, currentCall.getStart()*zoomLevel + currentCall.getLength()*zoomLevel, originalTrackHeight-2);
                    Line lineInv2 = new Line(currentCall.getStart()*zoomLevel + currentCall.getLength()*zoomLevel, 1, currentCall.getStart()*zoomLevel, originalTrackHeight-2);
                    lineInv1.setStroke(Color.rgb(200, 140, 0));
                    lineInv2.setStroke(Color.rgb(200, 140, 0));
                    lineInv1.setOpacity(0.6);
                    lineInv2.setOpacity(0.6);
                    currentCalls.getChildren().add(lineInv1);
                    currentCalls.getChildren().add(lineInv2);
                }
                else if (Objects.equals(currentCall.getType(), "DEL")) {
                    callRect.setStroke(Color.rgb(120, 30, 2));
                    callRect.setOpacity(0.5);
                    callRect.setFill(Color.rgb(164, 42, 4));
                    Line lineDel1 = new Line(currentCall.getStart()*zoomLevel, 1, currentCall.getStart()*zoomLevel + (currentCall.getLength()*zoomLevel / 2), originalTrackHeight-2);
                    Line lineDel2 = new Line(currentCall.getStart()*zoomLevel + (currentCall.getLength()*zoomLevel / 2), originalTrackHeight-2, currentCall.getStart()*zoomLevel + currentCall.getLength()*zoomLevel, 1);
                    lineDel1.setStroke(Color.rgb(120, 30, 2));
                    lineDel2.setStroke(Color.rgb(120, 30, 2));
                    lineDel1.setOpacity(0.4);
                    lineDel2.setOpacity(0.4);
                    currentCalls.getChildren().add(lineDel1);
                    currentCalls.getChildren().add(lineDel2);
                }
                else if (Objects.equals(currentCall.getType(), "INS")) {
                    callRect.setStroke(Color.rgb(100, 140, 80));
                    callRect.setOpacity(0.5);
                    callRect.setFill(Color.rgb(147, 197, 114));
                    Line lineIns1 = new Line(currentCall.getStart()*zoomLevel + (currentCall.getLength()*zoomLevel / 2), 1, currentCall.getStart()*zoomLevel, originalTrackHeight-2);
                    Line lineIns2 = new Line(currentCall.getStart()*zoomLevel + (currentCall.getLength()*zoomLevel / 2), 1, currentCall.getStart()*zoomLevel + currentCall.getLength()*zoomLevel, originalTrackHeight-2);
                    lineIns1.setStroke(Color.rgb(100, 140, 80));
                    lineIns2.setStroke(Color.rgb(100, 140, 80));
                    lineIns1.setOpacity(0.5);
                    lineIns2.setOpacity(0.5);
                    currentCalls.getChildren().add(lineIns1);
                    currentCalls.getChildren().add(lineIns2);
                }
                else if (Objects.equals(currentCall.getType(), "BND") || Objects.equals(currentCall.getType(), "TRA")){
                    callRect.setStroke(Color.BLACK);
                    callRect.setOpacity(0.7);
                    callRect.setFill(Color.BLACK);
                    callRect.getStrokeDashArray().setAll(12.0, 6.0);
                }
                currentCalls.getChildren().add(callRect);
                callRect.setOnMouseEntered(e -> {
                    callRect.setCursor(Cursor.HAND);
                });
                callRect.setOnMouseClicked(e -> {
                    callRect.setStroke(Color.BLACK);
                    this.showCallInformation(currentCall, samples);
                    this.setLiveCall(currentCall);
                    this.openSidePane();
                });
            }
        }
    }

    public void showTileCalls(Chromosome chromosome, ArrayList<Sample> samples, double zoomLevel, int originalTrackHeight, int startInterval, int endInterval) {
        /**
         * Pre-conditions/assumptions: Gets call pane for each sample by looking up the ID
         */
        this.samplePanel.setMinWidth(chromosome.getLength() * zoomLevel);
        this.samplePanel.setMaxWidth(chromosome.getLength() * zoomLevel);

        // loops through each sample and gets the sample pane to update, access order does not matter
        for (Sample sample : samples) {
            ArrayList<String> seenIds = new ArrayList<>();
            Pane currentCalls = (Pane) this.samplesContainer.lookup("#" + sample.getName());
            currentCalls.getChildren().clear();
            currentCalls.setMinWidth(chromosome.getLength() * zoomLevel);
            currentCalls.setPrefWidth(chromosome.getLength() * zoomLevel);
            currentCalls.setMaxWidth(chromosome.getLength() * zoomLevel);
            System.out.println("START AND END INTERVAL : " + startInterval + " , " + endInterval);
            // loop through each tile
            for (int i=startInterval; i<=endInterval; i++) {
                System.out.println("I IS " + i);
                // for each call
                ArrayList<Call> calls = sample.getTiledCalls().get(chromosome.getName()).get(i);
                if (calls == null) {
                    System.err.println("NULL for key: " + chromosome.getName() + " " + " interval " + i);
                }
                for (Call currentCall : sample.getTiledCalls().get(chromosome.getName()).get(i)) {
                    // already seen this call and added it
                    if (seenIds.contains(currentCall.getId())) {
                        // do nothing
                    }
                    // haven't added this call in any other tiles for this sample yet
                    else {
                        seenIds.add(currentCall.getId());
                        Rectangle callRect = new Rectangle(currentCall.getStart()*zoomLevel, 1, currentCall.getLength()*zoomLevel, originalTrackHeight-2);
                        Polygon traPoly = new Polygon();
                        // styling
                        callRect.setOpacity(1);
                        callRect.setStrokeWidth(2);
                        callRect.setArcWidth(5);   // horizontal roundness
                        callRect.setArcHeight(5);
                        if (Objects.equals(currentCall.getType(), "DUP")) {
                            callRect.setStroke(Color.rgb(40, 70, 160));
                            callRect.setOpacity(0.5);
                            callRect.setFill(Color.rgb(65, 105, 225));
                            // if the duplication rectangle is currently big enough to show the inside lines, show
                            // inset is the distance from inner border to outer border
                            int inset = 5;
                            if (currentCall.getLength()*zoomLevel > (inset*2) && originalTrackHeight > (inset*2)) {
                                // lines for inner border
                                double endX = currentCall.getStart()*zoomLevel + currentCall.getLength()*zoomLevel - inset;
                                Line lineDup1 = new Line(currentCall.getStart()*zoomLevel + inset, inset, endX, inset);
                                Line lineDup2 = new Line(currentCall.getStart()*zoomLevel + inset, originalTrackHeight - inset, endX, originalTrackHeight - inset);
                                Line lineDup3 = new Line(currentCall.getStart()*zoomLevel + inset, inset, currentCall.getStart()*zoomLevel + inset, originalTrackHeight - inset);
                                Line lineDup4 = new Line(endX, inset, endX, originalTrackHeight - inset);
                                lineDup1.setStroke(Color.rgb(40, 70, 160));
                                lineDup2.setStroke(Color.rgb(40, 70, 160));
                                lineDup3.setStroke(Color.rgb(40, 70, 160));
                                lineDup4.setStroke(Color.rgb(40, 70, 160));
                                lineDup1.setOpacity(0.5);
                                lineDup2.setOpacity(0.5);
                                lineDup3.setOpacity(0.5);
                                lineDup4.setOpacity(0.5);
                                currentCalls.getChildren().add(lineDup1);
                                currentCalls.getChildren().add(lineDup2);
                                currentCalls.getChildren().add(lineDup3);
                                currentCalls.getChildren().add(lineDup4);
                                // lines for duplication-insert portion
                                double middleDupX = ((currentCall.getStart()*zoomLevel + inset)+endX)/2;
                                Line lineDup5 = new Line(currentCall.getStart()*zoomLevel + inset, originalTrackHeight - inset, middleDupX, inset);
                                Line lineDup6 = new Line(middleDupX, inset, endX, originalTrackHeight - inset);
                                lineDup5.setStroke(Color.rgb(40, 70, 160));
                                lineDup6.setStroke(Color.rgb(40, 70, 160));
                                lineDup5.setOpacity(0.5);
                                lineDup6.setOpacity(0.5);
                                currentCalls.getChildren().add(lineDup5);
                                currentCalls.getChildren().add(lineDup6);
                            }
                            // otherwise, don't add additional lines
                            else {
                                // do nothing!
                            }
                        }
                        else if (Objects.equals(currentCall.getType(), "INV")) {
                            callRect.setStroke(Color.rgb(200, 140, 0));
                            callRect.setOpacity(0.5);
                            callRect.setFill(Color.rgb(255, 195, 0 ));
                            Line lineInv1 = new Line(currentCall.getStart()*zoomLevel, 1, currentCall.getStart()*zoomLevel + currentCall.getLength()*zoomLevel, originalTrackHeight-2);
                            Line lineInv2 = new Line(currentCall.getStart()*zoomLevel + currentCall.getLength()*zoomLevel, 1, currentCall.getStart()*zoomLevel, originalTrackHeight-2);
                            lineInv1.setStroke(Color.rgb(200, 140, 0));
                            lineInv2.setStroke(Color.rgb(200, 140, 0));
                            lineInv1.setOpacity(0.6);
                            lineInv2.setOpacity(0.6);
                            currentCalls.getChildren().add(lineInv1);
                            currentCalls.getChildren().add(lineInv2);
                        }
                        else if (Objects.equals(currentCall.getType(), "DEL")) {
                            callRect.setStroke(Color.rgb(120, 30, 2));
                            callRect.setOpacity(0.5);
                            callRect.setFill(Color.rgb(164, 42, 4));
                            Line lineDel1 = new Line(currentCall.getStart()*zoomLevel, 1, currentCall.getStart()*zoomLevel + (currentCall.getLength()*zoomLevel / 2), originalTrackHeight-2);
                            Line lineDel2 = new Line(currentCall.getStart()*zoomLevel + (currentCall.getLength()*zoomLevel / 2), originalTrackHeight-2, currentCall.getStart()*zoomLevel + currentCall.getLength()*zoomLevel, 1);
                            lineDel1.setStroke(Color.rgb(120, 30, 2));
                            lineDel2.setStroke(Color.rgb(120, 30, 2));
                            lineDel1.setOpacity(0.4);
                            lineDel2.setOpacity(0.4);
                            currentCalls.getChildren().add(lineDel1);
                            currentCalls.getChildren().add(lineDel2);
                        }
                        else if (Objects.equals(currentCall.getType(), "INS")) {
                            callRect.setStroke(Color.rgb(100, 140, 80));
                            callRect.setOpacity(0.5);
                            callRect.setFill(Color.rgb(147, 197, 114));
                            Line lineIns1 = new Line(currentCall.getStart()*zoomLevel + (currentCall.getLength()*zoomLevel / 2), 1, currentCall.getStart()*zoomLevel, originalTrackHeight-2);
                            Line lineIns2 = new Line(currentCall.getStart()*zoomLevel + (currentCall.getLength()*zoomLevel / 2), 1, currentCall.getStart()*zoomLevel + currentCall.getLength()*zoomLevel, originalTrackHeight-2);
                            lineIns1.setStroke(Color.rgb(100, 140, 80));
                            lineIns2.setStroke(Color.rgb(100, 140, 80));
                            lineIns1.setOpacity(0.5);
                            lineIns2.setOpacity(0.5);
                            currentCalls.getChildren().add(lineIns1);
                            currentCalls.getChildren().add(lineIns2);
                        }
                        else if (Objects.equals(currentCall.getType(), "BND") || Objects.equals(currentCall.getType(), "TRA")) {
                            // direction at first character, means join before
                            if (currentCall.getAlternate().charAt(0) ==  ']' || currentCall.getAlternate().charAt(0) == '[') {
                                // joining sequence is in reverse direction
                                if (currentCall.getAlternate().charAt(0) ==  ']') {
                                    traPoly.getPoints().addAll(
                                            currentCall.getStart()*zoomLevel, 1.0,
                                            currentCall.getStart()*zoomLevel, (double) originalTrackHeight /3,
                                            currentCall.getStart()*zoomLevel-7, (double) (originalTrackHeight / 3) /2
                                    );
                                }
                                // joining sequence is in forward direction
                                else {
                                    traPoly.getPoints().addAll(
                                            currentCall.getStart()*zoomLevel-7, 1.0,
                                            currentCall.getStart()*zoomLevel-7, (double) originalTrackHeight /3,
                                            currentCall.getStart()*zoomLevel, (double) (originalTrackHeight / 3) /2
                                    );
                                }
                            }
                            // otherwise other sequence joined after this ref char
                            else {
                                // joining sequence is in the reverse direction
                                if (currentCall.getAlternate().charAt(currentCall.getAlternate().length() - 1) ==  ']') {
                                    traPoly.getPoints().addAll(
                                            currentCall.getStart()*zoomLevel+7, 1.0,
                                            currentCall.getStart()*zoomLevel+7, (double) originalTrackHeight /3,
                                            currentCall.getStart()*zoomLevel, (double) (originalTrackHeight / 3) /2
                                    );
                                }
                                // joining sequence is in the forward direction
                                else {
                                    traPoly.getPoints().addAll(
                                            currentCall.getStart()*zoomLevel, 1.0,
                                            currentCall.getStart()*zoomLevel, (double) originalTrackHeight /3,
                                            currentCall.getStart()*zoomLevel+7, (double) (originalTrackHeight / 3) /2
                                    );
                                }
                            }
                            callRect.setStroke(Color.BLACK);
                            callRect.setOpacity(0.7);
                            callRect.setFill(Color.BLACK);
                            callRect.getStrokeDashArray().setAll(12.0, 6.0);
                            callRect.setStrokeWidth(3);
                        }
                        // for call rectangle
                        currentCalls.getChildren().add(callRect);
                        callRect.setOnMouseEntered(e -> {
                            callRect.setCursor(Cursor.HAND);
                        });
                        callRect.setOnMouseClicked(e -> {
                            callRect.setStroke(Color.BLACK);
                            this.showCallInformation(currentCall, samples);
                            this.setLiveCall(currentCall);
                            this.openSidePane();
                        });
                        // for TRA polygon
                        traPoly.setFill(Color.rgb(80, 80, 80));
                        currentCalls.getChildren().add(traPoly);
                        traPoly.setOnMouseEntered(e -> {
                            traPoly.setCursor(Cursor.HAND);
                        });
                        traPoly.setOnMouseClicked(e -> {
                            this.showCallInformation(currentCall, samples);
                            this.setLiveCall(currentCall);
                            this.openSidePane();
                        });
                    }
                }
            }
        }
    }

    void setLiveCall(Call call) {
        this.liveCall = call;
    }

    Call getLiveCall() {
        return this.liveCall;
    }

    void showCallInformation(Call call, ArrayList<Sample> samples) {
        Label typeLabel = (Label) callInfoSideContainer.lookup("#type");
        typeLabel.setText(call.getType());
        Label chromLabel = (Label) callInfoSideContainer.lookup("#chrom");
        chromLabel.setText(call.getChromosome());
        Label posLabel = (Label) callInfoSideContainer.lookup("#pos");
        posLabel.setText(String.valueOf(String.format("%,d", call.getStart())));
        Label idLabel = (Label) callInfoSideContainer.lookup("#id");
        idLabel.setText(call.getId());

        // try to look up mate container in side pane
        HBox bndContainer = (HBox) callInfoSideContainer.lookup("#bndContainer");
        // if call is a breakend type, check if it is present and add it if necessary
        if (Objects.equals(call.getType(), "BND") || Objects.equals(call.getType(), "TRA")) {
            // if not present, add node
            if (bndContainer == null) {
                callInfoSideContainer.getChildren().add(7, mateContainer);
            }
            // otherwise present
            else {
                // do nothing
            }
            Label mateLabel = (Label) callInfoSideContainer.lookup("#mate");
            mateLabel.setText(call.getAlternate());
        }
        // call is not breakend type, make sure mate container node is removed
        else {
            // if not null already, remove it
            if (bndContainer != null) {
                callInfoSideContainer.getChildren().remove(bndContainer);
            }
        }

        Label genotypeLabel = (Label) callInfoSideContainer.lookup("#genotypes");
        // clear genotype label from previous calls
        genotypeLabel.setText("");
        int count = 0;
        for (Sample sample : samples) {
            if (count == 0) {
                genotypeLabel.setText(sample.getName() + " = " + call.getGenotypes().get(sample.getName()));
            }
            else {
                genotypeLabel.setText(genotypeLabel.getText() + "\n" + sample.getName() + " = " + call.getGenotypes().get(sample.getName()));
            }
            count += 1;
        }

        // set call info side pane to visible
        callInfoSideContainer.setVisible(true);
        callInfoSideContainer.setManaged(true);
        selectionOptionsSideContainer.setVisible(false);
        selectionOptionsSideContainer.setManaged(false);
    }

    /**
     * Calculate the appropriate zoom level to show entire reference at startup
     * @param refLength int length of the entire reference
     * @return the zoom level
     */
    public double initZoomWG(int refLength) {
        this.ticksWrapper.getChildren().clear();
        // calculate zoom level such that whole genome is in view
        return callsPanel.getViewportBounds().getWidth() / refLength;
    }

    public void showCoords(Chromosome chromosome, int tickSpacing, double zoomLevel, LinkedHashMap<String, Chromosome> refContigs) {
        this.ticksWrapper.getChildren().clear();
        // display ticks for chromosome names
        if (Objects.equals(chromosome.getName(), "<ALL>")) {
            for (Map.Entry<String, Chromosome> refContig : refContigs.entrySet()) {
                if (Objects.equals(refContig.getKey(), "<ALL>")) {
                    // do nothing
                }
                else {
                    double pos = refContig.getValue().getAbsoluteStart();
                    Text text = new Text(String.valueOf(refContig.getValue().getName()));
                    double textWidth = text.getLayoutBounds().getWidth();
                    text.setX((pos*zoomLevel) - textWidth / 2);
                    text.setY(25);
                    // tick
                    Line tick = new Line(pos*zoomLevel, 30, pos*zoomLevel, 40);
                    // add to pane
                    this.ticksWrapper.getChildren().add(tick);
                    this.ticksWrapper.getChildren().add(text);
                }
            }
        }
        // display ticks for increment
        else {
            for (int x = 0; x <= chromosome.getLength(); x += tickSpacing) {
                Text text = new Text(String.valueOf(x));
                // if tick spacing is greater than 100,000 bp (100 kb), truncate
                if (tickSpacing >= 100000) {
                    double truncatedX = (double) x / 1000000;
                    if (truncatedX % 1 == 0) {
                        text = new Text(String.format("%.0f", truncatedX) + " Mb");
                    }
                    else {
                        text = new Text(String.format("%.1f", truncatedX) + " Mb");
                    }
                }
                // otherwise, do not truncate, but add commas
                else {
                    text = new Text(String.format("%,d", x) + " bp");
                }

                double textWidth = text.getLayoutBounds().getWidth();
                text.setX((x*zoomLevel) - textWidth / 2);
                text.setY(25);
                // tick
                Line tick = new Line(x*zoomLevel, 30, x*zoomLevel, 40);
                // add to pane
                this.ticksWrapper.getChildren().add(tick);
                this.ticksWrapper.getChildren().add(text);
            }
        }
    }


    /**
     * Gets the width of the vertical scrollbar (on Scrollpane callsPanel), this method does not assume
     * the scrollbar is visible it can be called if visible or not
     *
     * @return the width if visible, or 0 if not visible or null
     */
    public double getVerticalSBWidth() {
        ScrollBar vBar = (ScrollBar) this.callsPanel.lookup(".scroll-bar:vertical");
        if (vBar == null || !vBar.isVisible()) {
            return 0;
        }
        else {
            return vBar.getWidth();
        }
    }

    /**
     * Checks if the vertical scrollbar (on Scrollpane callsPanel) is visible or not
     *
     * @return true if visible, false otherwise
     */
    public boolean isVerticalSBVisible() {
        ScrollBar vBar = (ScrollBar) this.callsPanel.lookup(".scroll-bar:vertical");
        return vBar.isVisible();
    }

    // *************************************************************** TRACK CREATION FUNCTIONS ************************************************************************

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
            // set id
            callsWrapper.setId("AlleleFreq");


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
                    new Stop(0.0, Color.rgb(199, 92, 92, 0.8)),    // red from 0%...
                    new Stop(0.05, Color.rgb(199, 92, 92, 0.8)),   // ...to 5%
                    new Stop(0.25, Color.rgb(92, 156, 92, 0.6)),  // green at center
                    new Stop(0.75, Color.rgb(92, 156, 92, 0.6)),  // green at center
                    new Stop(0.95, Color.rgb(199, 92, 92, 0.8)),   // red starts again at 95%
                    new Stop(1.0, Color.rgb(199, 92, 92, 0.8))     // red to bottom
            );
            BackgroundFill bgFill = new BackgroundFill(lg, CornerRadii.EMPTY, Insets.EMPTY);
            callsWrapper.setBackground(new Background(bgFill));
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

        // create infoContainer to hold all sample graphics and info
        HBox infoContainer = new HBox();
        infoContainer.setAlignment(Pos.CENTER);
        infoContainer.setMinHeight(height);
        infoContainer.setMaxHeight(height);

        // lock container to hold lock if applicable
        VBox lockContainer = new VBox();

        // label container to hold label and color rect if applicable
        VBox labelContainer = new VBox();
        labelContainer.setAlignment(Pos.CENTER);
        labelWrapper.getChildren().add(sampleLabel);
        labelContainer.getChildren().add(labelWrapper);

        infoContainer.getChildren().addAll(lockContainer, labelContainer);
        this.samplesInfoContainer.getChildren().add(infoContainer);
        // add sampContainer to samplesContainer
        this.samplesContainer.getChildren().add(callsWrapper);
    }

    public void createNewCallTrack(HashMap<String, Color> sampleColors, int refLength, double zoomLevel, String sampleName, double baseFontSize, int height) {
        // create callsWrapper to hold sample calls
        Pane callsWrapper = new Pane();
        callsWrapper.setPrefWidth(refLength * zoomLevel);
        // force height of track (or else it will collapse if there is no content)
        callsWrapper.setMinHeight(height);
        callsWrapper.setMaxHeight(height);
        // create labelWrapper Pane to hold sample name
        StackPane labelWrapper = new StackPane();
        labelWrapper.setMinWidth(this.sampleSpaceWidth * 0.7);
        labelWrapper.setMaxWidth(this.sampleSpaceWidth * 0.7);
        // create sample label
        Label sampleLabel = new Label(sampleName);
        sampleLabel.setFont(Font.font("System", baseFontSize));

        // create infoContainer to hold all sample info and graphics
        HBox infoContainer = new HBox();
        infoContainer.setAlignment(Pos.CENTER);
        infoContainer.setMinHeight(height);
        infoContainer.setMaxHeight(height);

        // lock container to hold lock if applicable
        VBox lockContainer = new VBox();
        SVGPath lockIcon = new SVGPath();
        lockIcon.setContent(
                "M20 12.0078 Q20 11.1797 19.4062 10.6016 Q18.8281 10.0078 18 10.0078 L17 10.0078 L17 7.0078 Q17 5.9766 16.625 5.0703 Q16.2188 4.1484 15.5312 3.4766 Q14.8594 2.7891 13.9531 2.3984 Q13.0312 1.9922 12 1.9922 Q10.9688 1.9922 10.0625 2.3984 Q9.1406 2.7891 8.4531 3.4766 Q7.7812 4.1484 7.3906 5.0703 Q7.0156 5.9766 7.0156 7.0078 L7.0156 10.0078 L6 10.0078 Q5.1875 10.0078 4.5938 10.6016 Q4.0156 11.1797 4.0156 12.0078 L4.0156 19.9922 Q4.0156 20.8359 4.5938 21.4297 Q5.1875 22.0078 6 22.0078 L18 22.0078 Q18.8281 22.0078 19.4062 21.4297 Q20 20.8359 20 19.9922 L20 12.0078 ZM9 7.0078 Q9 5.7734 9.875 4.8984 Q10.7656 4.0078 12 4.0078 Q13.25 4.0078 14.125 4.8984 Q15 5.7734 15 7.0078 L15 10.0078 L9 10.0078 L9 7.0078 Z"
        );
        lockIcon.setFill(Color.TRANSPARENT);
        lockIcon.setScaleX(0.75);
        lockIcon.setScaleY(0.75);
        lockIcon.setId("lock");
        lockContainer.getChildren().add(lockIcon);
        lockContainer.setAlignment(Pos.CENTER_RIGHT);
        lockContainer.setMinWidth(this.sampleSpaceWidth * 0.3);
        lockContainer.setMinWidth(this.sampleSpaceWidth * 0.3);

        // label container to hold label and color rect if applicable
        VBox labelContainer = new VBox();
        labelContainer.setAlignment(Pos.CENTER);
        labelWrapper.getChildren().add(sampleLabel);
        Rectangle colorRect = new Rectangle(40, 2.5, sampleColors.get(sampleName));
        labelContainer.getChildren().addAll(labelWrapper, colorRect);

        infoContainer.getChildren().addAll(lockContainer, labelContainer);
        this.samplesInfoContainer.getChildren().add(infoContainer);
        // add sampContainer to samplesContainer
        this.samplesContainer.getChildren().add(callsWrapper);
        // set IDs
        infoContainer.setId(sampleName);
        callsWrapper.setId(sampleName);
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

    public double setScroll(int start, Chromosome chrom, double zoomLevel) {
        //System.out.println("SET SCROLL PERCENT IS " + percent);
        this.callsPanel.layout();
        double contentWidth = callsPanel.getContent().getBoundsInLocal().getWidth();
        double viewportWidth = callsPanel.getViewportBounds().getWidth();
        double maxScroll = contentWidth - viewportWidth;

        double scale = contentWidth / chrom.getLength();
        double targetPixelX = start * scale;
        double hvalue = targetPixelX / maxScroll;
        this.callsPanel.setHvalue(hvalue);
        return hvalue;
    }

    public double getHValue() {
        return this.callsPanel.getHvalue();
    }

    public void cleanUnusedNodes(Chromosome chromosome, int start, int end) {

    }

    public double getStartFromHVal(double hval, Chromosome currentChrom) {
        double contentWidth = callsPanel.getContent().getBoundsInLocal().getWidth();
        double viewportWidth = callsPanel.getViewportBounds().getWidth();
        double maxScroll = contentWidth - viewportWidth;
        double start = hval * maxScroll * currentChrom.getLength() / contentWidth;
        return start;
    }

    public double getContentWidth() {
        return callsPanel.getContent().getBoundsInLocal().getWidth();
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

    /**
     *
     * @param chromosome
     * @param zoomLevel
     * @param length
     */
    public void updateMarkerWidth(Chromosome chromosome, double zoomLevel, double length) {
        callsPanel.layout();
        double visibleWidth = length * zoomLevel;
        double contentWidth = chromosome.getLength() * zoomLevel;
        double proportionVisible = visibleWidth/contentWidth;
        // set width
        marker.setWidth(markerWrapper.getWidth() * proportionVisible);
        //marker.setWidth(chromosome.getPixelWidth() * proportionVisible);
    }

    /**
     *
     * @param chromosome
     * @param offset in pixels
     */
    public void updateMarkerPos(Chromosome chromosome, double offset) {
        //marker.setLayoutX(chromosome.getPixelAbsoluteOffset() + offset);
        marker.setLayoutX(offset);
    }

    public double getMarkerWrapperWidth() {
        return this.markerWrapper.getWidth();
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
            HBox container = (HBox) samplesInfoContainer.lookup("#" + sample.getName());
            container.setMinHeight(originalTrackHeight * trackHeightScale);
            container.setMaxHeight(originalTrackHeight * trackHeightScale);
            // get second vbox (labelcontainer)
            VBox labelContainer = (VBox) container.getChildren().get(1);
            // get the labelwrapper
            Pane labelWrapper = (Pane) labelContainer.getChildren().getFirst();
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

    public void updateSelections(ArrayList<Selection> selections, double zoomLevel) {
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

    public void toggleSampleLock(Sample sample) {
        HBox visualContainer = (HBox) this.samplesInfoContainer.lookup("#" + sample.getName());
        SVGPath lock = (SVGPath) visualContainer.lookup("#lock");
        if (lock.getFill().equals(Color.TRANSPARENT)) {
            lock.setFill(Color.rgb(105, 105, 105));
        }
        else {
            lock.setFill(Color.TRANSPARENT);
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
        HBox container = (HBox) this.samplesInfoContainer.lookup("#" + sample.getName());
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


    public boolean openSidePane() {
        // if its closed (300), open it
        if (this.sidePaneContainer.getTranslateX() > 0) {
            slideIn.setToX(0);
            slideIn.play();
            return true;
        } else {
            return false;
        }
    }

    public boolean closeSidePane() {
        // if its visible (0), close it
        if (this.sidePaneContainer.getTranslateX() == 0) {
            slideOut.setToX(300);
            // going to set selection info as present (default)
            slideOut.setOnFinished(event -> {
                selectionOptionsSideContainer.setVisible(true);
                selectionOptionsSideContainer.setManaged(true);
                callInfoSideContainer.setVisible(false);
                callInfoSideContainer.setManaged(false);
            });
            slideOut.play();
            return true;
        }
        // otherwise do nothing because already closed
        else {
            return false;
        }
    }

    public void updateMarkerOnDrag(MouseEvent event) {
        /**
         * Deal with moving
         */
        double mouseX = event.getSceneX();

        // Convert scene X to local X of the markerPane
        double localX = markerWrapper.sceneToLocal(mouseX, 0).getX();

        double startX = 0;
        double endX = markerWrapper.getWidth();
        double clampedX = Math.max(startX, Math.min(localX, endX));
        if (localX >= (endX - marker.getWidth())) {
            marker.setLayoutX(clampedX-marker.getWidth());
        }
        else {
            marker.setLayoutX(clampedX);
        }

        double percent = (clampedX - startX) / (endX - startX);
        System.out.printf("Marker at X: %.2f (%.1f%%)%n", clampedX, percent * 100);
        this.callsPanel.setHvalue(percent);
    }

    public String getTextFieldRegion() {
        return this.regionField.getText();
    }

    public void clearRegionField() {
        this.regionField.clear();
        regionField.getParent().requestFocus();
    }

    public void showInvalidRegionAlert(String regionText) {
        // create and show alert for invalid region
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Invalid region");
        alert.setHeaderText(null);
        alert.setContentText(regionText + " is an invalid region.");
        alert.showAndWait();
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
    public void chromComboBoxListener(EventHandler<ActionEvent> handler) {
        chromComboBox.setOnAction(handler);
    }
    public void processRegionButtonListener(EventHandler<ActionEvent> handler) {
        processRegionButton.setOnAction(handler);
    }
    public void showMateButtonListener(EventHandler<ActionEvent> handler) {
        showMateButton.setOnAction(handler);
    }

//    public void viewportWidthChange(EventHandler<ActionEvent> handler) {
//        callsPanel.viewportBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
//            if (oldBounds == null || newBounds == null || oldBounds.getWidth() != newBounds.getWidth()) {
//                handler.handle(new ActionEvent(this, null));
//            }
//        });
//    }

    public void scrollChange(ChangeListener<Number> listener) {
        this.callsPanel.hvalueProperty().addListener(listener);
    }

    public void markerDragged(EventHandler<MouseEvent> handler) {
        marker.setOnMouseDragged(handler);
    }
}
