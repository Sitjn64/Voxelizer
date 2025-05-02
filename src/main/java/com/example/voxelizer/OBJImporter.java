package com.example.voxelizer;

import java.io.*;
import java.util.*;

public class OBJImporter {
    public static class Vector3 {
        public float x, y, z;

        public Vector3(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public String toString() {
            return String.format("v %.4f %.4f %.4f", x, y, z);
        }
    }

    public static class Face {
        public int[] vertexIndices;

        public Face(int[] vertexIndices) {
            if (vertexIndices == null || vertexIndices.length < 3) {
                throw new IllegalArgumentException("Face must have at least 3 vertices");
            }
            this.vertexIndices = vertexIndices.clone(); // Create a defensive copy
        }

        @Override
        public String toString() {
            return "f " + Arrays.toString(vertexIndices);
        }
    }

    public final List<Vector3> vertices = new ArrayList<>();
    public final List<Face> faces = new ArrayList<>();

    public void load(String filePath) throws IOException {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }

        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + filePath);
        }

        vertices.clear();
        faces.clear();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                try {
                    if (line.startsWith("v ")) {
                        parseVertex(line);
                    } else if (line.startsWith("f ")) {
                        parseFace(line);
                    }
                } catch (Exception e) {
                    System.err.println("Warning: Skipping invalid line: " + line);
                    System.err.println("Error: " + e.getMessage());
                }
            }
        }

        if (vertices.isEmpty()) {
            throw new IllegalStateException("No vertices found in the file");
        }
    }

    private void parseVertex(String line) {
        String[] parts = line.split("\\s+");
        if (parts.length >= 4) {
            float x = Float.parseFloat(parts[1]);
            float y = Float.parseFloat(parts[2]);
            float z = Float.parseFloat(parts[3]);
            vertices.add(new Vector3(x, y, z));
        }
    }

    private void parseFace(String line) {
        String[] parts = line.split("\\s+");
        if (parts.length >= 4) {
            int[] vertexIndices = new int[parts.length - 1];
            for (int i = 1; i < parts.length; i++) {
                String[] indices = parts[i].split("/");
                int index = Integer.parseInt(indices[0]) - 1;
                if (index < 0 || index >= vertices.size()) {
                    throw new IllegalArgumentException("Invalid vertex index: " + (index + 1));
                }
                vertexIndices[i - 1] = index;
            }
            faces.add(new Face(vertexIndices));
        }
    }

    public void printSummary() {
        System.out.println("Model Summary:");
        System.out.println("Vertices: " + vertices.size());
        System.out.println("Faces: " + faces.size());
    }
}