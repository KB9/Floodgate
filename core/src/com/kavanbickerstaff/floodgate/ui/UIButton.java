package com.kavanbickerstaff.floodgate.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public abstract class UIButton extends UI.Widget {

    protected float lastPressX, lastPressY;
    protected TextureRegion region;

    public UIButton(TextureRegion region, float localX, float localY, float width, float height) {
        super(localX, localY, width, height);

        this.region = region;
    }

    public UIButton(TextureRegion region, float x, float y) {
        this(region, x, y, region.getRegionWidth(), region.getRegionHeight());
    }

    @Override
    public void onDraw(Batch batch) {
        if (region != null && visible) {
            if (isTouched) {
                batch.draw(region, getTransformX() + 10, getTransformY() + 10, width - 20, width - 20);
            } else {
                batch.draw(region, getTransformX(), getTransformY(), width, height);
            }
        }
    }

    @Override
    protected void onTouchDown(float screenX, float screenY) {
        lastPressX = screenX - getTransformX();
        lastPressY = screenY - getTransformY();
        onClick();
    }

    @Override
    protected void onTouchUp(float screenX, float screenY) {
        onRelease();
    }

    public abstract void onClick();
    public abstract void onRelease();
}
