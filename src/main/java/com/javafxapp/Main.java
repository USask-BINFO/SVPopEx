package com.javafxapp;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    public void start(Stage primaryStage) {
        Model model = new Model();
        View view = new View(primaryStage);
        new Controller(model, view);
        primaryStage.setScene(view.getScene());
        primaryStage.setTitle("JavaFX SV Visualization");
        primaryStage.show();
        primaryStage.setMaximized(true);
    }
    public static void main(String[] args) {
        launch(args);
    }
}