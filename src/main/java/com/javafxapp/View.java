package com.javafxapp;

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
import javafx.stage.Screen;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Set;

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
    private final VBox layout = new VBox();
    private final Stage primaryStage;
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
    // ---------- callsPanel ----------
    private ArrayList<Sample> sampleOrderInView = new ArrayList<Sample>();
    private final HBox callsContentContainer = new HBox();
    private final VBox samplesInfoContainer = new VBox();
    private final VBox samplesContainer = new VBox(0);
    private final HBox selectionContainer = new HBox();
    private final Pane selectionWrapper = new Pane();
    private final StackPane samplePanel = new StackPane(samplesContainer, selectionContainer);
    private final ScrollPane callsPanel = new ScrollPane(samplePanel);


    public View(Stage primaryStage) {
        this.primaryStage = primaryStage;
        // ---------- MENU --------------
        fileMenu.getItems().add(importVCFItem);
        menuBar.getMenus().add(fileMenu);
        // ---------- CONTROL PANEL -----
        // initially set buttons to disabled until file is loaded
        zoomInButton.setDisable(true);
        zoomOutButton.setDisable(true);
        processButton.setDisable(true);
        clearButton.setDisable(true);
        zoomInButton.setMinSize(35, 35);
        zoomInButton.setMaxSize(35, 35);
        zoomOutButton.setMinSize(35, 35);
        zoomOutButton.setMaxSize(35, 35);
        clearButton.setMinSize(50,35);
        processButton.setMinSize(50,35);
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
        // button style
        zoomInButton.setStyle(circularStyle);
        zoomOutButton.setStyle(circularStyle);
        clearButton.setStyle(circularStyle);
        processButton.setStyle(circularStyle);
        // add button
        controlContainer.getChildren().add(zoomInButton);
        controlContainer.getChildren().add(zoomOutButton);
        controlContainer.getChildren().add(clearButton);
        controlContainer.getChildren().add(processButton);
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
        this.clearButton.setDisable(false);
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
        System.out.println(startX);
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
        this.clearButton.setDisable(true);
    }

    public Scene getScene() {
        return new Scene(layout);
    }

    public Stage getPrimaryStage() {
        return this.primaryStage;
    }

    public void initReference(int refLength, String refName) {
        this.l1.setText(refName);
        this.l2.setText(String.valueOf(refLength) + " bp");
    }

    public void initSamples(ArrayList<Sample> samples, int refLength, double zoomLevel) {
        for (int i=0; i<samples.size(); i++) {
            sampleOrderInView.add(samples.get(i));
            // create sampContainer HBox to hold calls
            HBox sampContainer = new HBox();
            sampContainer.setPrefHeight(100);
            sampContainer.setMinWidth(refLength * zoomLevel);
            sampContainer.setPrefWidth(refLength * zoomLevel);
            sampContainer.setMaxWidth(refLength * zoomLevel);
            // create dragWrapper to hold drag lines
            StackPane dragWrapper = new StackPane();
            dragWrapper.setMinWidth(20);
            dragWrapper.setMaxWidth(20);
            dragWrapper.setPrefHeight(100);
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
            Pane labelWrapper = new Pane();
            labelWrapper.setMinWidth(70);
            labelWrapper.setMaxWidth(70);
            // create sample label
            Label sampleLabel = new Label(samples.get(i).getName());
            sampleLabel.setLayoutX(5);
            sampleLabel.setLayoutY(40);
            sampleLabel.setMinWidth(70);
            sampleLabel.setPrefWidth(70);
            sampleLabel.setMaxWidth(70);
            // create callsWrapper to hold sample calls
            Pane callsWrapper = new Pane();
            // create infoContainer to hold sample info
            HBox infoContainer = new HBox();
            // add drag lines, and sample label to infoContainer
            labelWrapper.getChildren().add(sampleLabel);
            infoContainer.getChildren().add(dragWrapper);
            infoContainer.getChildren().add(labelWrapper);
            this.samplesInfoContainer.getChildren().add(infoContainer);
            sampContainer.getChildren().add(callsWrapper);
            // add sampContainer to samplesContainer
            this.samplesContainer.getChildren().add(sampContainer);
        }
        this.callsPanel.setPannable(true);   // Optional: enables mouse drag scrolling
        for (int i=0; i<sampleOrderInView.size(); i++) {
            System.out.println(sampleOrderInView.get(i).getName());
        }
    }

    public void showCalls(ArrayList<Sample> samples, double zoomLevel, int refLength) {
        /**
         *
         */
        for (int i=0; i<samples.size(); i++) {
            HBox currentSampContainer = (HBox) this.samplesContainer.getChildren().get(i);
            Pane currentCalls = (Pane) currentSampContainer.getChildren().get(0);
            currentCalls.getChildren().clear();
            currentSampContainer.setMinWidth(refLength * zoomLevel);
            currentSampContainer.setPrefWidth(refLength * zoomLevel);
            currentSampContainer.setMaxWidth(refLength * zoomLevel);
            // loop through each call
            for (int j=0; j<samples.get(i).calls.size(); j++) {
                // create line
                Call currentCall = samples.get(i).calls.get(j);
                Rectangle callRect = new Rectangle(currentCall.getStart()*zoomLevel, 0, currentCall.getLength()*zoomLevel, 100);
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
        // set width
        double contentWidth = refLength * zoomLevel;
        double viewportWidth = callsPanel.getViewportBounds().getWidth();
        double proportionVisible = viewportWidth / contentWidth;
        double markerWidth = Screen.getPrimary().getVisualBounds().getWidth() * proportionVisible;
        marker.setWidth(markerWidth);
    }

    public void showCoords(int refLength, double zoomLevel) {
        this.ticksWrapper.getChildren().clear();
        int tickSpacing = 1000;
        int tickHeight = 10;
        // Draw ticks every 10 pixels horizontally
        for (int x = 0; x <= refLength; x += tickSpacing) {
            Label label = new Label(String.valueOf(x));
            label.setLayoutX(x*zoomLevel);
            label.setLayoutY(10);
            Line tick = new Line(x*zoomLevel, 0, x*zoomLevel, 5); // vertical line (tick)
            this.ticksWrapper.getChildren().add(tick);
            this.ticksWrapper.getChildren().add(label);
        }
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

    public void showPlot(LinkedHashMap<Selection, LinkedHashMap<String,Color>> results, ArrayList<Sample> samples, double zoomLevel) {
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

    public void updateZoom(ArrayList<Sample> samples, double zoomLevel, int refLength, ArrayList<Selection> selections, double baseLevel) {
        this.showCalls(samples, zoomLevel, refLength);
        this.showCoords(refLength, zoomLevel);
        this.updateSelections(selections, zoomLevel, baseLevel);
        // update marker width
        double contentWidth = refLength * zoomLevel;
        System.out.println("ZOOM LEVEL " + zoomLevel);
        double viewportWidth = callsPanel.getViewportBounds().getWidth();
        double proportionVisible = viewportWidth / contentWidth;
        double markerWidth = markerWrapper.getWidth() * proportionVisible;
        marker.setWidth(markerWidth);
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
    public void releaseSelectionListener(EventHandler<MouseEvent> handler) {
        this.releaseSelectionHandler = handler;
    }
}


