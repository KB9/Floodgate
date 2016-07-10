package com.kavanbickerstaff.floodgate;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.kavanbickerstaff.floodgate.ui.UI;
import com.kavanbickerstaff.floodgate.ui.UIButton;
import com.kavanbickerstaff.floodgate.ui.UIScroller;

public class LevelSelectorScreen implements Screen, InputProcessor {

    private SpriteBatch batch;
    private UI ui;

    private Texture background;

    private BitmapFont font;
    private GlyphLayout layout;

    private UIButton[] buttons;

    public LevelSelectorScreen(final Floodgate game) {
        batch = new SpriteBatch();
        ui = new UI();

        background = new Texture(Gdx.files.internal("sewer_background.png"));

        // FreeType font initialization
        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/arial.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter fontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        fontParameter.size = 50;
        font = fontGenerator.generateFont(fontParameter);
        fontGenerator.dispose();

        layout = new GlyphLayout();

        UIScroller scroller = new UIScroller(UIScroller.ScrollType.VERTICAL,
                0, 0,
                Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        ui.addWidget(scroller);

        int buttonColumns = 5;
        int buttonRows = 5;
        buttons = new UIButton[buttonColumns * buttonRows];

        float buttonWidth = Gdx.graphics.getWidth() / (buttonColumns + 1);
        float buttonHeight = buttonWidth;
        TextureRegion buttonTexture = new TextureRegion(
                new Texture(Gdx.files.internal("glossy_black_circle_button.png")));

        for (int row = 0; row < buttonRows; row++) {
            for (int col = 0; col < buttonColumns; col++) {
                final int buttonIndex = (row * buttonColumns) + col;

                float buttonX = (buttonWidth / 2f) + (col * buttonWidth);
                float buttonY = ((buttonRows - 1) * buttonHeight) - row * buttonHeight;

                UIButton button = new UIButton(buttonTexture, buttonX, buttonY, buttonWidth, buttonHeight) {
                    @Override
                    public void onClick() {

                    }

                    @Override
                    public void onRelease() {
                        int levelIndex = buttonIndex + 1;
                        String levelName = "Level_" + levelIndex;

                        if (doesLevelExist(levelName)) {
                            game.setScreen(new GameScreen(game, levelName));
                        } else {
                            Gdx.app.log("LevelSelector", "\"" + levelName + "\" does not exist!");
                        }
                    }
                };
                buttons[buttonIndex] = button;
                scroller.addChild(button);
            }
        }

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
        for (int i = 0; i < buttons.length; i++) {
            String levelName = String.valueOf(i + 1);

            layout.setText(font, levelName);
            float textWidth = layout.width;
            float textHeight = layout.height;

            float textX = buttons[i].getTransformX() +
                    (buttons[i].width - textWidth) / 2f;
            float textY = buttons[i].getTransformY() +
                    (buttons[i].height + textHeight) / 2f;

            font.draw(batch, levelName, textX, textY);
        }

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
}
