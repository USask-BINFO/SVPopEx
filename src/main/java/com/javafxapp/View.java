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
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.stage.Screen;


import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private ArrayList<Sample> sampleOrder = new ArrayList<Sample>();
    // ----------- root and side pane ---------------
    private VBox sidePaneContainer = new VBox(0);
    private VBox selectionOptionsSideContainer = new VBox(20);
    private VBox callInfoSideContainer = new VBox(7);
    private Call liveCall = null;
    private StackPane sidePaneSwapPanel = new StackPane(callInfoSideContainer, selectionOptionsSideContainer);
    private ArrayList<CheckBox> pinCheckboxes = new ArrayList<>();
    private VBox compareLabelContainer = new VBox();
    private VBox processRegionLabelContainer = new VBox();
    private HBox mateContainer = new HBox();
    private Button showMateButton = new Button("Show");
    private VBox comparators = new VBox();
    private Button closeSidePaneButton = new Button("\u00D7");
    private HBox closeButtonContainer = new HBox(closeSidePaneButton);
    private final VBox layout = new VBox(3);
    private StackPane root = new StackPane(layout, sidePaneContainer);
    private final Stage primaryStage;
    private TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), sidePaneContainer);
    private TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), sidePaneContainer);
    // ---------- menuBar -------------
    private final MenuBar menuBar = new MenuBar();
    private final Menu fileMenu = new Menu("File");
    private final MenuItem importVCFItem = new MenuItem("Import VCF");
    private final MenuItem importGFFItem = new MenuItem("Import GFF3");
    private final Menu viewMenu = new Menu("View");
    private final Menu colorThemesMenu = new Menu("Color Themes");
    private final Menu AFColorThemesMenu = new Menu("Allele Frequency Track");
    private final Menu SVGlyphThemesMenu = new Menu("SV Glyphs");
    private final ToggleGroup AFThemesGroup = new ToggleGroup();
    private final RadioMenuItem grayscaleAFTrackItem = new RadioMenuItem("Grayscale");
    private final ToggleGroup SVGlyphThemesGroup = new ToggleGroup();
    private final RadioMenuItem defaultSVGlyphColorItem = new RadioMenuItem("Default");
    private final RadioMenuItem colorblindSVGlyphItem = new RadioMenuItem("Colorblind Friendly");
    private final HashMap<String, HashMap<String, HashMap<String, Color>>> SVGlyphColorThemes = new HashMap<>();
    private String currentSVGlyphTheme = "default";
    // ------- referenceContainer ----------
    private final VBox referenceContainer = new VBox(5);
    private Rectangle marker = new Rectangle(0,0,0,50);
    private final Pane referenceWrapper = new Pane();
    private final Pane markerWrapper = new Pane();
    private Label l1 = new Label("");
    private Label l2 = new Label("");
    private VBox labelsBox = new VBox(10, l1, l2);
    private StackPane rectangleWithLabels = new StackPane(referenceWrapper, labelsBox);
    private StackPane rectWithMarker = new StackPane(rectangleWithLabels, markerWrapper);
    // ---------- tickContainer ----------
    private SVGPath handIcon = new SVGPath();
    private final HBox tickContainer = new HBox();
    private final Pane spaceWrapper = new Pane();
    private final Pane ticksWrapper = new Pane();
    private EventHandler<MouseEvent> releaseSelectionHandler;
    private final int sampleSpaceWidth = 90;
    // ----------- logoContainer ------------------
    private final HBox logoContainer = new HBox();
    private Rectangle logoRectangle = new Rectangle(158, 61);
    private Image logoImage = new Image("file:./src/main/resources/com/javafxapp/logo.png",
            0, 0,
            true,
            true);
    private ImagePattern imagePattern = new ImagePattern(
            logoImage
    );
    // ----------- control container --------
    private GridPane controlWrapper = new GridPane();
    private ColumnConstraints controlCol0 = new ColumnConstraints();
    private ColumnConstraints controlCol1 = new ColumnConstraints();
    private ColumnConstraints controlCol2 = new ColumnConstraints();
    // nav container
    private final HBox navContainer = new HBox(10);
    private TextField regionField = new TextField();
    public ComboBox<String> chromComboBox = new ComboBox<>();
    private Button processRegionButton = new Button("Go");
    // controls
    private final HBox generalControlContainer = new HBox();
    private Region generalControlSpacer = new Region();
    private Button zoomInButton = new Button("Zoom +");
    private Button zoomOutButton = new Button("Zoom -");
    private Button processButton = new Button("Color by Haplotype");
    private Button showSameButton = new Button("Show Same Calls");
    private Button showDiffButton = new Button("Show Diff Calls");
    private Button shrinkTrackHeightButton = new Button("- Height");
    private Button growTrackHeightButton = new Button("+ Height");
    // ------------ selectionControlContainer ------------------
    private final HBox selectionControlContainer = new HBox();
    private Region selectionControlSpacer = new Region();
    private Button clearButton = new Button("Clear");
    private Button sidePaneButton = new Button("Comparative Options");
    // ---------- callsPanel ----------
    private double scrollViewportHeight = 0;
    private final HBox callsContentContainer = new HBox();
    // information (left hand side)
    private final VBox annotationsInfoContainer = new VBox(0);
    private final VBox samplesInfoContainer = new VBox(0);
    private final VBox tracksInfoContainer = new VBox(0, annotationsInfoContainer, samplesInfoContainer);
    private final ScrollPane tracksInfoPanel = new ScrollPane(tracksInfoContainer);

    // calls (right hand side)
    private final VBox annotationsContainer = new VBox(0);
    private final VBox samplesContainer = new VBox(0);
    private final VBox tracksContainer = new VBox(0, annotationsContainer, samplesContainer);

    private final HBox selectionContainer = new HBox();
    private final Pane selectionWrapper = new Pane();
    private final StackPane tracksPanel = new StackPane(tracksContainer, selectionContainer);
    private VBox tracksGroup = new VBox(tracksPanel);

    private final ScrollPane callsPanel = new ScrollPane(tracksGroup);
    private final HashMap<String, ArrayList<Node>> nodeGroups = new HashMap<>();


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
        // ----------- SET GLYPH COLOR THEMES -----------
        // default
        this.SVGlyphColorThemes.put("default", new HashMap<>());
        this.SVGlyphColorThemes.get("default").put("INV", new HashMap<>());
        this.SVGlyphColorThemes.get("default").put("DUP", new HashMap<>());
        this.SVGlyphColorThemes.get("default").put("INS", new HashMap<>());
        this.SVGlyphColorThemes.get("default").put("DEL", new HashMap<>());
        this.SVGlyphColorThemes.get("default").get("INV").put("fill", Color.rgb(255, 195, 0 ));
        this.SVGlyphColorThemes.get("default").get("INV").put("stroke", Color.rgb(200, 140, 0));
        this.SVGlyphColorThemes.get("default").get("DUP").put("fill", Color.rgb(65, 105, 225));
        this.SVGlyphColorThemes.get("default").get("DUP").put("stroke", Color.rgb(40, 70, 160));
        this.SVGlyphColorThemes.get("default").get("INS").put("fill", Color.rgb(147, 197, 114));
        this.SVGlyphColorThemes.get("default").get("INS").put("stroke", Color.rgb(100, 140, 80));
        this.SVGlyphColorThemes.get("default").get("DEL").put("fill", Color.rgb(164, 42, 4));
        this.SVGlyphColorThemes.get("default").get("DEL").put("stroke", Color.rgb(120, 30, 2));
        // colorblind friendly
        this.SVGlyphColorThemes.put("colorblind", new HashMap<>());
        this.SVGlyphColorThemes.get("colorblind").put("INV", new HashMap<>());
        this.SVGlyphColorThemes.get("colorblind").put("DUP", new HashMap<>());
        this.SVGlyphColorThemes.get("colorblind").put("INS", new HashMap<>());
        this.SVGlyphColorThemes.get("colorblind").put("DEL", new HashMap<>());
        this.SVGlyphColorThemes.get("colorblind").get("INV").put("fill", Color.rgb(249, 214, 44));
        this.SVGlyphColorThemes.get("colorblind").get("INV").put("stroke", Color.rgb(199, 171, 35));
        this.SVGlyphColorThemes.get("colorblind").get("DUP").put("fill", Color.rgb(113, 111, 111));
        this.SVGlyphColorThemes.get("colorblind").get("DUP").put("stroke", Color.rgb(90, 89, 89));
        this.SVGlyphColorThemes.get("colorblind").get("INS").put("fill", Color.rgb(30, 136, 229));
        this.SVGlyphColorThemes.get("colorblind").get("INS").put("stroke", Color.rgb(24, 109, 183));
        this.SVGlyphColorThemes.get("colorblind").get("DEL").put("fill", Color.rgb(150, 38, 22));
        this.SVGlyphColorThemes.get("colorblind").get("DEL").put("stroke", Color.rgb(120, 30, 18));


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
        Label title = new Label("Comparative Options");
        String style = """
               -fx-font-size: 18px;
               -fx-text-fill: #555555;
               -fx-font-weight: bold;
               """;
        String noteStyle = """
                -fx-text-fill: #6f6f6f;
                -fx-font-size: 10px;
                -fx-font-style: italic;
                """;
        title.setStyle(style);
        Region spacer1 = new Region();
        spacer1.setMinHeight(15);
        Region spacer2 = new Region();
        spacer2.setMinHeight(20);
        selectionOptionsSideContainer.setAlignment(Pos.TOP_CENTER);

        selectionOptionsSideContainer.getChildren().add(spacer1);
        selectionOptionsSideContainer.getChildren().add(title);
        Separator separator1 = new Separator();
        selectionOptionsSideContainer.getChildren().add(separator1);

        // pin checkboxes
        selectionOptionsSideContainer.getChildren().add(comparators);
        // plots
        compareLabelContainer.setPadding(new Insets(0, 0, 0, 20));
        Label compareLabel = new Label("Compare:");
        compareLabel.setStyle("""
                -fx-text-fill: #555555;
                -fx-font-weight: bold;
                -fx-font-size: 14px;
                """);
        Label selectRegionLabel1 = new Label("*region must be selected");
        selectRegionLabel1.setStyle(noteStyle);
        Label selectRegionLabel2 = new Label("*region must be selected");
        selectRegionLabel2.setStyle(noteStyle);
        compareLabelContainer.getChildren().add(compareLabel);
        compareLabelContainer.getChildren().add(selectRegionLabel1);
        processRegionLabelContainer.setPadding(new Insets(0, 0, 0, 20));
        Label regionLabel = new Label("Process Region:");
        regionLabel.setStyle("""
                -fx-text-fill: #555555;
                -fx-font-weight: bold;
                -fx-font-size: 14px;
                """);
        processRegionLabelContainer.getChildren().add(regionLabel);
        processRegionLabelContainer.getChildren().add(selectRegionLabel2);
        selectionOptionsSideContainer.getChildren().add(compareLabelContainer);
        selectionOptionsSideContainer.getChildren().add(showSameButton);
        selectionOptionsSideContainer.getChildren().add(showDiffButton);
        Separator separator2 = new Separator();
        selectionOptionsSideContainer.getChildren().add(separator2);
        selectionOptionsSideContainer.getChildren().add(processRegionLabelContainer);
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
        Label idFill = new Label("");
        idFill.setId("id");
        Label typeFill = new Label("");
        typeFill.setId("type");
        Label chromFill = new Label("");
        chromFill.setId("chrom");
        Label posFill = new Label("");
        posFill.setId("pos");
        Label lengthFill = new Label("");
        lengthFill.setId("length");
        Label qualFill = new Label("");
        qualFill.setId("qual");
        Label filterFill = new Label("");
        filterFill.setId("filter");

        this.mateContainer.setId("bndContainer");
        this.mateContainer.setAlignment(Pos.CENTER_LEFT);
        Label mateFill = new Label("");
        mateFill.setId("mate");

        Label genotypesFill = new Label("");
        genotypesFill.setId("genotypes");
        // styling to set genotypes to the right
        genotypesFill.setPadding(new Insets(0, 0, 0, 15));

        Label idLabel = new Label("ID: ");
        idLabel.setStyle(regularStyle);
        Label typeLabel = new Label("TYPE: ");
        typeLabel.setStyle(regularStyle);
        Label chromLabel = new Label("CHROM: ");
        chromLabel.setStyle(regularStyle);
        Label posLabel = new Label("POS: ");
        posLabel.setStyle(regularStyle);
        Label lengthLabel = new Label("LENGTH: ");
        lengthLabel.setStyle(regularStyle);
        Label qualLabel = new Label("QUAL: ");
        qualLabel.setStyle(regularStyle);
        Label filterLabel = new Label("FILTER: ");
        filterLabel.setStyle(regularStyle);
        Label mateLabel = new Label("MATE: ");
        mateLabel.setStyle(regularStyle);
        Label genotypesLabel = new Label("GENOTYPES: ");
        genotypesLabel.setStyle(regularStyle);

        callInfoSideContainer.getChildren().add(new HBox(idLabel, idFill));
        callInfoSideContainer.getChildren().add(new HBox(typeLabel, typeFill));
        callInfoSideContainer.getChildren().add(new HBox(chromLabel, chromFill));
        callInfoSideContainer.getChildren().add(new HBox(posLabel, posFill));
        callInfoSideContainer.getChildren().add(new HBox(lengthLabel, lengthFill));
        callInfoSideContainer.getChildren().add(new HBox(qualLabel, qualFill));
        callInfoSideContainer.getChildren().add(new HBox(filterLabel, filterFill));
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
        fileMenu.getItems().add(importGFFItem);
        menuBar.getMenus().add(fileMenu);
        menuBar.getMenus().add(viewMenu);
        viewMenu.getItems().add(colorThemesMenu);
        // add items to color themes and initially disable
        colorThemesMenu.setDisable(true);
        colorThemesMenu.getItems().add(AFColorThemesMenu);
        colorThemesMenu.getItems().add(SVGlyphThemesMenu);
        // SV glyph toggling
        SVGlyphThemesMenu.getItems().add(defaultSVGlyphColorItem);
        SVGlyphThemesMenu.getItems().add(colorblindSVGlyphItem);
        defaultSVGlyphColorItem.setToggleGroup(SVGlyphThemesGroup);
        colorblindSVGlyphItem.setToggleGroup(SVGlyphThemesGroup);
        defaultSVGlyphColorItem.setOnAction(e -> {
            this.updateCurrentSVGlyphTheme("default");
        });
        colorblindSVGlyphItem.setOnAction(e -> {
            this.updateCurrentSVGlyphTheme("colorblind");
        });
        defaultSVGlyphColorItem.setSelected(true);
        // AF toggling
        AFColorThemesMenu.getItems().add(grayscaleAFTrackItem);
        grayscaleAFTrackItem.setToggleGroup(AFThemesGroup);
        // ---------- LOGO CONTAINER --------------
        logoRectangle.setFill(Color.TRANSPARENT);
        logoContainer.getChildren().add(logoRectangle);
        logoRectangle.setFill(imagePattern);

        // ---------- CONTROL CONTAINER ----
        // controlled width columns
        controlCol0.setPercentWidth(33);
        controlCol0.setHgrow(Priority.ALWAYS);
        controlCol1.setPercentWidth(34);
        controlCol1.setHgrow(Priority.ALWAYS);
        controlCol2.setPercentWidth(33);
        controlCol2.setHgrow(Priority.ALWAYS);
        controlWrapper.getColumnConstraints().addAll(controlCol0, controlCol1, controlCol2);
        // add hboxes
        controlWrapper.add(logoContainer, 0, 0);
        controlWrapper.add(navContainer, 1, 0);
        controlWrapper.add(generalControlContainer, 2, 0);

        // navigation
        navContainer.setAlignment(Pos.CENTER);
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
        navContainer.getChildren().addAll(chromComboBox, regionField, processRegionButton);
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
        // controls
        // push buttons right
        HBox.setHgrow(generalControlSpacer, Priority.ALWAYS);
        // initially set buttons to disabled until file is loaded
        zoomInButton.setDisable(true);
        zoomOutButton.setDisable(true);
        processButton.setDisable(true);
        showSameButton.setDisable(true);
        showDiffButton.setDisable(true);
        clearButton.setDisable(true);
        shrinkTrackHeightButton.setDisable(true);
        growTrackHeightButton.setDisable(true);
        sidePaneButton.setDisable(true);
        closeSidePaneButton.setDisable(true);
        showMateButton.setDisable(true);
        zoomInButton.setMinSize(70, 25);
        zoomInButton.setMaxSize(70, 25);
        zoomOutButton.setMinSize(70, 25);
        zoomOutButton.setMaxSize(70, 25);
        clearButton.setMinSize(50,25);
        processButton.setMinSize(50,25);
        shrinkTrackHeightButton.setMinSize(75,25);
        shrinkTrackHeightButton.setMaxSize(75,25);
        growTrackHeightButton.setMinSize(80,25);
        growTrackHeightButton.setMaxSize(80,25);
        sidePaneButton.setMinSize(140,25);
        sidePaneButton.setMaxSize(140,25);
        closeSidePaneButton.setMinSize(25,25);
        closeSidePaneButton.setMaxSize(25,25);
        // button focus off
        zoomInButton.setFocusTraversable(false);
        zoomOutButton.setFocusTraversable(false);
        clearButton.setFocusTraversable(false);
        processButton.setFocusTraversable(false);
        showSameButton.setFocusTraversable(false);
        showDiffButton.setFocusTraversable(false);
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
        showSameButton.setStyle(circularStyle);
        showDiffButton.setStyle(circularStyle);
        shrinkTrackHeightButton.setStyle(circularStyle);
        growTrackHeightButton.setStyle(circularStyle);
        sidePaneButton.setStyle(circularStyle);
        closeSidePaneButton.setStyle(circularStyle);
        closeSidePaneButton.setStyle("-fx-font-size: 12px;");
        // add button
        generalControlContainer.setAlignment(Pos.CENTER_RIGHT);
        generalControlContainer.getChildren().add(generalControlSpacer);
        generalControlContainer.getChildren().add(zoomInButton);
        generalControlContainer.getChildren().add(zoomOutButton);
        generalControlContainer.getChildren().add(shrinkTrackHeightButton);
        generalControlContainer.getChildren().add(growTrackHeightButton);

        // -----------SELECTIONCONTROLCONTAINER --------------------
        selectionControlContainer.setMinHeight(30);
        selectionControlContainer.setMaxHeight(30);
        selectionControlContainer.setPrefHeight(30);
        selectionControlContainer.setAlignment(Pos.BOTTOM_RIGHT);
        HBox.setHgrow(selectionControlSpacer, Priority.ALWAYS);
        selectionControlContainer.getChildren().add(selectionControlSpacer);
        selectionControlContainer.getChildren().add(clearButton);
        selectionControlContainer.getChildren().add(sidePaneButton);


        // ---------- REF PANEL ---------
        // reference rectangle
        this.referenceWrapper.setPrefHeight(50);
        this.referenceWrapper.setMinHeight(50);
        this.referenceWrapper.setMaxHeight(50);
        this.referenceContainer.getChildren().add(rectWithMarker);
        // marker
        markerWrapper.getChildren().add(marker);
        marker.setOnMouseEntered(e -> marker.setCursor(Cursor.HAND));
        marker.setOnMouseExited(e -> marker.setCursor(Cursor.DEFAULT));
        layout.getChildren().addAll(menuBar, controlWrapper, selectionControlContainer, referenceContainer, tickContainer, callsContentContainer);
        // ---------- TICK PANEL ---------
        spaceWrapper.setMinWidth(this.sampleSpaceWidth);
        spaceWrapper.setPrefWidth(this.sampleSpaceWidth);
        spaceWrapper.setMaxWidth(this.sampleSpaceWidth);
        // hand icon
        handIcon.setContent(
                "M12 1.5 Q11.2344 1.5 10.625 1.9844 Q10.0312 2.4531 9.8438 3.1875 Q9.4062 3 9 3 Q8.0938 3 7.4219 3.6719 Q6.75 4.3281 6.75 5.25 L6.75 13.3125 L6.0938 12.6562 Q5.4375 12 4.5 12 Q3.5625 12 2.9062 12.6562 Q2.25 13.3125 2.25 14.25 Q2.25 15.1875 2.9062 15.8438 L8 20.9219 Q8.7188 21.6406 9.5625 22.0625 Q10.5 22.5 11.5312 22.5 L15 22.5 Q16.4375 22.5 17.6406 21.7969 Q18.8438 21.0938 19.5469 19.8906 Q20.25 18.6719 20.25 17.25 L20.25 8.25 Q20.25 7.3281 19.5781 6.6719 Q18.9219 6 18 6 Q17.6562 6 17.25 6.1406 L17.25 5.25 Q17.25 4.3281 16.5781 3.6719 Q15.9219 3 15 3 Q14.6094 3 14.1562 3.1875 Q13.9688 2.4531 13.375 1.9844 Q12.7812 1.5 12 1.5 ZM12 3 Q12.3281 3 12.5312 3.2188 Q12.75 3.4219 12.75 3.75 L12.75 11.25 L14.25 11.25 L14.25 5.25 Q14.25 4.9219 14.4531 4.7188 Q14.6719 4.5 15 4.5 Q15.3281 4.5 15.5312 4.7188 Q15.75 4.9219 15.75 5.25 L15.75 11.25 L17.25 11.25 L17.25 8.25 Q17.25 7.9219 17.4531 7.7188 Q17.6719 7.5 18 7.5 Q18.3281 7.5 18.5312 7.7188 Q18.75 7.9219 18.75 8.25 L18.75 17.25 Q18.75 18.2812 18.25 19.1406 Q17.75 19.9844 16.8906 20.5 Q16.0312 21 15 21 L11.5312 21 Q10.2188 21 9.0781 19.8438 L3.9688 14.7812 Q3.7344 14.5469 3.7344 14.25 Q3.7344 13.9375 3.9688 13.7031 Q4.2031 13.4688 4.5 13.4688 Q4.8125 13.4688 5.0469 13.7031 L8.25 16.9375 L8.25 5.25 Q8.25 4.9219 8.4531 4.7188 Q8.6719 4.5 9 4.5 Q9.3281 4.5 9.5312 4.7188 Q9.75 4.9219 9.75 5.25 L9.75 11.25 L11.25 11.25 L11.25 3.75 Q11.25 3.4219 11.4531 3.2188 Q11.6719 3 12 3 Z"
        );
        handIcon.setFill(Color.TRANSPARENT);
        handIcon.setScaleX(1);
        handIcon.setScaleY(1);
        handIcon.setId("handIcon");

        tickContainer.getChildren().add(spaceWrapper);
        this.tickContainer.setPrefHeight(40);
        this.tickContainer.setMinHeight(40);
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
        this.callsContentContainer.getChildren().add(tracksInfoPanel);
        this.callsContentContainer.getChildren().add(callsPanel);
        tracksInfoPanel.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        tracksInfoPanel.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        //tracksInfoPanel.vvalueProperty().bind(callsPanel.vvalueProperty());
        tracksInfoPanel.setStyle("-fx-background-color: transparent;");
        this.callsPanel.setStyle("-fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
        this.callsPanel.setStyle("-fx-background-color:transparent;");
        this.tracksPanel.setStyle("-fx-background-color:white;");
        this.callsPanel.setMinWidth(Screen.getPrimary().getBounds().getWidth() - sampleSpaceWidth);
        this.callsPanel.setPrefWidth(Screen.getPrimary().getBounds().getWidth() - sampleSpaceWidth);
        this.callsPanel.setMaxWidth(Screen.getPrimary().getBounds().getWidth() - sampleSpaceWidth);
        callsPanel.setOnMouseReleased(e -> {
            this.hBarReleased(e, callsPanel.getHvalue());
        });
        selectionWrapper.setPickOnBounds(false);
        this.selectionContainer.setPickOnBounds(false);
        this.selectionContainer.getChildren().add(selectionWrapper);
    }

    // *************************************************************** MAIN CONTROL FUNCTIONS ************************************************************************
    /**
     * Enables all buttons in controlContainer and sidePane. Side Pane translation also done here after layout is complete
     **/
    public void enableControls() {
        this.colorThemesMenu.setDisable(false);
        this.zoomInButton.setDisable(false);
        this.zoomOutButton.setDisable(false);
        this.processButton.setDisable(false);
        this.showSameButton.setDisable(false);
        this.showDiffButton.setDisable(false);
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

        // 2. Get the total height of the window
        double topOfPanelOnScreen = callsPanel.localToScreen(0, 0).getY();
        double windowBottomOnScreen = callsPanel.getScene().getWindow().getY() + callsPanel.getScene().getWindow().getHeight();
        StackPane.setAlignment(layout, Pos.TOP_CENTER);
        callsPanel.setMinHeight(windowBottomOnScreen - topOfPanelOnScreen);
        callsPanel.setPrefHeight(windowBottomOnScreen - topOfPanelOnScreen);
        callsPanel.setMaxHeight(windowBottomOnScreen - topOfPanelOnScreen);
        callsPanel.setFitToHeight(false);
        callsPanel.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        this.scrollViewportHeight = windowBottomOnScreen - topOfPanelOnScreen;

        // bind the layout height and content height of the tracks info with the calls panel
        tracksInfoPanel.prefHeightProperty().bind(callsPanel.prefHeightProperty());
        tracksInfoPanel.minHeightProperty().bind(callsPanel.minHeightProperty());
        tracksInfoPanel.maxHeightProperty().bind(callsPanel.maxHeightProperty());
        Region callsContent = (Region) callsPanel.getContent();
        Region tracksContent = (Region) tracksInfoPanel.getContent();
        tracksContent.prefHeightProperty().bind(callsContent.prefHeightProperty());
        tracksContent.minHeightProperty().bind(callsContent.minHeightProperty());
        tracksContent.maxHeightProperty().bind(callsContent.maxHeightProperty());
        // Now that the heights of scrollpanes are identical, bind the vvalues
        tracksInfoPanel.vvalueProperty().bind(callsPanel.vvalueProperty());
    }

    /**
     * Resets the relavent components in the View such that a new VCF file can be uploaded.
     */
    public void reset() {
        // clear panes
        this.ticksWrapper.getChildren().clear();
        this.selectionWrapper.getChildren().clear();
        this.samplesContainer.getChildren().clear();
        // disable buttons
        this.zoomInButton.setDisable(true);
        this.zoomOutButton.setDisable(true);
        this.processButton.setDisable(true);
        this.showSameButton.setDisable(true);
        this.showDiffButton.setDisable(true);
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

    public void initSidePane(ArrayList<Sample> samples, HashMap<String, Color> sampleColors) {
        HashMap<String,Boolean> result = new HashMap<>();
        comparators.setPadding(new Insets(0, 0, 0, 20));
        Label pinLabel = new Label("Pin to Top:");
        String style = """
                -fx-text-fill: #555555;
                -fx-font-weight: bold;
                -fx-font-size: 14px;
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
                    moveSample(sample, OGIndex, "TOP");
                    toggleSampleLock(sample);
                }
                else {
                    moveSample(sample, OGIndex, "BOTTOM");
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
    public void initReference(LinkedHashMap<String, Chromosome> refContigs, long totalRefLength) {
        // add necessary icon as init reference
        // display hand icon
        handIcon.layoutXProperty().bind(spaceWrapper.widthProperty().subtract(handIcon.boundsInLocalProperty().get().getWidth()).subtract(spaceWrapper.widthProperty().multiply(0.25)));
        handIcon.layoutYProperty().bind(
                spaceWrapper.heightProperty()
                        .subtract(spaceWrapper.heightProperty().multiply(0.1)) // 25% from bottom
                        .subtract(handIcon.boundsInLocalProperty().get().getHeight()) // move icon top up
        );
        spaceWrapper.getChildren().add(handIcon);

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

    public void initSamples(ArrayList<Sample> samples, HashMap<String, Color> sampleColors, long refLength, double zoomLevel, double baseFontSize, int originalTrackHeight, int AFTrackHeight) {
        /*
        Post-conditions: Samples added to sampleOrder ArrayList
         */
        // add additional track for allele frequency
        this.createNewAnnotationTrack(refLength, zoomLevel, "Allele Freq.", baseFontSize, AFTrackHeight, "AF");
        for (Sample sample : samples) {
            sampleOrder.add(sample);
            this.createNewCallTrack(sampleColors, refLength, zoomLevel, sample.getName(), baseFontSize, originalTrackHeight);
        }
        this.callsPanel.setPannable(true);   // Optional: enables mouse drag scrolling
    }

    /*
    Shows the allele frequency markers for a given chromosome in specified tiles
    @param refContigs - Map of <String,Chromosome> of Chromosome objects
    @param chromosome - Chromosome object to show allele freq markers for
    @param zoomLevel - double for the current zoom level
    @param AFTrackHeight - integer for the AF track height
    @param allCalls - Map of <Chromosome<Int,Tile>> that holds tiles for Chromosome objects
    @param startInterval - start interval of allele freq to show (-1 if whole chrom)
    @param endInterval - end interval of allele freq to show (-1 if whole chrom)
     */
    public void showAlleleFreq(LinkedHashMap<String,Chromosome> refContigs, Chromosome chromosome, double zoomLevel, int AFTrackHeight, HashMap<Chromosome,HashMap<Integer,Tile>> allTiles, int startInterval, int endInterval) {
        // accessing track, clearing it, and updating the width
        Pane freqPane = (Pane) this.annotationsContainer.lookup("#" + "AlleleFreq");
        freqPane.getChildren().clear();
        freqPane.setMinWidth(chromosome.getLength() * zoomLevel);
        freqPane.setPrefWidth(chromosome.getLength() * zoomLevel);
        freqPane.setMaxWidth(chromosome.getLength() * zoomLevel);

        // loop through and show all chromosomes
        if (Objects.equals(chromosome.getName(), "<ALL>")) {
            // loop through each chromosome
            for (Map.Entry<String, Chromosome> chromEntry : refContigs.entrySet()) {
                if (Objects.equals(chromEntry.getKey(), "<ALL>")) {
                    // do nothing when we reach <ALL> becauase we are showing all chromosomes anyway
                }
                else {
                    // loop through each tile
                    for (Map.Entry<Integer, Tile> tileEntry : allTiles.get(chromEntry.getValue()).entrySet()) {
                        Tile tile = tileEntry.getValue();
                        for (int i=0; i<tile.getTileCalls().size(); i++) {
                            Call currentCall = tile.getTileCalls().get(i);
                            double currentFreq = tile.getTileCalls().get(i).getAlleleFreq();
                            Circle circle;
                            // ** needs to be absolutestart here because we are showing for <ALL>
                            circle = new Circle(currentCall.getAbsoluteStart()*zoomLevel, AFTrackHeight * currentFreq, 1);
                            circle.setFill(Color.BLACK);
                            circle.setStroke(Color.BLACK);
                            freqPane.getChildren().add(circle);
                        }
                    }
                }
            }
        }
        // only showing one chromosome of interest
        else {
            // if whole chromosome needs to be shown, find end interval
            if (startInterval == -1 && endInterval == -1) {
                startInterval = 1;
                endInterval = chromosome.getTileEndInterval();
            }
            // show tiles of interest
            for (int i=startInterval; i<=endInterval; i++) {
                Tile tile = allTiles.get(chromosome).get(i);
                for (Call currentCall : tile.getTileCalls()) {
                    double currentFreq = currentCall.getAlleleFreq();
                    Circle circle = new Circle(currentCall.getStart() * zoomLevel, AFTrackHeight * currentFreq, 1);
                    circle.setFill(Color.BLACK);
                    circle.setStroke(Color.BLACK);
                    freqPane.getChildren().add(circle);
                }
            }
        }
    }

    /*
    Shows the SV calls for a given chromosome in specified tiles
    @param refContigs - Map of <String,Chromosome> to hold reference sequences
    @param chromosome - Chromosome object of chromosome to show calls
    @param samples - ArrayList of Sample objects
    @param zoomLevel - double for the current zoom level
    @param originalTrackHeight - double for the original SV track height
    @param allTiles - Map of <Chromosome,HashMap<Integer,Tile>> to hold tile information, tiles numbered starting from 1
    @param startInterval - start interval of region to show, -1 if showing whole chromosome(s)
    @param endInterval - end interval of region to show, -1 if showing whole chromosome(s)
     */
    public void showSVCalls(LinkedHashMap<String,Chromosome> refContigs, Chromosome chromosome, ArrayList<Sample> samples, double zoomLevel, int originalTrackHeight, HashMap<Chromosome,HashMap<Integer,Tile>> allTiles, int startInterval, int endInterval) {
        /**
         * Pre-conditions/assumptions: Gets call pane for each sample by looking up the ID
         */
        // updating width
        this.tracksPanel.setMinWidth(chromosome.getLength() * zoomLevel);
        this.tracksPanel.setMaxWidth(chromosome.getLength() * zoomLevel);
        // update width for sample tracks
        for (Sample sample : samples) {
            Pane currentCalls = (Pane) this.samplesContainer.lookup("#" + sample.getName());
            currentCalls.getChildren().clear();
            currentCalls.setMinWidth(chromosome.getLength() * zoomLevel);
            currentCalls.setPrefWidth(chromosome.getLength() * zoomLevel);
            currentCalls.setMaxWidth(chromosome.getLength() * zoomLevel);
        }

        // show calls for all chromosomes
        if (Objects.equals(chromosome.getName(), "<ALL>")) {
            System.out.println("SHOWING ALL");
            // loop through all chromosomes
            for (Map.Entry<String, Chromosome> chromEntry : refContigs.entrySet()) {
                System.out.println("PROCESSING " + chromEntry.getValue().getName());
                if (Objects.equals(chromEntry.getKey(), "<ALL>")) {
                    // do nothing when we reach <ALL> becauase we are showing all chromosomes anyway
                } else {
                    // loop through each tile
                    for (Map.Entry<Integer, Tile> tileEntry : allTiles.get(chromEntry.getValue()).entrySet()) {
                        System.out.println("PROCESSING TILE " + tileEntry.getKey() + " FOR CHROMSOME " + chromosome.getName());
                        Tile tile = tileEntry.getValue();
                        // show the calls for each sample
                        for (Sample sample : samples) {
                            Pane currentCalls = (Pane) this.samplesContainer.lookup("#" + sample.getName());
                            for (Call currentCall : tile.getSampleCalls().get(sample)) {
                                Rectangle callRect;
                                callRect = new Rectangle(currentCall.getAbsoluteStart()*zoomLevel, 1, currentCall.getLength()*zoomLevel, originalTrackHeight-2);
                                // styling
                                callRect.setOpacity(1);
                                callRect.setStrokeWidth(2);
                                callRect.setArcWidth(5);
                                callRect.setArcHeight(5);
                                // DUP STYLING
                                if (Objects.equals(currentCall.getType(), "DUP")) {
                                    callRect.setStroke(SVGlyphColorThemes.get(this.currentSVGlyphTheme).get("DUP").get("stroke"));
                                    callRect.setOpacity(0.5);
                                    callRect.setFill(SVGlyphColorThemes.get(this.currentSVGlyphTheme).get("DUP").get("fill"));
                                }
                                else if (Objects.equals(currentCall.getType(), "INV")) {
                                    callRect.setStroke(SVGlyphColorThemes.get(this.currentSVGlyphTheme).get("INV").get("stroke"));
                                    callRect.setOpacity(0.5);
                                    callRect.setFill(SVGlyphColorThemes.get(this.currentSVGlyphTheme).get("INV").get("fill"));
                                    Line lineInv1 = new Line(currentCall.getAbsoluteStart()*zoomLevel, 1, currentCall.getAbsoluteStart()*zoomLevel + currentCall.getLength()*zoomLevel, originalTrackHeight-2);
                                    Line lineInv2 = new Line(currentCall.getAbsoluteStart()*zoomLevel + currentCall.getLength()*zoomLevel, 1, currentCall.getAbsoluteStart()*zoomLevel, originalTrackHeight-2);
                                    lineInv1.setStroke(SVGlyphColorThemes.get(this.currentSVGlyphTheme).get("INV").get("stroke"));
                                    lineInv2.setStroke(SVGlyphColorThemes.get(this.currentSVGlyphTheme).get("INV").get("stroke"));
                                    lineInv1.setOpacity(0.6);
                                    lineInv2.setOpacity(0.6);
                                    currentCalls.getChildren().add(lineInv1);
                                    currentCalls.getChildren().add(lineInv2);
                                }
                                else if (Objects.equals(currentCall.getType(), "DEL")) {
                                    callRect.setStroke(SVGlyphColorThemes.get(this.currentSVGlyphTheme).get("DEL").get("stroke"));
                                    callRect.setOpacity(0.5);
                                    callRect.setFill(SVGlyphColorThemes.get(this.currentSVGlyphTheme).get("DEL").get("fill"));
                                    Line lineDel1 = new Line(currentCall.getAbsoluteStart()*zoomLevel, 1, currentCall.getAbsoluteStart()*zoomLevel + (currentCall.getLength()*zoomLevel / 2), originalTrackHeight-2);
                                    Line lineDel2 = new Line(currentCall.getAbsoluteStart()*zoomLevel + (currentCall.getLength()*zoomLevel / 2), originalTrackHeight-2, currentCall.getAbsoluteStart()*zoomLevel + currentCall.getLength()*zoomLevel, 1);
                                    lineDel1.setStroke(SVGlyphColorThemes.get(this.currentSVGlyphTheme).get("DEL").get("stroke"));
                                    lineDel2.setStroke(SVGlyphColorThemes.get(this.currentSVGlyphTheme).get("DEL").get("stroke"));
                                    lineDel1.setOpacity(0.4);
                                    lineDel2.setOpacity(0.4);
                                    currentCalls.getChildren().add(lineDel1);
                                    currentCalls.getChildren().add(lineDel2);
                                }
                                else if (Objects.equals(currentCall.getType(), "INS")) {
                                    callRect.setStroke(SVGlyphColorThemes.get(this.currentSVGlyphTheme).get("INS").get("stroke"));
                                    callRect.setOpacity(0.5);
                                    callRect.setFill(SVGlyphColorThemes.get(this.currentSVGlyphTheme).get("INS").get("fill"));
                                    Line lineIns1 = new Line(currentCall.getAbsoluteStart()*zoomLevel + (currentCall.getLength()*zoomLevel / 2), 1, currentCall.getAbsoluteStart()*zoomLevel, originalTrackHeight-2);
                                    Line lineIns2 = new Line(currentCall.getAbsoluteStart()*zoomLevel + (currentCall.getLength()*zoomLevel / 2), 1, currentCall.getAbsoluteStart()*zoomLevel + currentCall.getLength()*zoomLevel, originalTrackHeight-2);
                                    lineIns1.setStroke(SVGlyphColorThemes.get(this.currentSVGlyphTheme).get("INS").get("stroke"));
                                    lineIns2.setStroke(SVGlyphColorThemes.get(this.currentSVGlyphTheme).get("INS").get("stroke"));
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
                                else {
                                    System.err.println("Unrecognized/unsupported SV type that was not caught earlier: " + currentCall.getType() + ". Ignoring call.");
                                    continue;
                                }
                                currentCalls.getChildren().add(callRect);
                            }
                        }
                    }
                }
            }

        }
        // show calls for a region
        else {
            System.out.println("SHOWING REGION ONLY");
            // if whole chromosome needs to be shown, find end interval
            if (startInterval == -1 && endInterval == -1) {
                startInterval = 1;
                endInterval = chromosome.getTileEndInterval();
            }
            for (int i=startInterval; i<=endInterval; i++) {
                System.out.println("PROCESSING TILE " + i + " FOR CHROMSOME " + chromosome.getName());
                Tile tile = allTiles.get(chromosome).get(i);
                for (Sample sample : samples) {
                    // get sample pane
                    Pane currentCalls = (Pane) this.samplesContainer.lookup("#" + sample.getName());
                    // loop through sample calls for tile
                    for (Call currentCall : tile.getSampleCalls().get(sample)) {
//                        if (!nodeGroups.containsKey(currentCall.getId())) {
//                            nodeGroups.put(currentCall.getId(), new ArrayList<>());
//                        }
//                        else {
//                            // do nothing
//                        }
                        Rectangle callRect;
                        callRect = new Rectangle(currentCall.getStart()*zoomLevel, 1, currentCall.getLength()*zoomLevel, originalTrackHeight-2);
                        // styling
                        callRect.setStrokeWidth(2);
                        callRect.setArcWidth(5);   // horizontal roundness
                        callRect.setArcHeight(5);
                        if (Objects.equals(currentCall.getType(), "DUP")) {
                            callRect.setStroke(SVGlyphColorThemes.get(this.currentSVGlyphTheme).get("DUP").get("stroke"));
                            callRect.setOpacity(0.5);
                            callRect.setFill(SVGlyphColorThemes.get(this.currentSVGlyphTheme).get("DUP").get("fill"));
                            //nodeGroups.get(currentCall.getId()).add(callRect);
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
                                lineDup1.setStroke(SVGlyphColorThemes.get(this.currentSVGlyphTheme).get("DUP").get("stroke"));
                                lineDup2.setStroke(SVGlyphColorThemes.get(this.currentSVGlyphTheme).get("DUP").get("stroke"));
                                lineDup3.setStroke(SVGlyphColorThemes.get(this.currentSVGlyphTheme).get("DUP").get("stroke"));
                                lineDup4.setStroke(SVGlyphColorThemes.get(this.currentSVGlyphTheme).get("DUP").get("stroke"));
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
                                lineDup5.setStroke(SVGlyphColorThemes.get(this.currentSVGlyphTheme).get("DUP").get("stroke"));
                                lineDup6.setStroke(SVGlyphColorThemes.get(this.currentSVGlyphTheme).get("DUP").get("stroke"));
                                lineDup5.setOpacity(0.5);
                                lineDup6.setOpacity(0.5);
                                currentCalls.getChildren().add(lineDup5);
                                currentCalls.getChildren().add(lineDup6);
                                // add to node groups
                                //nodeGroups.get(currentCall.getId()).add(lineDup1);
//                                nodeGroups.get(currentCall.getId()).add(lineDup2);
//                                nodeGroups.get(currentCall.getId()).add(lineDup3);
//                                nodeGroups.get(currentCall.getId()).add(lineDup4);
//                                nodeGroups.get(currentCall.getId()).add(lineDup5);
//                                nodeGroups.get(currentCall.getId()).add(lineDup6);

                            }
                            // otherwise, don't add additional lines
                            else {
                                // do nothing!
                            }
                        }
                        else if (Objects.equals(currentCall.getType(), "INV")) {
                            callRect.setStroke(SVGlyphColorThemes.get(this.currentSVGlyphTheme).get("INV").get("stroke"));
                            callRect.setOpacity(0.5);
                            callRect.setFill(SVGlyphColorThemes.get(this.currentSVGlyphTheme).get("INV").get("fill"));
                            Line lineInv1 = new Line(currentCall.getStart()*zoomLevel, 1, currentCall.getStart()*zoomLevel + currentCall.getLength()*zoomLevel, originalTrackHeight-2);
                            Line lineInv2 = new Line(currentCall.getStart()*zoomLevel + currentCall.getLength()*zoomLevel, 1, currentCall.getStart()*zoomLevel, originalTrackHeight-2);
                            lineInv1.setStroke(SVGlyphColorThemes.get(this.currentSVGlyphTheme).get("INV").get("stroke"));
                            lineInv2.setStroke(SVGlyphColorThemes.get(this.currentSVGlyphTheme).get("INV").get("stroke"));
                            lineInv1.setOpacity(0.6);
                            lineInv2.setOpacity(0.6);
                            currentCalls.getChildren().add(lineInv1);
                            currentCalls.getChildren().add(lineInv2);
                            // add to node groups
//                            nodeGroups.get(currentCall.getId()).add(callRect);
//                            nodeGroups.get(currentCall.getId()).add(lineInv1);
//                            nodeGroups.get(currentCall.getId()).add(lineInv2);
                        }
                        else if (Objects.equals(currentCall.getType(), "DEL")) {
                            callRect.setStroke(SVGlyphColorThemes.get(this.currentSVGlyphTheme).get("DEL").get("stroke"));
                            callRect.setOpacity(0.5);
                            callRect.setFill(SVGlyphColorThemes.get(this.currentSVGlyphTheme).get("DEL").get("fill"));
                            Line lineDel1 = new Line(currentCall.getStart()*zoomLevel, 1, currentCall.getStart()*zoomLevel + (currentCall.getLength()*zoomLevel / 2), originalTrackHeight-2);
                            Line lineDel2 = new Line(currentCall.getStart()*zoomLevel + (currentCall.getLength()*zoomLevel / 2), originalTrackHeight-2, currentCall.getStart()*zoomLevel + currentCall.getLength()*zoomLevel, 1);
                            lineDel1.setStroke(SVGlyphColorThemes.get(this.currentSVGlyphTheme).get("DEL").get("stroke"));
                            lineDel2.setStroke(SVGlyphColorThemes.get(this.currentSVGlyphTheme).get("DEL").get("stroke"));
                            lineDel1.setOpacity(0.4);
                            lineDel2.setOpacity(0.4);
                            currentCalls.getChildren().add(lineDel1);
                            currentCalls.getChildren().add(lineDel2);
                            // add to node groups
//                            nodeGroups.get(currentCall.getId()).add(callRect);
//                            nodeGroups.get(currentCall.getId()).add(lineDel1);
//                            nodeGroups.get(currentCall.getId()).add(lineDel2);
                        }
                        else if (Objects.equals(currentCall.getType(), "INS")) {
                            callRect.setStroke(SVGlyphColorThemes.get(this.currentSVGlyphTheme).get("INS").get("stroke"));
                            callRect.setOpacity(0.5);
                            callRect.setFill(SVGlyphColorThemes.get(this.currentSVGlyphTheme).get("INS").get("fill"));
                            Line lineIns1 = new Line(currentCall.getStart()*zoomLevel + (currentCall.getLength()*zoomLevel / 2), 1, currentCall.getStart()*zoomLevel, originalTrackHeight-2);
                            Line lineIns2 = new Line(currentCall.getStart()*zoomLevel + (currentCall.getLength()*zoomLevel / 2), 1, currentCall.getStart()*zoomLevel + currentCall.getLength()*zoomLevel, originalTrackHeight-2);
                            lineIns1.setStroke(SVGlyphColorThemes.get(this.currentSVGlyphTheme).get("INS").get("stroke"));
                            lineIns2.setStroke(SVGlyphColorThemes.get(this.currentSVGlyphTheme).get("INS").get("stroke"));
                            lineIns1.setOpacity(0.5);
                            lineIns2.setOpacity(0.5);
                            currentCalls.getChildren().add(lineIns1);
                            currentCalls.getChildren().add(lineIns2);
                            // add to node groups
//                            nodeGroups.get(currentCall.getId()).add(callRect);
//                            nodeGroups.get(currentCall.getId()).add(lineIns1);
//                            nodeGroups.get(currentCall.getId()).add(lineIns2);
                        }
                        else if (Objects.equals(currentCall.getType(), "BND") || Objects.equals(currentCall.getType(), "TRA")) {
                            Polygon traPoly = new Polygon();
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
                            // for TRA polygon
                            traPoly.setFill(Color.rgb(80, 80, 80));
                            traPoly.setId("TRA" + currentCall.getId());
                            currentCalls.getChildren().add(traPoly);
                            traPoly.setOnMouseEntered(this.callEnteredHandler);
                            traPoly.setOnMouseClicked(
                                this.callClickHandler
//                                this.showCallInformation(currentCall, samples);
//                                this.setLiveCall(currentCall);
//                                this.openSidePane();
                            );
                            // add to node groups
//                            nodeGroups.get(currentCall.getId()).add(callRect);
//                            nodeGroups.get(currentCall.getId()).add(traPoly);
                        }
                        else {
                            System.err.println("Unrecognized/unsupported SV type that was not caught earlier: " + currentCall.getType() + ". Ignoring call.");
                            continue;
                        }
                        // for call rectangle
                        currentCalls.getChildren().add(callRect);
                        callRect.setOnMouseEntered(this.callEnteredHandler);
                        callRect.setOnMouseClicked(
                            this.callClickHandler
//                            callRect.setStroke(Color.BLACK);
//                            this.showCallInformation(currentCall, samples);
//                            this.setLiveCall(currentCall);
//                            this.openSidePane();
                        );
                        callRect.setId(currentCall.getId());
                    }
                }
            }

        }
    }

    private EventHandler<MouseEvent> callClickHandler = e -> {
        Rectangle rect = (Rectangle) e.getSource();
        Call call = (Call) rect.getUserData();
        rect.setStroke(Color.BLACK);
        this.openSidePane();
    };

    private EventHandler<MouseEvent> callEnteredHandler = e -> {
        Node node = (Node) e.getSource();
        node.setCursor(Cursor.HAND);
    };

    public void hideCalls(ArrayList<Sample> samples, ArrayList<Call> calls) {
        // loop through each sample
        for (Sample sample : samples) {
            // get sample track
            Pane currentCalls = (Pane) this.samplesContainer.lookup("#" + sample.getName());
            // loop through calls to hide
            for (Call call : calls) {
                System.out.println("CALL TO STRING " + call.toString());
                Node rect = currentCalls.lookup("#" + call.getId());
                if (rect != null) {
                    rect.setOpacity(0.01);
                }
//                for (Node nodeToRemove : nodeGroups.get(call.getId())) {
//                    //currentCalls.getChildren().remove(nodeToRemove);
//                    nodeToRemove.setOpacity(0.01);
//                }
            }
        }
    }

    public void setLiveCall(Call call) {
        this.liveCall = call;
    }

    public Call getLiveCall() {
        return this.liveCall;
    }

    void showCallInformation(Call call, ArrayList<Sample> samples) {
        Label idLabel = (Label) callInfoSideContainer.lookup("#id");
        idLabel.setText(call.getId());
        Label typeLabel = (Label) callInfoSideContainer.lookup("#type");
        typeLabel.setText(call.getType());
        Label chromLabel = (Label) callInfoSideContainer.lookup("#chrom");
        chromLabel.setText(call.getChromosome());
        Label posLabel = (Label) callInfoSideContainer.lookup("#pos");
        posLabel.setText(String.valueOf(String.format("%,d", call.getStart())));
        Label lengthLabel = (Label) callInfoSideContainer.lookup("#length");
        lengthLabel.setText(String.valueOf(call.getLength()));
        Label qualLabel = (Label) callInfoSideContainer.lookup("#qual");
        qualLabel.setText(call.getQual());
        Label filterLabel = (Label) callInfoSideContainer.lookup("#filter");
        filterLabel.setText(call.getFilter());

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
            Pattern pattern = Pattern.compile("([\\[\\]])(.+)\\1");
            Matcher matcher = pattern.matcher(call.getAlternate());
            if (matcher.find()) {
                mateLabel.setText(matcher.group(2));
            }
            else {
                System.err.println("Error: could not identify mate region in ALT field for BND or TRA. ID is " + call.getId() + " and alternate is " + call.getAlternate());
            }
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
    public double initZoomWG(long refLength) {
        this.ticksWrapper.getChildren().clear();
        // calculate zoom level such that whole genome is in view
        return callsPanel.getViewportBounds().getWidth() / refLength;
    }

    public void showCoords(Chromosome chromosome, int tickSpacing, double zoomLevel, LinkedHashMap<String, Chromosome> refContigs) {
        this.ticksWrapper.getChildren().clear();
        // get status of hand icon
        SVGPath handIcon = (SVGPath) this.spaceWrapper.lookup("#handIcon");
        // display ticks for chromosome names
        if (Objects.equals(chromosome.getName(), "<ALL>")) {
            // update hand icon
            if (handIcon.getFill().equals(Color.TRANSPARENT)) {
                // do nothing
            }
            else {
                handIcon.setFill(Color.TRANSPARENT);
            }
            // show names
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
            // update hand icon
            if (handIcon.getFill().equals(Color.TRANSPARENT)) {
                handIcon.setFill(Color.rgb(65,65,65));
            }
            else {
                // do nothing, already black
            }
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

    public double getHorizontalSBHeight() {
        ScrollBar hBar = (ScrollBar) this.callsPanel.lookup(".scroll-bar:horizontal");
        if (hBar == null || !hBar.isVisible()) {
            return 0;
        }
        else {
            return hBar.getHeight();
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

    public void createNewAnnotationTrack(long refLength, double zoomLevel, String trackName, double baseFontSize, int height, String key) {
        // types
        // GENEREPEAT
        // AF
        // PILEUP
        StackPane callsWrapper = new StackPane();
        callsWrapper.setPadding(new Insets(1, 0, 1, 0));

        Pane freqPane = new Pane();
        callsWrapper.getChildren().add(freqPane);

        freqPane.setPrefWidth(refLength * zoomLevel);
        // force height of track (or else it will collapse if there is no content)
        freqPane.setMinHeight(height);
        freqPane.setMaxHeight(height);
        if (Objects.equals(key, "AF")) {
            // set id
            freqPane.setId("AlleleFreq");


            Line topLine = new Line();
            Line bottomLine = new Line();

            // Make them dotted (dashed)
            topLine.getStrokeDashArray().addAll(5.0, 5.0);
            bottomLine.getStrokeDashArray().addAll(5.0, 5.0);

            // Set color
            topLine.setStroke(Color.BLACK);
            bottomLine.setStroke(Color.BLACK);
            // set actions for radio controls
            this.grayscaleAFTrackItem.setOnAction(e -> {
                this.applyAFColorTheme("grayscale AF", callsWrapper);
            });
            // trigger default color theme
            this.applyAFColorTheme("grayscale AF", callsWrapper);
            this.grayscaleAFTrackItem.setSelected(true);
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
        this.annotationsInfoContainer.getChildren().add(infoContainer);
        // add sampContainer to samplesContainer
        this.annotationsContainer.getChildren().add(callsWrapper);
    }

    public void createNewCallTrack(HashMap<String, Color> sampleColors, long refLength, double zoomLevel, String sampleName, double baseFontSize, int height) {
        // create callsWrapper to hold sample calls
        Pane callsWrapper = new Pane();
        callsWrapper.setPrefWidth(refLength * zoomLevel);
        // force height of track (or else it will collapse if there is no content)
        callsWrapper.setMinHeight(height);
        callsWrapper.setMaxHeight(height);

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
        lockContainer.setMinWidth(this.sampleSpaceWidth * 0.2);
        lockContainer.setMinWidth(this.sampleSpaceWidth * 0.2);

        // label container to hold label and color rect if applicable
        HBox labelContainer = new HBox();
        labelContainer.setMinWidth(this.sampleSpaceWidth*0.8);
        labelContainer.setMaxWidth(this.sampleSpaceWidth*0.8);
        labelContainer.setAlignment(Pos.CENTER_LEFT);
        // create sample color with padding around it
        Rectangle colorRect = new Rectangle(7, 7, sampleColors.get(sampleName));
        HBox.setMargin(colorRect, new Insets(0, 2, 0, 2));
        // create labelWrapper Pane to hold sample name
        StackPane labelWrapper = new StackPane();
        Label sampleLabel = new Label(sampleName);
        sampleLabel.setFont(Font.font("System", baseFontSize));
        labelWrapper.getChildren().add(sampleLabel);
        // add elements to label container
        labelContainer.getChildren().addAll(colorRect, labelWrapper);

        infoContainer.getChildren().addAll(lockContainer, labelContainer);
        this.samplesInfoContainer.getChildren().add(infoContainer);
        // add sampContainer to samplesContainer
        callsWrapper.setStyle("-fx-border-color: #DFE0DF; -fx-border-width: 0.5;");
        this.samplesContainer.getChildren().add(callsWrapper);
        // set IDs
        infoContainer.setId(sampleName);
        callsWrapper.setId(sampleName);
    }


    /**
     * syncScroll is a method that updates the translation of the coordinate system and the layout of the marker within the
     * markerWrapper.
     * @param newVal value between 0 and 1 corresponding to the start position of the new genomic region in view
     */
    public void syncScroll(Number newVal) {
        System.out.println("--------------- SYNC SCROLL TRIGGERED ----------------");
        System.out.println("new val is " + newVal);
        // getContent() gets node scrollpane is scrolling, getboundsinlocal gets actual width and height of node, so maxX is the maximum distance that can be scrolled
        double maxX = callsPanel.getContent().getBoundsInLocal().getWidth()
                - callsPanel.getViewportBounds().getWidth();
        double translateX = -newVal.doubleValue() * maxX;
        ticksWrapper.setTranslateX(translateX);
        double max = markerWrapper.getWidth() - marker.getWidth();
        double scrollX = newVal.doubleValue() * max;
        marker.setLayoutX(scrollX);
    }

    /**
     *
     * @param chromosome
     * @param zoomLevel
     * @param length
     * @param offset in pixels
     */
    public void updateMarker(Chromosome chromosome, double zoomLevel, double length, double offset) {
        callsPanel.layout();
        double visibleWidth = length * zoomLevel;
        double contentWidth = chromosome.getLength() * zoomLevel;
        double proportionVisible = visibleWidth/contentWidth;
        // set width
        marker.setWidth(markerWrapper.getWidth() * proportionVisible);
        System.out.println("OFFSET IS " + offset);
        marker.setLayoutX(offset);
    }

    public double setScroll(int start, Chromosome chrom, double zoomLevel) {
        //System.out.println("SET SCROLL PERCENT IS " + percent);
        this.callsPanel.layout();
        double contentWidth = callsPanel.getContent().getBoundsInLocal().getWidth();
        System.out.println("CONTENT WIDTH FROM SETSCROLL IS " + contentWidth);
        double viewportWidth = callsPanel.getViewportBounds().getWidth();
        double maxScroll = contentWidth - viewportWidth;

        double scale = contentWidth / chrom.getLength();
        double targetPixelX = start * scale;
        double hvalue = targetPixelX / maxScroll;
        System.out.println("SET SCROLL RETURN VAL IS " + hvalue);
        // triggers controller.processScrollChange()
        callsPanel.layout();
        this.callsPanel.setHvalue(hvalue);
        return hvalue;
    }

    public double getHValue() {
        return this.callsPanel.getHvalue();
    }

    public void cleanUnusedNodes(Chromosome chromosome, int start, int end) {

    }

    public double getStartFromHVal(double hval, Chromosome currentChrom, double zoomLevel) {
        // pixel lengths
        // this is based off the zoom level not the processed length in case hval and zoom are updated but not calls
        double contentWidth = currentChrom.getLength() * zoomLevel;
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

    public void hBarReleased(MouseEvent e, double hValue) {
        this.syncScroll(hValue);
    }

    public double getMarkerWrapperWidth() {
        return this.markerWrapper.getWidth();
    }

    public double getCallsPanelHeight() {
        return this.scrollViewportHeight;
    }

    public void updateTrackHeight(ArrayList<Sample> samples, double val) {
        Scale scale = new Scale();
        // pivot at top edge, so it grows and shrinks from top
        scale.setPivotY(0);
        scale.setY(val);
        // remove old transforms
        this.samplesContainer.getTransforms().clear();
        this.samplesContainer.getTransforms().add(scale);
        this.samplesInfoContainer.getTransforms().clear();
        this.samplesInfoContainer.getTransforms().add(scale);
        this.triggerScrollPane();
    }

    private void triggerScrollPane() {
        // Apply CSS/layout for accurate prefHeight
        double newHeight = this.samplesContainer.getBoundsInParent().getHeight() + this.annotationsContainer.getLayoutBounds().getHeight();
        tracksGroup.setMinHeight(newHeight);
        tracksGroup.setPrefHeight(newHeight);
        tracksGroup.setMaxHeight(newHeight);
        tracksContainer.setMinHeight(newHeight);
        tracksContainer.setPrefHeight(newHeight);
        tracksContainer.setMaxHeight(newHeight);
        ScrollBar hScrollBar = (ScrollBar) callsPanel.lookup(".scroll-bar:horizontal");
        double scrollbarHeight = 0;
        if (hScrollBar != null) {
            scrollbarHeight = hScrollBar.getHeight();
        }
        samplesInfoContainer.setMinHeight(newHeight + scrollbarHeight);
        samplesInfoContainer.setPrefHeight(newHeight + scrollbarHeight);
        samplesInfoContainer.setMaxHeight(newHeight + scrollbarHeight);
    }

    public void redrawSampleInfoAfterScale(ArrayList<Sample> samples, double baseFontSize, double trackHeightScale, int originalTrackHeight) {
        /**
         * Pre-conditions/assumptions: Gets info pane for each sample by looking up the ID
         */
        for (Sample sample : samples) {
            HBox container = (HBox) samplesInfoContainer.lookup("#" + sample.getName());
//            container.setMinHeight(originalTrackHeight * trackHeightScale);
//            container.setMaxHeight(originalTrackHeight * trackHeightScale);
            // get second HBox (labelContainer), lockContainer is first
            VBox lockContainer = (VBox) container.getChildren().getFirst();
            SVGPath lockIcon = (SVGPath) lockContainer.getChildren().getFirst();
            HBox labelContainer = (HBox) container.getChildren().get(1);
            // get the labelwrapper and color
            Rectangle rect = (Rectangle) labelContainer.getChildren().getFirst();
            Pane labelWrapper = (Pane) labelContainer.getChildren().get(1);
            Label sampleLabel = (Label) labelWrapper.getChildren().getFirst();
            sampleLabel.setScaleY(1.0/trackHeightScale);
            rect.setScaleY(1.0/trackHeightScale);
            lockIcon.setScaleY(0.75/trackHeightScale);
            //sampleLabel.setFont(new Font(sampleLabel.getFont().getFamily(), baseFontSize*trackHeightScale));
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
            double start = (selections.get(i).getPixelStart() * zoomLevel) / selections.get(i).getZoomLevel();
            double length = (selections.get(i).getPixelLength() * zoomLevel) / selections.get(i).getZoomLevel();
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


    public void moveSample(Sample sample, int OGIndex, String setting) {
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
        this.samplesContainer.getChildren().add(newIndex, calls);
        this.samplesInfoContainer.getChildren().add(newIndex, container);
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
    public void processShowSameCallsListener(EventHandler<ActionEvent> handler) {
        showSameButton.setOnAction(handler);
    }
    public void processShowDiffCallsListener(EventHandler<ActionEvent> handler) {
        showDiffButton.setOnAction(handler);
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

    public void browserDragged(ChangeListener<Number> listener) {
        this.callsPanel.hvalueProperty().addListener(listener);
    }

    public void markerDragged(EventHandler<MouseEvent> handler) {
        marker.setOnMouseDragged(handler);
    }



    public void applyAFColorTheme(String theme, StackPane callsWrapper) {
        // RED GREEN GRADIENT
        LinearGradient redGreenGradient = new LinearGradient(
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
        BackgroundFill redGreenFill = new BackgroundFill(redGreenGradient, CornerRadii.EMPTY, Insets.EMPTY);

        // GRAYSCALE GRADIENT
        LinearGradient grayscaleGradient = new LinearGradient(
                0, 0, 0, 1,      // startX, startY, endX, endY
                true,            // proportional
                CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.rgb(140, 144, 140, 0.8)),    // gray
                new Stop(0.05, Color.rgb(140, 144, 140, 0.8)),   // gray
                new Stop(0.25, Color.rgb(245, 245, 245, 0.6)),  // platinum
                new Stop(0.75, Color.rgb(245, 245, 245, 0.6)),  // platinum
                new Stop(0.95, Color.rgb(140, 144, 140, 0.8)),   // gray
                new Stop(1.0, Color.rgb(140, 144, 140, 0.8))     // gray
        );
        BackgroundFill grayscaleFill = new BackgroundFill(grayscaleGradient, CornerRadii.EMPTY, Insets.EMPTY);

        // apply to AF callsWrapper based on specified theme
        if (Objects.equals(theme, "red-green AF")) {
            callsWrapper.setBackground(new Background(redGreenFill));
        }
        if (Objects.equals(theme, "grayscale AF")) {
            callsWrapper.setBackground(new Background(grayscaleFill));
        }
    }

    public void updateCurrentSVGlyphTheme(String theme) {
        if (Objects.equals(theme, "default")) {
            this.currentSVGlyphTheme = "default";
        }
        else if (Objects.equals(theme, "colorblind")) {
            this.currentSVGlyphTheme = "colorblind";
        }
        else {
            // do nothing, not recognized, update for bug later
        }
    }
}
