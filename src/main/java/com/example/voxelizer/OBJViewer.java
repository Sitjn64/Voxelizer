package com.example.voxelizer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

public class OBJViewer extends Application {
    private static OBJViewer instance;
    private static OBJImporter importer;
    private static Stage primaryStage;
    private static boolean isInitialized = false;

    @Override
    public void start(Stage stage) {
        instance = this;
        primaryStage = stage;
        displayModel();
    }

    private void displayModel() {
        if (importer == null || importer.vertices.isEmpty()) {
            return;
        }

        TriangleMesh mesh = new TriangleMesh();

        for (OBJImporter.Vector3 v : importer.vertices) {
            mesh.getPoints().addAll(v.x, v.y, v.z);
        }

        mesh.getTexCoords().addAll(0, 0);

        for (OBJImporter.Face face : importer.faces) {
            if (face.vertexIndices.length == 3) {
                mesh.getFaces().addAll(
                        face.vertexIndices[0], 0,
                        face.vertexIndices[1], 0,
                        face.vertexIndices[2], 0
                );
            }
        }

        MeshView meshView = new MeshView(mesh);
        meshView.setMaterial(new PhongMaterial(Color.DODGERBLUE));
        meshView.setDrawMode(DrawMode.FILL);
        meshView.getTransforms().addAll(
                new Rotate(0, Rotate.X_AXIS),
                new Rotate(0, Rotate.Y_AXIS),
                new Translate(0, 0, 0)
        );

        Group root = new Group(meshView);

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setTranslateZ(-500);

        Scene scene = new Scene(root, 800, 600, true);
        scene.setFill(Color.GRAY);
        scene.setCamera(camera);

        primaryStage.setTitle("OBJ Viewer");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void launchViewer(OBJImporter loadedModel) {
        importer = loadedModel;
        if (!isInitialized) {
            isInitialized = true;
            launch();
        } else {
            Platform.runLater(() -> {
                if (primaryStage != null && instance != null) {
                    instance.displayModel();
                }
            });
        }
    }

    @Override
    public void stop() {
        isInitialized = false;
        instance = null;
        primaryStage = null;
    }
