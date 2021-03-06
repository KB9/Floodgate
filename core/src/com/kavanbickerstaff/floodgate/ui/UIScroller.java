package com.kavanbickerstaff.floodgate.ui;

import com.badlogic.gdx.math.MathUtils;

public class UIScroller extends UI.Widget {

    public enum ScrollType {
        HORIZONTAL,
        VERTICAL,
        ALL
    }

    private ScrollType scrollType;
    private float scrollX, scrollY;
    private float minX, minY;
    private float maxX, maxY;

    private float lastX, lastY;

    public UIScroller(ScrollType scrollType, float localX, float localY, float width, float height) {
        super(localX, localY, width, height);

        this.scrollType = scrollType;

        minX = 0;
        maxX = width;
        minY = -height;
        maxY = 0;
    }

    public void setScrollPos(float x, float y) {
        scrollX = x;
        scrollY = y;
    }

    public void setHScrollBounds(float minX, float maxX) {
        this.minX = minX;
        this.maxX = maxX;
    }

    public void setVScrollBounds(float minY, float maxY) {
        this.minY = minY;
        this.maxY = maxY;
    }

    @Override
    protected void onTouchDown(float screenX, float screenY) {
        lastX = screenX;
        lastY = screenY;
    }

    @Override
    protected void onTouchDragged(float screenX, float screenY) {
        float deltaX = screenX - lastX;
        float deltaY = screenY - lastY;

        // Apply scroll direction
        if (scrollType == ScrollType.VERTICAL) deltaX = 0;
        if (scrollType == ScrollType.HORIZONTAL) deltaY = 0;

        // Apply scroll deltas to scroll positions (with clamping)
        scrollX = MathUtils.clamp(scrollX + deltaX, minX, maxX);
        scrollY = MathUtils.clamp(scrollY + deltaY, minY, maxY);

        lastX = screenX;
        lastY = screenY;
    }

    @Override
    protected void onTouchEntered(float screenX, float screenY) {
        lastX = screenX;
        lastY = screenY;
    }

    @Override
    protected void updateTransform(float x, float y) {
        super.updateTransform(x, y);
        for (UI.Widget child : children) {
            child.updateTransform(x + scrollX, y + scrollY);
        }
    }
}
