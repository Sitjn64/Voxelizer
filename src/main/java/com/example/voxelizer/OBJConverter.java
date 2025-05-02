package com.example.voxelizer;

import javafx.geometry.Point3D;
import javafx.scene.shape.Box;
import java.util.*;
import javafx.scene.Group;

public class OBJConverter {
    public static class Voxel {
        private final float x, y, z;
        private final float size;
        private Box box;

        public Voxel(float x, float y, float z, float size) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.size = size;
        }

        public Box getBox() {
            if (box == null) {
                box = new Box(size, size, size);
                box.setTranslateX(x);
                box.setTranslateY(y);
                box.setTranslateZ(z);
            }
            // Remove the box from its current parent if it has one
            if (box.getParent() != null) {
                ((Group) box.getParent()).getChildren().remove(box);
            }
            return box;
        }

        public float getY() {
            return y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Voxel)) return false;
            Voxel voxel = (Voxel) o;
            return Float.compare(voxel.x, x) == 0 && 
                   Float.compare(voxel.y, y) == 0 && 
                   Float.compare(voxel.z, z) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, z);
        }
    }

    private final Set<Voxel> voxels = new HashSet<>();
    private float modelScale;
    private Point3D modelCenter;
    private int resolution;

    public List<Voxel> convertToVoxels(OBJImporter model, int resolution) {
        this.resolution = resolution;
        voxels.clear();
        calculateModelBounds(model);
        voxelizeModel(model);
        return new ArrayList<>(voxels);
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
        // Increase the scale factor (e.g., multiply by 2 or 3 for larger voxels)
        modelScale = (maxDimension / resolution) * 3; 
        modelCenter = new Point3D(
            (minX + maxX) / 2,
            (minY + maxY) / 2,
            (minZ + maxZ) / 2
        );
    }

    private void voxelizeModel(OBJImporter model) {
        for (OBJImporter.Face face : model.faces) {
            if (face.vertexIndices.length >= 3) {
                voxelizeTriangle(
                    model.vertices.get(face.vertexIndices[0]),
                    model.vertices.get(face.vertexIndices[1]),
                    model.vertices.get(face.vertexIndices[2])
                );
            }
        }
    }

    private void voxelizeTriangle(OBJImporter.Vector3 v1, OBJImporter.Vector3 v2, OBJImporter.Vector3 v3) {
        Point3D p1 = transformToVoxelSpace(v1);
        Point3D p2 = transformToVoxelSpace(v2);
        Point3D p3 = transformToVoxelSpace(v3);

        // Get bounds of triangle in voxel space
        int minX = (int) Math.floor(Math.min(Math.min(p1.getX(), p2.getX()), p3.getX()));
        int maxX = (int) Math.ceil(Math.max(Math.max(p1.getX(), p2.getX()), p3.getX()));
        int minY = (int) Math.floor(Math.min(Math.min(p1.getY(), p2.getY()), p3.getY()));
        int maxY = (int) Math.ceil(Math.max(Math.max(p1.getY(), p2.getY()), p3.getY()));
        int minZ = (int) Math.floor(Math.min(Math.min(p1.getZ(), p2.getZ()), p3.getZ()));
        int maxZ = (int) Math.ceil(Math.max(Math.max(p1.getZ(), p2.getZ()), p3.getZ()));

        // Check each voxel in the bounds
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Point3D voxelCenter = new Point3D(x + 0.5, y + 0.5, z + 0.5);
                    if (isPointNearTriangle(voxelCenter, p1, p2, p3)) {
                        // Convert voxel grid coordinates back to model space
                        float modelX = (x - resolution/2) * modelScale + (float)modelCenter.getX();
                        float modelY = (y - resolution/2) * modelScale + (float)modelCenter.getY();
                        float modelZ = (z - resolution/2) * modelScale + (float)modelCenter.getZ();
                        voxels.add(new Voxel(modelX, modelY, modelZ, modelScale));
                    }
                }
            }
        }
    }

    private Point3D transformToVoxelSpace(OBJImporter.Vector3 v) {
        return new Point3D(
            (v.x - modelCenter.getX()) * modelScale + resolution/2,
            (v.y - modelCenter.getY()) * modelScale + resolution/2,
            (v.z - modelCenter.getZ()) * modelScale + resolution/2
        );
    }

    private boolean isPointNearTriangle(Point3D point, Point3D v1, Point3D v2, Point3D v3) {
        // Compute triangle normal
        Point3D edge1 = v2.subtract(v1);
        Point3D edge2 = v3.subtract(v1);
        Point3D normal = edge1.crossProduct(edge2).normalize();
        
        // Distance from point to triangle plane
        double distance = Math.abs(point.subtract(v1).dotProduct(normal));
        
        return distance < 1.0; // Using 1.0 as the voxel size
    }

    public List<Voxel> getLayerVoxels(List<Voxel> voxels, float layer) {
        return voxels.stream()
                .filter(v -> Math.abs(v.getY() - layer) < modelScale/2)
                .toList();
    }

    public float getMinLayer(List<Voxel> voxels) {
        return voxels.stream()
                .map(Voxel::getY)
                .min(Float::compare)
                .orElse(0f);
    }

    public float getMaxLayer(List<Voxel> voxels) {
        return voxels.stream()
                .map(Voxel::getY)
                .max(Float::compare)
                .orElse(0f);
    }
}