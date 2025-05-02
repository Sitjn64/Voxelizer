package com.example.voxelizer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloApplication extends Application {
    private static OBJImporter initialModel;
    private static boolean hasBeenLaunched = false;

    @Override
    public void start(Stage stage) throws java.io.IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("Model-viewer.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        HelloController controller = fxmlLoader.getController();
        
        // If we have an initial model, load it
        if (initialModel != null) {
            controller.loadModel(initialModel);
        }
        
        stage.setTitle("Voxelizer");
        stage.setScene(scene);
        stage.show();
    }

    public static void launchViewer(OBJImporter model) {
        if (!hasBeenLaunched) {
            initialModel = model;
            launch();
            hasBeenLaunched = true;
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            launch(args);
            return;
        }

        try {
            // Create and load the OBJ file
            OBJImporter importer = new OBJImporter();
            importer.load(args[0]);

            // Print model information
            System.out.println("Model loaded successfully!");
            importer.printSummary();

            // Set the initial model and launch
            initialModel = importer;
            launch(args);

        } catch (Exception e) {
            System.err.println("Error loading or displaying the model: " + e.getMessage());
            e.printStackTrace();
        }
    }
}