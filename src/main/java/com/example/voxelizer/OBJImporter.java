package com.example.voxelizer;

import java.io.*;
import java.util.*;

public class OBJImporter {

    public static class Vector3 {
        public float x, y, z;

        public Vector3(float x, float y, float z) {
            this.x = x; this.y = y; this.z = z;
        }

        @Override
        public String toString() {
            return String.format("v %.4f %.4f %.4f", x, y, z);
        }
    }

    public static class Face {
        public int[] vertexIndices;

        public Face(int[] vertexIndices) {
            this.vertexIndices = vertexIndices;
        }

        @Override
        public String toString() {
            return "f " + Arrays.toString(vertexIndices);
        }
    }

    public List<Vector3> vertices = new ArrayList<>();
    public List<Face> faces = new ArrayList<>();

    public void load(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;

        while ((line = reader.readLine()) != null) {
            if (line.startsWith("v ")) {
                String[] parts = line.split("\\s+");
                float x = Float.parseFloat(parts[1]);
                float y = Float.parseFloat(parts[2]);
                float z = Float.parseFloat(parts[3]);
                vertices.add(new Vector3(x, y, z));
            } else if (line.startsWith("f ")) {
                String[] parts = line.split("\\s+");
                int[] vertexIndices = new int[parts.length - 1];

                for (int i = 1; i < parts.length; i++) {
                    String[] indices = parts[i].split("/");
                    vertexIndices[i - 1] = Integer.parseInt(indices[0]) - 1; // OBJ uses 1-based indexing
                }

                faces.add(new Face(vertexIndices));
            }
        }

        reader.close();
    }

    public void printSummary() {
        System.out.println("Vertices: " + vertices.size());
        System.out.println("Faces: " + faces.size());
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java OBJImporter <file.obj>");
            return;
        }

        OBJImporter importer = new OBJImporter();
        try {
            importer.load(args[0]);
            importer.printSummary();
        } catch (IOException e) {
            System.err.println("Failed to load OBJ file: " + e.getMessage());
        }
    }
}
