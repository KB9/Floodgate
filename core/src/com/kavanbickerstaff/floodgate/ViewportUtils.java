package com.kavanbickerstaff.floodgate;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;

public class ViewportUtils {

    // TODO: I don't even need the camera to work out the conversions.
    // The conversion is simply the ratio of world size and screen size.

    public static int screenToWorldX(OrthographicCamera camera, int x) {
        return (int)screenToWorld(camera, x, 0).x;
    }

    public static int screenToWorldY(OrthographicCamera camera, int y) {
        return (int)screenToWorld(camera, 0, y).y;
    }

    public static Vector3 screenToWorld(OrthographicCamera camera, int x, int y) {
        return camera.unproject(new Vector3(x, y, 0));
    }

    public static int worldToScreenX(OrthographicCamera camera, int x) {
        return (int)worldToScreen(camera, x, 0).x;
    }

    public static int worldToScreenY(OrthographicCamera camera, int y) {
        return (int)worldToScreen(camera, 0, y).y;
    }

    public static Vector3 worldToScreen(OrthographicCamera camera, int x, int y) {
        return camera.project(new Vector3(x, y, 0));
    }
}
