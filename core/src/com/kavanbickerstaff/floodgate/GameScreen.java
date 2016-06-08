package com.kavanbickerstaff.floodgate;

import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.uwsoft.editor.renderer.SceneLoader;

public class GameScreen implements Screen {

    private final Floodgate game;

    private SceneLoader sceneLoader;
    private PooledEngine engine;

    public GameScreen(final Floodgate game) {
        this.game = game;

        FitViewport viewport = new FitViewport(800, 480);
        sceneLoader = new SceneLoader();
        sceneLoader.loadScene("MainScene", viewport);

        engine = new PooledEngine();
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {

    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}
