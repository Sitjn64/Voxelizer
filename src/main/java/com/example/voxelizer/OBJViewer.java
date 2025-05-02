package com.example.voxelizer;

import com.example.voxelizer.OBJImporter;
import javafx.application.Application;
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
    public static OBJImporter importer;

    @Override
    public void start(Stage primaryStage) {
        TriangleMesh mesh = new TriangleMesh();

        for (OBJImporter.Vector3 v : importer.vertices) {
            mesh.getPoints().addAll(v.x, v.y, v.z);
        }

        mesh.getTexCoords().addAll(0, 0); // dummy texture coordinates

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
        launch();
    }

}