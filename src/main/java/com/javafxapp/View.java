package com.javafxapp;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
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
import java.util.Objects;

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
    private final VBox layout = new VBox(10);
    private final Stage primaryStage;
    // ---------- menuBar -------------
    private final MenuBar menuBar = new MenuBar();
    private final Menu fileMenu = new Menu("File");
    private final MenuItem importVCFItem = new MenuItem("Import VCF");
    // ------- referenceContainer ----------
    private final VBox referenceContainer = new VBox(5);
    private Rectangle marker = new Rectangle(0,0,0,50);
    private final Pane referenceRect = new Pane();
    // ---------- tickContainer ----------
    private final HBox tickContainer = new HBox();
    private final Pane spaceWrapper1 = new Pane();
    private final Pane ticksWrapper = new Pane();
    private EventHandler<MouseEvent> releaseSelectionHandler;
    // controlContainer
    private final HBox controlContainer = new HBox();
    Button zoomInButton = new Button("+");
    Button zoomOutButton = new Button("-");
    Button clearButton = new Button("Clear");
    // ---------- callsPanel ----------
    private final VBox samplesContainer = new VBox(0);
    private final HBox selectionContainer = new HBox();
    private final Pane selectionWrapper = new Pane();
    private final StackPane samplePanel = new StackPane(samplesContainer, selectionContainer);
    private final ScrollPane callsPanel = new ScrollPane(samplePanel);
    private final Pane markerWrapper = new Pane();
    private final Pane spaceWrapper2 = new Pane();


    public View(Stage primaryStage) {
        this.primaryStage = primaryStage;
        // ---------- MENU --------------
        fileMenu.getItems().add(importVCFItem);
        menuBar.getMenus().add(fileMenu);
        // ---------- REF PANEL ---------
        referenceContainer.setStyle("-fx-background-color: white;");
        layout.getChildren().addAll(menuBar, referenceContainer, tickContainer, controlContainer, callsPanel);
        // ---------- SAMPLE PANEL -------
        selectionWrapper.setPickOnBounds(false);
    }

    public Scene getScene() {
        return new Scene(layout);
    }

    public Stage getPrimaryStage() {
        return this.primaryStage;
    }

    public void initReference(int refLength, String refName) {
        // reference rectangle
        this.referenceRect.setPrefHeight(50);
        referenceRect.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
        referenceRect.setBorder(new Border(new BorderStroke(
                Color.BLACK,
                BorderStrokeStyle.SOLID,
                CornerRadii.EMPTY,
                new BorderWidths(1)
        )));
        // marker
        markerWrapper.getChildren().add(marker);
        marker.setFill(Color.ORANGERED);
        marker.setOpacity(0.5);
        marker.setOnMouseDragged(event -> updateHighLevelView(event));
        // reference name and length
        Label l1 = new Label(refName);
        Label l2 = new Label(String.valueOf(refLength) + " bp");
        // in a VBox
        VBox labelsBox = new VBox(10, l1, l2);
        labelsBox.setStyle("-fx-alignment: center;");
        StackPane rectangleWithLabels = new StackPane(referenceRect, labelsBox);
        StackPane rectWithMarker = new StackPane(rectangleWithLabels, markerWrapper);
        this.referenceContainer.getChildren().add(rectWithMarker);
    }

    public void initCoords() {
        // button size
        zoomInButton.setMinSize(35, 35);
        zoomInButton.setMaxSize(35, 35);
        zoomOutButton.setMinSize(35, 35);
        zoomOutButton.setMaxSize(35, 35);
        clearButton.setMinSize(50,35);
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
        // button style
        zoomInButton.setStyle(circularStyle);
        zoomOutButton.setStyle(circularStyle);
        clearButton.setStyle(circularStyle);
        // add button
        controlContainer.getChildren().add(zoomInButton);
        controlContainer.getChildren().add(zoomOutButton);
        controlContainer.getChildren().add(clearButton);
        // coordinate ticks
        spaceWrapper1.setMinWidth(100);
        spaceWrapper1.setPrefWidth(100);
        spaceWrapper1.setMaxWidth(100);
        tickContainer.getChildren().add(spaceWrapper1);
        tickContainer.getChildren().add(ticksWrapper);
        ticksWrapper.setOnMouseEntered(e -> ticksWrapper.setCursor(Cursor.HAND));
        ticksWrapper.setOnMouseExited(e -> ticksWrapper.setCursor(Cursor.DEFAULT));
        // sync scrollpane scroll with marker and coordinate ticks
        this.callsPanel.hvalueProperty().addListener((obs, oldVal, newVal) -> {
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
        });
        this.ticksWrapper.setOnMousePressed(e -> {
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
        });
        this.ticksWrapper.setOnMouseDragged(e -> {
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
        });
        this.ticksWrapper.setOnMouseReleased(e -> {
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
        });
    }

    public void initSamples(ArrayList<Sample> samples, int refLength, double zoomLevel) {
        spaceWrapper2.setMinWidth(100);
        spaceWrapper2.setPrefWidth(100);
        spaceWrapper2.setMaxWidth(100);
        this.selectionContainer.getChildren().add(spaceWrapper2);
        this.selectionContainer.getChildren().add(selectionWrapper);
        for (int i=0; i<samples.size(); i++) {
            // create sampContainer HBox to hold sample label and calls
            HBox sampContainer = new HBox();
            sampContainer.setPrefHeight(100);
            sampContainer.setMinWidth(refLength * zoomLevel);
            sampContainer.setPrefWidth(refLength * zoomLevel);
            sampContainer.setMaxWidth(refLength * zoomLevel);
            // create labelWrapper Pane to hold sample name
            Pane labelWrapper = new Pane();
            Label sampleLabel = new Label(samples.get(i).getName());
            sampleLabel.setLayoutX(5);
            sampleLabel.setLayoutY(40);
            sampleLabel.setMinWidth(100);
            sampleLabel.setPrefWidth(100);
            sampleLabel.setMaxWidth(100);
            // create calls Pane to hold sample calls
            Pane callsWrapper = new Pane();
            // add Label to Pane, and Pane to sampContainer
            labelWrapper.getChildren().add(sampleLabel);
            sampContainer.getChildren().add(labelWrapper);
            sampContainer.getChildren().add(callsWrapper);
            // add sampContainer to samplesContainer
            this.samplesContainer.getChildren().add(sampContainer);
        }
        this.callsPanel.setPannable(true);   // Optional: enables mouse drag scrolling
    }

    public void showCalls(ArrayList<Sample> samples, double zoomLevel, int refLength) {
        /**
         *
         */
        for (int i=0; i<samples.size(); i++) {
            HBox currentSampContainer = (HBox) this.samplesContainer.getChildren().get(i);
            Pane currentCalls = (Pane) currentSampContainer.getChildren().get(1);
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
            double length = (selections.get(i).getLength() / selections.get(i).getZoomLevel()) * zoomLevel;
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
    public void releaseSelectionListener(EventHandler<MouseEvent> handler) {
        this.releaseSelectionHandler = handler;
    }
}


