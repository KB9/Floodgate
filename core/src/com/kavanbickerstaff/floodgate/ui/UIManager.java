package com.kavanbickerstaff.floodgate.ui;

import com.badlogic.gdx.graphics.g2d.Batch;

public class UIManager {

    private boolean hasFocus;

    private UIWidget rootWidget;

    public UIManager() {
        rootWidget = new UIWidget();
    }

    public void touchDown(int screenX, int screenY) {
        hasFocus = rootWidget.touchDown(screenX, screenY);
    }

    public void touchUp(int screenX, int screenY) {
        rootWidget.touchUp(screenX, screenY);
        hasFocus = false;
    }

    public void touchDragged(int screenX, int screenY) {
        rootWidget.touchDragged(screenX, screenY);
    }

    public void render(Batch batch) {
        rootWidget.updateTransform(0, 0);
        rootWidget.draw(batch);
    }

    public void addWidget(UIWidget widget) {
        rootWidget.addChild(widget);
    }

    public boolean hasFocus() {
        return hasFocus;
    }
}
