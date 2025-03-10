package com.app;

import java.io.IOException;
import com.raylib.Colors;
import com.raylib.Raylib;
import com.raylib.Raylib.Camera3D;
import com.raylib.Raylib.Vector3;
import com.reconstruction.Model;
import com.reconstruction.View;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.util.Pair;


@SuppressWarnings("resource")
public class Main {

    private final static float CAMERA_ROTATION = 0.05f;
    private final static int TARGET_FPS = 60;
    private final static int AXES_LENGTH = 100;

    private final static Vector3 x_axis = new Vector3().x(AXES_LENGTH);
    private final static Vector3 y_axis = new Vector3().y(AXES_LENGTH);
    private final static Vector3 z_axis = new Vector3().z(AXES_LENGTH);

    private static Camera3D camera;
    private static Model model;
    private final static String MODEL_NAME = "cross";

    public static void main(String args[]) throws IOException {
        // Try to load the 3D Model
        model = new Model(MODEL_NAME);
        model.initialReconstruction();
        model.refineModel();
        model.generateEdges();
        System.out.println(model);

        // Proceed with the data visualization
        Raylib.SetTraceLogLevel(Raylib.LOG_ERROR);
        Raylib.InitWindow(1000, 1000, MODEL_NAME);
        Raylib.SetTargetFPS(TARGET_FPS);

        camera = new Camera3D()
            ._position(new Vector3().x(400).y(400).z(400))
            .target(Raylib.Vector3Zero())
            .projection(Raylib.CAMERA_ORTHOGRAPHIC)
            .up(new Vector3().y(1))
            .fovy(90);

        while (!Raylib.WindowShouldClose()) {
            handleInput();
            Raylib.BeginDrawing();
            Raylib.ClearBackground(Colors.WHITE);
            Raylib.BeginMode3D(camera);
            drawAxes();
            drawModel();
            Raylib.EndMode3D();
            Raylib.EndDrawing();
        }

        Raylib.CloseWindow();
    }

    private static void drawModel() {
        // Draw each detected vertex in each view
        for (View view : model.getViews()) {
            for (Vector3D vec : view.getVertices()) {
                Vector3 raylib_translate = new Vector3()
                    .x((float) vec.getX())
                    .y((float) vec.getZ())
                    .z((float) vec.getY());
                Raylib.DrawSphere(raylib_translate, 0.5f, Colors.BLUE);
            }
        }

        // Draw the reconstructed model vertices
        for (Vector3D vec : model.getVertices()) {
            Vector3 raylib_translate = new Vector3()
                .x((float) vec.getX())
                .y((float) vec.getZ())
                .z((float) vec.getY());
            Raylib.DrawSphere(raylib_translate, 0.5f, Colors.BLACK);
        }

        // Draw the reconstructed model edges.
        for (Pair<Vector3D, Vector3D> edge : model.getEdges()) {
            Vector3D a = edge.getKey();
            Vector3D b = edge.getValue();

            Vector3 raylib_translate_a = new Vector3()
                .x((float) a.getX())
                .y((float) a.getZ())
                .z((float) a.getY());

            Vector3 raylib_translate_b = new Vector3()
                .x((float) b.getX())
                .y((float) b.getZ())
                .z((float) b.getY());

            Raylib.DrawLine3D(raylib_translate_a, raylib_translate_b, Colors.BLACK);
        }
    }

    private static void drawAxes() {
        Raylib.DrawLine3D(Raylib.Vector3Zero(), x_axis, Colors.GRAY);
        Raylib.DrawLine3D(Raylib.Vector3Zero(), y_axis, Colors.GRAY);
        Raylib.DrawLine3D(Raylib.Vector3Zero(), z_axis, Colors.GRAY);
    }

    private static void handleInput() {
        if (Raylib.IsKeyDown(Raylib.KEY_RIGHT)) {
            // Rotate the camera around the 'Y' axis counter-clockwise.
            camera._position(Raylib.Vector3RotateByAxisAngle(camera._position(), 
                y_axis, CAMERA_ROTATION*0.2f));
        
        } else if (Raylib.IsKeyDown(Raylib.KEY_LEFT)) {
            // Rotate the camera around the 'Y' axis clockwise.
            camera._position(Raylib.Vector3RotateByAxisAngle(camera._position(),
                y_axis, -CAMERA_ROTATION));
        }
    }
}