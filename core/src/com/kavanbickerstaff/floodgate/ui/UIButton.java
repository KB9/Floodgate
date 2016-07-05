package com.kavanbickerstaff.floodgate.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public abstract class UIButton extends UIWidget {

    protected boolean isPressed;
    protected float lastPressX, lastPressY;
    public TextureRegion region;

    public UIButton(TextureRegion region, float localX, float localY, float width, float height) {
        this.region = region;
        this.localX = localX;
        this.localY = localY;
        this.width = width;
        this.height = height;
    }

    public UIButton(TextureRegion region, float x, float y) {
        this(region, x, y, region.getRegionWidth(), region.getRegionHeight());
    }

    public UIButton(TextureRegion region) {
        this(region, 0, 0, region.getRegionWidth(), region.getRegionHeight());
    }

    @Override
    public void draw(Batch batch) {
        if (region != null && visible) {
            if (isPressed) {
                batch.draw(region, getTransformX() + 10, getTransformY() + 10, width - 20, width - 20);
            } else {
                batch.draw(region, getTransformX(), getTransformY(), width, height);
            }
        }
        super.draw(batch);
    }

    @Override
    protected boolean touchDown(float screenX, float screenY) {
        isPressed = true;
        lastPressX = screenX - getTransformX();
        lastPressY = screenY - getTransformY();
        onClick();

        return true;
    }

    @Override
    protected boolean touchUp(float screenX, float screenY) {
        isPressed = false;
        onRelease();

        return true;
    }

    public abstract void onClick();
    public abstract void onRelease();
}
