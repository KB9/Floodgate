package com.kavanbickerstaff.floodgate;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.viewport.Viewport;

public class ViewportUtils {

    public static int screenToSceneX(Viewport viewport, int x) {
        int newX = (int)(((float)x / (float) Gdx.graphics.getWidth()) * viewport.getWorldWidth());
        newX += viewport.getCamera().position.x - (viewport.getWorldWidth() / 2f);
        return newX;
    }

    public static int screenToSceneY(Viewport viewport, int y) {
        int newY = (int)(((float)y / (float)Gdx.graphics.getHeight()) * viewport.getWorldHeight());
        newY -= viewport.getCamera().position.y - (viewport.getWorldHeight() / 2f);
        return newY;
    }

    public static int sceneToScreenX(Viewport viewport, int x) {
        x -= viewport.getCamera().position.x - (viewport.getWorldWidth() / 2f);
        return (int)(((float)x / viewport.getWorldWidth()) * (float)Gdx.graphics.getWidth());
    }

    public static int sceneToScreenY(Viewport viewport, int y) {
        y += viewport.getCamera().position.y - (viewport.getWorldHeight() / 2f);
        return (int)(((float)y / viewport.getWorldHeight()) * (float)Gdx.graphics.getHeight());
    }
}
