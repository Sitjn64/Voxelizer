package com.example.voxelizer;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.ToggleButton;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.stage.FileChooser;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Box;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class HelloController implements Initializable {
    @FXML
    private BorderPane rootPane;
    @FXML
    private SubScene modelSubScene;
    @FXML
    private Slider zoomSlider;
    @FXML
    private Slider layerSlider;
    @FXML
    private Button voxelizeButton;
    @FXML
    private Spinner<Integer> resolutionSpinner;
	@FXML
	private Slider voxelScaleSlider;
    @FXML
    private ToggleButton viewModeToggle;
    private boolean isLayerView = true;

    private Group modelGroup;
    private PerspectiveCamera camera;
    private MeshView meshView;
    private final Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
    private double anchorX, anchorY;
    private double anchorAngleX = 0;
    private double anchorAngleY = 0;
    private OBJImporter importer;
    private OBJConverter objConverter = new OBJConverter();
    private List<OBJConverter.Voxel> voxels = new ArrayList<>();
    private int currentLayer;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupSubScene();
        setupZoomListener();
        setupMouseControl();
        setupVoxelControls();
        setupViewModeToggle();
		setupVoxelScaleSlider();
    }

	private void setupVoxelScaleSlider() {
		voxelScaleSlider.setMin(0.5);
		voxelScaleSlider.setMax(3.0);
		voxelScaleSlider.setValue(1.0);
		voxelScaleSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
			OBJConverter.Voxel.setGlobalSizeMultiplier(newVal.floatValue());
			if (voxels != null && !voxels.isEmpty()) {
				if (viewModeToggle.isSelected()) {
					displayCurrentLayer();
				} else {
					display3DView();
				}
			}
		});
	}

    private void setupViewModeToggle() {
        viewModeToggle.setSelected(true);
        viewModeToggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
            isLayerView = newVal;
            if (voxels != null && !voxels.isEmpty()) {
                if (isLayerView) {
                    displayCurrentLayer();
                } else {
                    display3DView();
                }
            }
        });
    }

    private void display3DView() {
        modelGroup.getChildren().clear();

        AmbientLight ambient = new AmbientLight(Color.WHITE);
        PointLight light = new PointLight(Color.WHITE);
        light.setTranslateZ(-1000);
        modelGroup.getChildren().addAll(ambient, light);
        PhongMaterial material = new PhongMaterial(Color.LIGHTSKYBLUE);
        for (OBJConverter.Voxel voxel : voxels) {
            Box box = voxel.getBox();
            box.setMaterial(material);
            box.setScaleX(0.9);
            box.setScaleY(0.9);
            box.setScaleZ(0.9);
            modelGroup.getChildren().add(box);
        }
    }

    private void setupSubScene() {
        Group root = (Group) modelSubScene.getRoot();

        camera = new PerspectiveCamera(true);
        camera.setTranslateZ(-1500);
        camera.setNearClip(0.1);
        camera.setFarClip(2000000.0);

        modelGroup = new Group();
        root.getChildren().add(modelGroup);
        
        AmbientLight ambient = new AmbientLight(Color.WHITE);
        PointLight light = new PointLight(Color.WHITE);
        light.setTranslateZ(-1000);
        root.getChildren().addAll(ambient, light);

        modelSubScene.setFill(Color.GRAY);
        modelSubScene.setCamera(camera);
    }

    private void setupZoomListener() {
        zoomSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (modelGroup != null) {
                modelGroup.setScaleX(newVal.doubleValue());
                modelGroup.setScaleY(newVal.doubleValue());
                modelGroup.setScaleZ(newVal.doubleValue());
            }
        });
    }

    private void setupMouseControl() {
        modelSubScene.setOnMousePressed(event -> {
            anchorX = event.getSceneX();
            anchorY = event.getSceneY();
            anchorAngleX = rotateX.getAngle();
            anchorAngleY = rotateY.getAngle();
        });

        modelSubScene.setOnMouseDragged(event -> {
            rotateX.setAngle(anchorAngleX - (anchorY - event.getSceneY()));
            rotateY.setAngle(anchorAngleY + (anchorX - event.getSceneX()));
        });
        
        modelSubScene.setOnScroll(event -> {
            double delta = event.getDeltaY() * 0.1;
            zoomSlider.setValue(zoomSlider.getValue() + delta);
        });
    }

    private void setupVoxelControls() {
        resolutionSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 100, 30)
        );

        layerSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            currentLayer = newVal.intValue();
            displayCurrentLayer();
        });
    }

    @FXML
    private void handleVoxelize() {
        if (importer == null) return;

        voxels = objConverter.convertToVoxels(importer, resolutionSpinner.getValue());

        float minLayer = objConverter.getMinLayer(voxels);
        float maxLayer = objConverter.getMaxLayer(voxels);
        layerSlider.setMin(minLayer);
        layerSlider.setMax(maxLayer);
        layerSlider.setValue(minLayer);
    }

    private void displayCurrentLayer() {
        modelGroup.getChildren().clear();
        
        AmbientLight ambient = new AmbientLight(Color.WHITE);
        PointLight light = new PointLight(Color.WHITE);
        light.setTranslateZ(-1000);
        modelGroup.getChildren().addAll(ambient, light);
        
        PhongMaterial previousMaterial = new PhongMaterial(Color.GRAY);
        previousMaterial.setDiffuseColor(Color.GRAY.deriveColor(0, 1, 1, 0.3)); // 30% opacity
        
        PhongMaterial currentMaterial = new PhongMaterial(Color.LIGHTSKYBLUE);

        for (float layer = objConverter.getMinLayer(voxels); layer < currentLayer; layer++) {
            for (OBJConverter.Voxel voxel : objConverter.getLayerVoxels(voxels, layer)) {
                Box box = new Box(voxel.getBox().getWidth(), voxel.getBox().getHeight(), voxel.getBox().getDepth());
                box.setTranslateX(voxel.getBox().getTranslateX());
                box.setTranslateY(voxel.getBox().getTranslateY());
                box.setTranslateZ(voxel.getBox().getTranslateZ());
                box.setMaterial(previousMaterial);
                box.setScaleX(0.9);
                box.setScaleY(0.9);
                box.setScaleZ(0.9);
                modelGroup.getChildren().add(box);
            }
        }
        
        for (OBJConverter.Voxel voxel : objConverter.getLayerVoxels(voxels, currentLayer)) {
            Box box = new Box(voxel.getBox().getWidth(), voxel.getBox().getHeight(), voxel.getBox().getDepth());
            box.setTranslateX(voxel.getBox().getTranslateX());
            box.setTranslateY(voxel.getBox().getTranslateY());
            box.setTranslateZ(voxel.getBox().getTranslateZ());
            box.setMaterial(currentMaterial);
            box.setScaleX(0.9);
            box.setScaleY(0.9);
            box.setScaleZ(0.9);
            modelGroup.getChildren().add(box);
        }
    }

    @FXML
    private void handleLoadFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("OBJ Files", "*.obj")
        );
        var file = fileChooser.showOpenDialog(rootPane.getScene().getWindow());
        if (file != null) {
            try {
                importer = new OBJImporter();
                importer.load(file.getPath());
                displayModel();
            } catch (Exception ex) {
                System.err.println("Error loading file: " + ex.getMessage());
            }
        }
    }

    @FXML
    private void handleWireframeToggle() {
        if (meshView != null) {
            meshView.setDrawMode(
                    meshView.getDrawMode() == DrawMode.FILL ?
                            DrawMode.LINE : DrawMode.FILL
            );
        }
    }

    @FXML
    private void handleResetView() {
        rotateX.setAngle(0);
        rotateY.setAngle(0);
        zoomSlider.setValue(1);
    }

    private void displayModel() {
        modelGroup.getChildren().clear();

        TriangleMesh mesh = new TriangleMesh();
        
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, minZ = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE, maxZ = -Float.MAX_VALUE;

        for (OBJImporter.Vector3 v : importer.vertices) {
            minX = Math.min(minX, v.x);
            minY = Math.min(minY, v.y);
            minZ = Math.min(minZ, v.z);
            maxX = Math.max(maxX, v.x);
            maxY = Math.max(maxY, v.y);
            maxZ = Math.max(maxZ, v.z);
            mesh.getPoints().addAll(v.x, v.y, v.z);
        }

        float centerX = (minX + maxX) / 2;
        float centerY = (minY + maxY) / 2;
        float centerZ = (minZ + maxZ) / 2;
        float maxSize = Math.max(Math.max(maxX - minX, maxY - minY), maxZ - minZ);
        float scale = 400 / maxSize;

        for (int i = 0; i < mesh.getPoints().size(); i += 3) {
            mesh.getPoints().set(i, (mesh.getPoints().get(i) - centerX) * scale);
            mesh.getPoints().set(i + 1, (mesh.getPoints().get(i + 1) - centerY) * scale);
            mesh.getPoints().set(i + 2, (mesh.getPoints().get(i + 2) - centerZ) * scale);
        }

        mesh.getTexCoords().addAll(0, 0);

        // Add faces
        for (OBJImporter.Face face : importer.faces) {
            if (face.vertexIndices.length >= 3) {
                mesh.getFaces().addAll(
                    face.vertexIndices[0], 0,
                    face.vertexIndices[1], 0,
                    face.vertexIndices[2], 0
                );
            }
        }

        meshView = new MeshView(mesh);
        meshView.setMaterial(new PhongMaterial(Color.LIGHTSKYBLUE));
        meshView.setDrawMode(DrawMode.FILL);
        meshView.getTransforms().addAll(rotateX, rotateY);

        modelGroup.getChildren().add(meshView);
    }
	public void loadModel(OBJImporter model) {
		this.importer = model;
		displayModel();
	}
}
