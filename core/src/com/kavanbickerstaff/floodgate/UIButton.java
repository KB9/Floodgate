package com.kavanbickerstaff.floodgate;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;

public abstract class UIButton {

    public float x, y;
    public float width, height;
    public boolean isPressed;
    private Texture texture;

    public UIButton(Texture texture, float x, float y, float width, float height) {
        this.texture = texture;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public UIButton(Texture texture, float x, float y) {
        this(texture, x, y, texture.getWidth(), texture.getHeight());
    }

    public UIButton(Texture texture) {
        this(texture, 0, 0, texture.getWidth(), texture.getHeight());
    }

    public void draw(Batch batch) {
        batch.draw(texture, x, y, width, height);
    }

    public abstract void onClick();
    public abstract void onRelease();
}
