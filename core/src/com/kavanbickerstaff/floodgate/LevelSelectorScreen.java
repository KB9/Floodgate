package com.kavanbickerstaff.floodgate;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.kavanbickerstaff.floodgate.ui.UI;
import com.kavanbickerstaff.floodgate.ui.UIButton;
import com.kavanbickerstaff.floodgate.ui.UIScroller;
import com.kavanbickerstaff.floodgate.ui.UIText;

public class LevelSelectorScreen implements Screen, InputProcessor {

    private SpriteBatch batch;
    private UI ui;
    private Texture background;

    private Floodgate game;

    public LevelSelectorScreen(final Floodgate game) {
        this.game = game;

        batch = new SpriteBatch();
        ui = new UI();

        background = new Texture(Gdx.files.internal("sewer_background.png"));

        // FreeType font initialization
        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/arial.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter fontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        fontParameter.size = 50;
        BitmapFont font = fontGenerator.generateFont(fontParameter);
        fontGenerator.dispose();

        // Create scroller for scrolling through level selection buttons
        final UIScroller scroller = new UIScroller(UIScroller.ScrollType.VERTICAL,
                0, 0,
                Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        ui.addWidget(scroller);

        // Create buttons and button numbers for selecting levels
        int buttonColumns = 5;
        int buttonRows = 6;
        float buttonWidth = Gdx.graphics.getWidth() / (buttonColumns + 1);
        float buttonHeight = buttonWidth;
        TextureRegion buttonTexture = new TextureRegion(
                new Texture(Gdx.files.internal("glossy_black_circle_button.png"))
        );

        // Loop through all rows/columns and put buttons/text in each
        for (int row = 0; row < buttonRows; row++) {
            for (int col = 0; col < buttonColumns; col++) {
                final int buttonIndex = (row * buttonColumns) + col;

                float buttonX = (buttonWidth / 2f) + (col * buttonWidth);
                float buttonY = ((buttonRows - 1) * buttonHeight) - row * buttonHeight;

                LevelSelectorButton button = new LevelSelectorButton(
                        "Level_" + (buttonIndex + 1),
                        buttonTexture, buttonX, buttonY, buttonWidth, buttonHeight
                );
                scroller.addChild(button);

                UIText buttonText = new UIText(
                        font, String.valueOf(buttonIndex + 1),
                        button.width / 2f, button.height / 2f
                );
                button.addChild(buttonText);
            }
        }

        // Apply scrolling boundaries and set scroll position to start from the top
        scroller.setVScrollBounds(Gdx.graphics.getHeight() - (buttonRows * buttonHeight), 0);
        scroller.setScrollPos(0, Gdx.graphics.getHeight() - (buttonRows * buttonHeight));
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        ui.render(batch);

        batch.end();
    }

    private boolean doesLevelExist(String levelName) {
        return Gdx.files.internal("scenes/" + levelName + ".dt").exists();
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
        batch.dispose();
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        ui.touchDown(screenX, Gdx.graphics.getHeight() - screenY);
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        ui.touchUp(screenX, Gdx.graphics.getHeight() - screenY);
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        ui.touchDragged(screenX, Gdx.graphics.getHeight() - screenY);
        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

    private class LevelSelectorButton extends UIButton {

        private final float TOUCH_DRAG_THRESHOLD = 0.05f;
        private float startPercentX, startPercentY;
        private boolean tempDisabled;

        private String levelName;

        public LevelSelectorButton(String levelName, TextureRegion region,
                                   float localX, float localY, float width, float height) {
            super(region, localX, localY, width, height);

            this.levelName = levelName;
        }

        @Override
        protected void onTouchDown(float screenX, float screenY) {
            super.onTouchDown(screenX, screenY);

            // Get position of touch as a percentage of screen dimensions
            startPercentX = screenX / Gdx.graphics.getWidth();
            startPercentY = screenY / Gdx.graphics.getHeight();

            tempDisabled = false;
        }

        @Override
        protected void onTouchDragged(float screenX, float screenY) {
            if (!tempDisabled) {
                // Get position of touch as a percentage of screen dimensions
                float currentPercentX = screenX / Gdx.graphics.getWidth();
                float currentPercentY = screenY / Gdx.graphics.getHeight();

                // Get distance of vector between touch down and current touch position
                double dist = Math.sqrt(
                        Math.pow(currentPercentX - startPercentX, 2) + Math.pow(currentPercentY - startPercentY, 2)
                );

                // If distance is greater than threshold, temp disable this button
                if (dist > TOUCH_DRAG_THRESHOLD) {
                    isTouched = false;
                    tempDisabled = true;
                }
            } else {
                // If temp disabled, stop re-touching caused by button remaining in focus
                isTouched = false;
            }
        }

        @Override
        public void onClick() {

        }

        @Override
        public void onRelease() {
            if (doesLevelExist(levelName)) {
                game.setScreen(new GameScreen(game, levelName));
            } else {
                Gdx.app.log("LevelSelector", "\"" + levelName + "\" does not exist!");
            }
        }
    }
}
