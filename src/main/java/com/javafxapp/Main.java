package com.javafxapp;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    /**
     * Entry point for the appliation
     * @param primaryStage the primary stage for this application, onto which
     * the application scene can be set.
     */
    public void start(Stage primaryStage) {
        // create model-view-controller
        Model model = new Model();
        View view = new View(primaryStage);
        new Controller(model, view);
        primaryStage.setScene(view.getScene());
        primaryStage.setTitle("SVPopEx");
        primaryStage.show();
        primaryStage.setMaximized(true);
    }

    /**
     * Main method which launches the application
     * @param args
     */
    public static void main(String[] args) {
        launch(args);
    }
}
