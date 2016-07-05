package com.kavanbickerstaff.floodgate.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Array;

public class UIWidget {

    protected static boolean hasGlobalFocus;

    private Array<UIWidget> children;

    private boolean hasFocus;
    protected boolean isTouched;

    public float localX, localY;
    public float width, height;
    private float transformX, transformY;

    private float alpha;
    public boolean visible;
    public boolean enabled;

    public UIWidget() {
        children = new Array<UIWidget>();

        alpha = 1;
        visible = true;
        enabled = true;
    }

    public void addChild(UIWidget widget) {
        children.add(widget);
    }

    protected void onDraw(Batch batch) {}
    protected void onTouchDown(float screenX, float screenY) {}
    protected void onTouchUp(float screenX, float screenY) {}
    protected void onTouchDragged(float screenX, float screenY) {}
    protected void onTouchEntered() {}
    protected void onTouchExited() {}

    protected final void draw(Batch batch) {
        float oldAlpha = batch.getColor().a;
        batch.setColor(1, 1, 1, alpha);

        onDraw(batch);
        for (UIWidget child : children) {
            child.draw(batch);
        }

        batch.setColor(1, 1, 1, oldAlpha);
    }

    protected final void touchDown(float screenX, float screenY) {
        onTouchDown(screenX, screenY);

        for (UIWidget child : children) {
            if (child.checkAABB(screenX, screenY)) {
                child.hasFocus = true;
                child.isTouched = true;
                hasGlobalFocus = true;

                child.touchDown(screenX, screenY);
            }
        }
    }

    protected final void touchUp(float screenX, float screenY) {
        onTouchUp(screenX, screenY);

        for (UIWidget child : children) {
            if (child.hasFocus && child.isTouched) {
                child.touchUp(screenX, screenY);
            }

            child.hasFocus = false;
            child.isTouched = false;
            hasGlobalFocus = false;
        }
    }

    protected final void touchDragged(float screenX, float screenY) {
        onTouchDragged(screenX, screenY);

        for (UIWidget child : children) {

            // If the child has focus
            if (child.hasFocus) {
                // If the child is currently being touched
                if (child.isTouched) {
                    // If the touch is still within the the widget's box
                    if (child.checkAABB(screenX, screenY)) {
                        child.touchDragged(screenX, screenY);
                    } else {
                        // The touch was just dragged out of the widget's box
                        child.isTouched = false;
                        child.onTouchExited();
                    }
                } else {
                    // If the touch is dragged into the focused widget's box
                    if (child.checkAABB(screenX, screenY)) {
                        child.isTouched = true;
                        child.onTouchEntered();
                        child.touchDragged(screenX, screenY);
                    }
                }
            }
        }
    }

    protected final void updateTransform(float x, float y) {
        transformX = x + localX;
        transformY = y + localY;
        for (UIWidget child : children) {
            child.updateTransform(transformX, transformY);
        }
    }

    public final boolean checkAABB(float screenX, float screenY) {
        return screenX >= transformX && screenX <= (transformX + width) &&
                screenY >= transformY && screenY <= (transformY + height);
    }

    public final void setAlpha(float value) {
        alpha = value;
        for (UIWidget child : children) {
            child.setAlpha(value);
        }
    }

    public final float getTransformX() {
        return transformX;
    }

    public final float getTransformY() {
        return transformY;
    }
}
