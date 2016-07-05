package com.kavanbickerstaff.floodgate.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Array;

public class UIWidget {

    private Array<UIWidget> children;

    public float localX, localY;
    public float width, height;
    private float transformX, transformY;

    public boolean visible = true;
    public boolean enabled = true;

    public UIWidget() {
        children = new Array<UIWidget>();
    }

    public void addChild(UIWidget widget) {
        children.add(widget);
    }

    protected void draw(Batch batch) {
        for (UIWidget child : children) {
            child.draw(batch);
        }
    }

    protected boolean touchDown(float screenX, float screenY) {
        for (UIWidget child : children) {
            if (child.checkAABB(screenX, screenY)) {
                if (child.touchDown(screenX, screenY)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean touchUp(float screenX, float screenY) {
        for (UIWidget child : children) {
            if (child.checkAABB(screenX, screenY)) {
                if (child.touchUp(screenX, screenY)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean touchDragged(float screenX, float screenY) {
        for (UIWidget child : children) {
            if (child.checkAABB(screenX, screenY)) {
                if (child.touchDragged(screenX, screenY)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected void updateTransform(float x, float y) {
        transformX = x + localX;
        transformY = y + localY;
        for (UIWidget child : children) {
            child.updateTransform(transformX, transformY);
        }
    }

    public boolean checkAABB(float screenX, float screenY) {
        return screenX >= transformX && screenX <= (transformX + width) &&
                screenY >= transformY && screenY <= (transformY + height);
    }

    public float getTransformX() {
        return transformX;
    }

    public float getTransformY() {
        return transformY;
    }
}
