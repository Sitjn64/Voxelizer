package com.example.voxelizer;

import javafx.geometry.Point3D;
import javafx.scene.shape.Box;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OBJConverter {
    private float resolution;
    private float modelScale;
    private Point3D modelCenter;

    public static class Voxel {
        private final float x, y, z;
        private final float size;
        private Box box;
        private static float sizeMultiplier = 1.0f;

        public Voxel(float x, float y, float z, float size) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.size = size;
        }

        public Box getBox() {
            if (box == null) {
                box = new Box(size * sizeMultiplier, size * sizeMultiplier, size * sizeMultiplier);
                box.setTranslateX(x);
                box.setTranslateY(y);
                box.setTranslateZ(z);
            }
            return box;
        }

        public float getY() {
            return y;
        }

        public static void setGlobalSizeMultiplier(float multiplier) {
            sizeMultiplier = multiplier;
        }
    }

    public List<Voxel> convertToVoxels(OBJImporter model, int resolution) {
        this.resolution = resolution;
        calculateModelBounds(model);
        
        List<Voxel> voxels = new ArrayList<>();
        float voxelSize = modelScale;

        // Create voxel grid
        for (OBJImporter.Face face : model.faces) {
            if (face.vertexIndices.length >= 3) {
                voxelizeFace(face, model.vertices, voxelSize, voxels);
            }
        }

        return voxels;
    }

    private void calculateModelBounds(OBJImporter model) {
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, minZ = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE, maxZ = -Float.MAX_VALUE;

        for (OBJImporter.Vector3 v : model.vertices) {
            minX = Math.min(minX, v.x);
            minY = Math.min(minY, v.y);
            minZ = Math.min(minZ, v.z);
            maxX = Math.max(maxX, v.x);
            maxY = Math.max(maxY, v.y);
            maxZ = Math.max(maxZ, v.z);
        }

        float maxDimension = Math.max(Math.max(maxX - minX, maxY - minY), maxZ - minZ);
        modelScale = maxDimension / resolution;
        modelCenter = new Point3D(
            (minX + maxX) / 2,
            (minY + maxY) / 2,
            (minZ + maxZ) / 2
        );
    }

    private void voxelizeFace(OBJImporter.Face face, List<OBJImporter.Vector3> vertices, float voxelSize, List<Voxel> voxels) {
        // Get vertices of the face
        OBJImporter.Vector3 v1 = vertices.get(face.vertexIndices[0]);
        OBJImporter.Vector3 v2 = vertices.get(face.vertexIndices[1]);
        OBJImporter.Vector3 v3 = vertices.get(face.vertexIndices[2]);

        // Calculate bounds of the face
        float minX = Math.min(Math.min(v1.x, v2.x), v3.x);
        float minY = Math.min(Math.min(v1.y, v2.y), v3.y);
        float minZ = Math.min(Math.min(v1.z, v2.z), v3.z);
        float maxX = Math.max(Math.max(v1.x, v2.x), v3.x);
        float maxY = Math.max(Math.max(v1.y, v2.y), v3.y);
        float maxZ = Math.max(Math.max(v1.z, v2.z), v3.z);

        // Create voxels for the face
        for (float x = minX; x <= maxX; x += voxelSize) {
            for (float y = minY; y <= maxY; y += voxelSize) {
                for (float z = minZ; z <= maxZ; z += voxelSize) {
                    Point3D point = new Point3D(x, y, z);
                    if (isPointInTriangle(point, v1, v2, v3)) {
                        voxels.add(new Voxel(x, y, z, voxelSize));
                    }
                }
            }
        }
    }

    private boolean isPointInTriangle(Point3D p, OBJImporter.Vector3 v1, OBJImporter.Vector3 v2, OBJImporter.Vector3 v3) {
        // Simplified point-in-triangle test
        Point3D a = new Point3D(v1.x, v1.y, v1.z);
        Point3D b = new Point3D(v2.x, v2.y, v2.z);
        Point3D c = new Point3D(v3.x, v3.y, v3.z);

        // Calculate barycentric coordinates
        Point3D vec0 = b.subtract(a);
        Point3D vec1 = c.subtract(a);
        Point3D vec2 = p.subtract(a);

        double d00 = vec0.dotProduct(vec0);
        double d01 = vec0.dotProduct(vec1);
        double d11 = vec1.dotProduct(vec1);
        double d20 = vec2.dotProduct(vec0);
        double d21 = vec2.dotProduct(vec1);

        double denom = d00 * d11 - d01 * d01;
        if (denom == 0) return false;

        double v = (d11 * d20 - d01 * d21) / denom;
        double w = (d00 * d21 - d01 * d20) / denom;
        double u = 1.0 - v - w;

        return v >= 0 && w >= 0 && (v + w) <= 1;
    }

    public float getMinLayer(List<Voxel> voxels) {
        return voxels.stream()
                .map(Voxel::getY)
                .min(Float::compareTo)
                .orElse(0f);
    }

    public float getMaxLayer(List<Voxel> voxels) {
        return voxels.stream()
                .map(Voxel::getY)
                .max(Float::compareTo)
                .orElse(0f);
    }

    public List<Voxel> getLayerVoxels(List<Voxel> voxels, float layer) {
        return voxels.stream()
                .filter(v -> Math.abs(v.getY() - layer) < modelScale/2)
                .collect(Collectors.toList());
    }
}