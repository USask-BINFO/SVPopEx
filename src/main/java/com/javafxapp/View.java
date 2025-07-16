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

public class View {
    private VBox layout = new VBox(10);
    private MenuItem importVCFItem = new MenuItem("Import VCF");
    private MenuItem zoomInItem = new MenuItem("Zoom In");
    private MenuItem zoomOutItem = new MenuItem("Zoom Out");
    private VBox referencePanel = new VBox(5);
    private VBox samplesContainer = new VBox(0);
    HBox selectionContainer = new HBox();
    Pane selectionWrapper = new Pane();
    StackPane samplePanel = new StackPane(samplesContainer, selectionContainer);
    private ScrollPane scrollPane = new ScrollPane(samplePanel);
    private Stage primaryStage;
    private EventHandler<MouseEvent> releaseSelectionHandler;
    Pane referenceRect = new Pane();
    Pane markerWrapper = new Pane();
    HBox tickContainer = new HBox();
    Pane spaceWrapper1 = new Pane();
    Pane spaceWrapper2 = new Pane();
    Pane ticksWrapper = new Pane();
    Rectangle marker = new Rectangle(0,0,0,50);


    public View(Stage primaryStage) {
        this.primaryStage = primaryStage;
        // ---------- MENU --------------
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        fileMenu.getItems().add(importVCFItem);
        Menu viewMenu = new Menu("View");
        viewMenu.getItems().add(zoomInItem);
        viewMenu.getItems().add(zoomOutItem);
        menuBar.getMenus().add(fileMenu);
        menuBar.getMenus().add(viewMenu);
        // ---------- REF PANEL ---------
        referencePanel.setStyle("-fx-background-color: white;");
        layout.getChildren().addAll(menuBar, referencePanel, scrollPane);
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
        this.referencePanel.getChildren().add(rectWithMarker);
    }

    public void initCoords() {
        // coordinate ticks
        spaceWrapper1.setMinWidth(100);
        spaceWrapper1.setPrefWidth(100);
        spaceWrapper1.setMaxWidth(100);
        tickContainer.getChildren().add(spaceWrapper1);
        tickContainer.getChildren().add(ticksWrapper);
        ticksWrapper.setOnMouseEntered(e -> ticksWrapper.setCursor(Cursor.HAND));
        ticksWrapper.setOnMouseExited(e -> ticksWrapper.setCursor(Cursor.DEFAULT));
        this.referencePanel.getChildren().add(tickContainer);
        // sync scrollpane scroll with marker and coordinate ticks
        this.scrollPane.hvalueProperty().addListener((obs, oldVal, newVal) -> {
            // oldVal = old scroll position (between 0 and 1)
            // newVal = new scroll position (between 0 and 1)
            // getContent() gets node scrollpane is scrolling, getboundsinlocal gets actual width and height of node, so maxX is the maximum distance that can be scrolled
            double maxX = scrollPane.getContent().getBoundsInLocal().getWidth()
                    - scrollPane.getViewportBounds().getWidth();
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

    public void initSamples(ArrayList<Sample> samples) {
        spaceWrapper2.setMinWidth(100);
        spaceWrapper2.setPrefWidth(100);
        spaceWrapper2.setMaxWidth(100);
        this.selectionContainer.getChildren().add(spaceWrapper2);
        this.selectionContainer.getChildren().add(selectionWrapper);
        for (int i=0; i<samples.size(); i++) {
            // create sampContainer HBox to hold sample label and calls
            HBox sampContainer = new HBox();
            sampContainer.setPrefHeight(100);
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
        this.scrollPane.setPannable(true);   // Optional: enables mouse drag scrolling
    }

    public void showCalls(ArrayList<Sample> samples, double zoomLevel) {
        /**
         *
         */
        for (int i=0; i<samples.size(); i++) {
            HBox currentSampContainer = (HBox) this.samplesContainer.getChildren().get(i);
            Pane currentCalls = (Pane) currentSampContainer.getChildren().get(1);
            currentCalls.getChildren().clear();
            // loop through each call
            for (int j=0; j<samples.get(i).calls.size(); j++) {
                // create line
                Call currentCall = samples.get(i).calls.get(j);
                Rectangle callRect = new Rectangle(currentCall.getStart()*zoomLevel, 0, (currentCall.getStart() + currentCall.getLength())*zoomLevel, 100);
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
        double contentWidth = scrollPane.getContent().getBoundsInLocal().getWidth();
        double viewportWidth = scrollPane.getViewportBounds().getWidth();
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

    public void clearSelection() {
        Rectangle tickRect = (Rectangle) this.ticksWrapper.lookup("#selectionRect");
        Rectangle selectRect = (Rectangle) this.selectionWrapper.lookup("#selectionRect");
        tickRect.setId(null);
        selectRect.setId(null);
    }

    public void updateZoom(ArrayList<Sample> samples, double zoomLevel, int refLength) {
        this.showCoords(refLength, zoomLevel);
        this.showCalls(samples, zoomLevel);
        // update marker width
        double contentWidth = scrollPane.getContent().getBoundsInLocal().getWidth();
        double viewportWidth = scrollPane.getViewportBounds().getWidth();
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
        this.scrollPane.setHvalue(percent);
    }


    public void importListener(EventHandler<ActionEvent> handler) {
        importVCFItem.setOnAction(handler);
    }
    public void zoomInListener(EventHandler<ActionEvent> handler) {
        zoomInItem.setOnAction(handler);
    }
    public void zoomOutListener(EventHandler<ActionEvent> handler) {
        zoomOutItem.setOnAction(handler);
    }
    public void releaseSelectionListener(EventHandler<MouseEvent> handler) {
        this.releaseSelectionHandler = handler;
    }
}


