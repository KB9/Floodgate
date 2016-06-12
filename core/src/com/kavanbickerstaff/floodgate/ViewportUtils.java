package com.kavanbickerstaff.floodgate;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.viewport.Viewport;

public class ViewportUtils {

    /**
     * Converts from a screen x-coordinate to the equivalent x-coordinate in viewport space.
     * @param viewport The viewport with which the x-coordinate will be calculated.
     * @param x The screen x-coordinate to convert from.
     * @return The screen x-coordinate in viewport space.
     */
    public static int screenToViewportX(Viewport viewport, int x) {
        return (int)(((float)x / (float) Gdx.graphics.getWidth()) * viewport.getWorldWidth());
    }

    /**
     * Converts from a screen y-coordinate to the equivalent y-coordinate in viewport space.
     * @param viewport The viewport with which the y-coordinate will be calculated.
     * @param y The screen y-coordinate to convert from.
     * @return The screen y-coordinate in viewport space.
     */
    public static int screenToViewportY(Viewport viewport, int y) {
        return (int)(((float)y / (float)Gdx.graphics.getHeight()) * viewport.getWorldHeight());
    }

    /**
     * Converts from a viewport x-coordinate to the equivalent x-coordinate in world space, which
     * takes into account the position of the viewport camera on the x-axis.
     * @param viewport The viewport with which the x-coordinate will be calculated.
     * @param x The viewport x-coordinate to convert from.
     * @return The viewport x-coordinate in world space.
     */
    public static int viewportToWorldX(Viewport viewport, int x) {
        return (int)(x + (viewport.getCamera().position.x - (viewport.getWorldWidth() / 2f)));
    }

    /**
     * Converts from a viewport y-coordinate to the equivalent y-coordinate in world space, which
     * takes into account the position of the viewport camera on the y-axis.
     * @param viewport The viewport with which the x-coordinate will be calculated.
     * @param y The viewport y-coordinate to convert from.
     * @return The viewport y-coordinate in world space.
     */
    public static int viewportToWorldY(Viewport viewport, int y) {
        // NOTE: This used to be y + ..., but that seemed to be incorrect
        // Then why does the other calculation work when it's a + as well?
        // TODO: Work on this math...
        return (int)(y + (viewport.getCamera().position.y - (viewport.getWorldHeight() / 2f)));
    }

    /**
     * Converts from a screen x-coordinate to the equivalent x-coordinate in world space, which
     * takes into account the position of the viewport camera on the x-axis.
     * @param viewport The viewport with which the x-coordinate will be calculated.
     * @param x The screen x-coordinate to convert from.
     * @return The screen x-coordinate in world space.
     */
    public static int screenToWorldX(Viewport viewport, int x) {
        return viewportToWorldX(viewport, screenToViewportX(viewport, x));
    }

    /**
     * Converts from a screen y-coordinate to the equivalent y-coordinate in world space, which
     * takes into account the position of the viewport camera on the y-axis.
     * @param viewport The viewport with which the y-coordinate will be calculated.
     * @param y The screen y-coordinate to convert from.
     * @return The screen y-coordinate in world space.
     */
    public static int screenToWorldY(Viewport viewport, int y) {
        return viewportToWorldY(viewport, screenToViewportY(viewport, y));
    }

    /**
     * Converts from a world x-coordinate to the equivalent x-coordinate in viewport space, which
     * takes into account the position of the viewport camera on the x-axis.
     * @param viewport The viewport with which the x-coordinate will be calculated.
     * @param x The world x-coordinate to convert from.
     * @return The world x-coordinate in viewport space.
     */
    public static int worldToViewportX(Viewport viewport, int x) {
        return (int)(x - (viewport.getCamera().position.x - (viewport.getWorldWidth() / 2f)));
    }

    /**
     * Converts from a world y-coordinate to the equivalent y-coordinate in viewport space, which
     * takes into account the position of the viewport camera on the y-axis.
     * @param viewport The viewport with which the y-coordinate will be calculated.
     * @param y The world y-coordinate to convert from.
     * @return The world y-coordinate in viewport space.
     */
    public static int worldToViewportY(Viewport viewport, int y) {
        return (int)(y + (viewport.getCamera().position.y - (viewport.getWorldHeight() / 2f)));
    }

    /**
     * Converts from a viewport x-coordinate to the equivalent x-coordinate in screen space.
     * @param viewport The viewport with which the x-coordinate will be calculated.
     * @param x The viewport x-coordinate to convert from.
     * @return The viewport x-coordinate in screen space.
     */
    public static int viewportToScreenX(Viewport viewport, int x) {
        return (int)(((float)x / viewport.getWorldWidth()) * (float)Gdx.graphics.getWidth());
    }

    /**
     * Converts from a viewport y-coordinate to the equivalent x-coordinate in screen space.
     * @param viewport The viewport with which the y-coordinate will be calculated.
     * @param y The viewport y-coordinate to convert from.
     * @return The viewport y-coordinate in screen space.
     */
    public static int viewportToScreenY(Viewport viewport, int y) {
        return (int)(((float)y / viewport.getWorldHeight()) * (float)Gdx.graphics.getHeight());
    }

    /**
     * Converts from a world x-coordinate to the equivalent x-coordinate in screen space, which
     * takes into account the position of the viewport camera on the x-axis.
     * @param viewport The viewport with which the x-coordinate will be calculated.
     * @param x The world x-coordinate to convert from.
     * @return The world x-coordinate in screen space.
     */
    public static int worldToScreenX(Viewport viewport, int x) {
        return viewportToScreenX(viewport, worldToViewportX(viewport, x));
    }

    /**
     * Converts from a world y-coordinate to the equivalent y-coordinate in screen space, which
     * takes into account the position of the viewport camera on the y-axis.
     * @param viewport The viewport with which the y-coordinate will be calculated.
     * @param y The world y-coordinate to convert from.
     * @return The world y-coordinate in screen space.
     */
    public static int worldToScreenY(Viewport viewport, int y) {
        return viewportToScreenY(viewport, worldToViewportY(viewport, y));
    }

    /*
    public static int screenToSceneX(Viewport viewport, int x, boolean cameraAdjust) {
        int newX = (int)(((float)x / (float) Gdx.graphics.getWidth()) * viewport.getWorldWidth());
        if (cameraAdjust)
            newX += viewport.getCamera().position.x - (viewport.getWorldWidth() / 2f);
        return newX;
    }

    public static int screenToSceneY(Viewport viewport, int y, boolean cameraAdjust) {
        int newY = (int)(((float)y / (float)Gdx.graphics.getHeight()) * viewport.getWorldHeight());
        if (cameraAdjust)
            newY -= viewport.getCamera().position.y - (viewport.getWorldHeight() / 2f);
        return newY;
    }

    public static int sceneToScreenX(Viewport viewport, int x, boolean cameraAdjust) {
        if (cameraAdjust)
            x -= viewport.getCamera().position.x - (viewport.getWorldWidth() / 2f);
        return (int)(((float)x / viewport.getWorldWidth()) * (float)Gdx.graphics.getWidth());
    }

    public static int sceneToScreenY(Viewport viewport, int y, boolean cameraAdjust) {
        if (cameraAdjust)
            y += viewport.getCamera().position.y - (viewport.getWorldHeight() / 2f);
        return (int)(((float)y / viewport.getWorldHeight()) * (float)Gdx.graphics.getHeight());
    }
    */
}
