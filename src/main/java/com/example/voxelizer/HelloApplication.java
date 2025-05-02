package com.example.voxelizer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloApplication extends Application {
    private static OBJImporter initialModel;

    @Override
    public void start(Stage stage) throws java.io.IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("Model-viewer.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Voxelizer");
        stage.setScene(scene);
        stage.show();
    }

    public static void launchViewer(OBJImporter model) {
        initialModel = model;
        launch();
    }


    public static void main(String[] args) {
        if (args.length < 1) {
            // No arguments, just launch the empty viewer
            launch(args);
            return;
        }

        // Create and load the OBJ file
        OBJImporter importer = new OBJImporter();
        try {
            // Load the 3D model
            importer.load(args[0]);

            // Print model information
            System.out.println("Model loaded successfully!");
            importer.printSummary();

            // Launch the viewer with the loaded model
            initialModel = importer;
            launch(args);

        } catch (Exception e) {
            System.err.println("Error loading or displaying the model: " + e.getMessage());
            e.printStackTrace();
        }
    }


}