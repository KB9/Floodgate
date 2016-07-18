package com.kavanbickerstaff.floodgate;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;

public class ViewportUtils {

    public static int screenToWorldX(OrthographicCamera camera, int x) {
        return (int)screenToWorld(camera, x, 0).x;
    }

    public static int screenToWorldY(OrthographicCamera camera, int y) {
        return (int)screenToWorld(camera, 0, y).y;
    }

    public static Vector3 screenToWorld(OrthographicCamera camera, int x, int y) {
        return camera.unproject(new Vector3(x, y, 0));
    }

    public static int worldToScreenX(OrthographicCamera camera, float x) {
        return (int)worldToScreen(camera, x, 0).x;
    }

    public static int worldToScreenY(OrthographicCamera camera, float y) {
        return (int)worldToScreen(camera, 0, y).y;
    }

    public static Vector3 worldToScreen(OrthographicCamera camera, float x, float y) {
        return camera.project(new Vector3(x, y, 0));
    }
}
