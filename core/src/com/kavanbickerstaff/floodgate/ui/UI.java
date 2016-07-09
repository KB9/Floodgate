package com.kavanbickerstaff.floodgate.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Array;

public class UI {

    public static class Widget {

        protected Array<Widget> children;

        private boolean hasFocus;
        protected boolean isTouched;

        public float localX, localY;
        public float width, height;
        private float transformX, transformY;

        private Color color;
        public boolean visible;
        public boolean enabled;

        public Widget(float localX, float localY, float width, float height) {
            this.localX = localX;
            this.localY = localY;
            this.width = width;
            this.height = height;

            children = new Array<Widget>();
            color = new Color(1, 1, 1, 1);
            visible = true;
            enabled = true;
        }

        public void addChild(Widget widget) {
            children.add(widget);
        }

        protected void onDraw(Batch batch) {}
        protected void onTouchDown(float screenX, float screenY) {}
        protected void onTouchUp(float screenX, float screenY) {}
        protected void onTouchDragged(float screenX, float screenY) {}
        protected void onTouchEntered(float screenX, float screenY) {}
        protected void onTouchExited(float screenX, float screenY) {}
        protected void onFocusLost() {}

        private void draw(Batch batch) {
            float oldR = batch.getColor().r;
            float oldG = batch.getColor().g;
            float oldB = batch.getColor().b;
            float oldA = batch.getColor().a;

            batch.setColor(color);
            if (visible) onDraw(batch);
            for (Widget child : children) {
                child.draw(batch);
            }

            batch.setColor(oldR, oldG, oldB, oldA);
        }

        private void touchDown(float screenX, float screenY) {
            if (enabled) onTouchDown(screenX, screenY);

            for (Widget child : children) {
                if (child.checkAABB(screenX, screenY)) {
                    child.hasFocus = true;
                    child.isTouched = true;

                    child.touchDown(screenX, screenY);
                }
            }
        }

        private void touchUp(float screenX, float screenY) {
            if (enabled) onTouchUp(screenX, screenY);

            for (Widget child : children) {
                if (child.hasFocus && child.isTouched) {
                    child.touchUp(screenX, screenY);
                }

                child.isTouched = false;
                child.loseFocus();
            }
        }

        private void touchDragged(float screenX, float screenY) {
            if (enabled) onTouchDragged(screenX, screenY);

            for (Widget child : children) {

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
                            child.touchExited(screenX, screenY);
                        }
                    } else {
                        // If the touch is dragged into the focused widget's box
                        if (child.checkAABB(screenX, screenY)) {
                            child.isTouched = true;
                            child.touchEntered(screenX, screenY);
                            child.touchDragged(screenX, screenY);
                        }
                    }
                }
            }
        }

        private void touchEntered(float screenX, float screenY) {
            if (enabled) onTouchEntered(screenX, screenY);

            for (Widget child : children) {
                if (child.checkAABB(screenX, screenY)) {
                    child.isTouched = true;
                    child.touchEntered(screenX, screenY);
                }
            }
        }

        private void touchExited(float screenX, float screenY) {
            if (enabled) onTouchExited(screenX, screenY);

            for (Widget child : children) {
                if (!child.checkAABB(screenX, screenY) && child.isTouched) {
                    child.isTouched = false;
                    child.touchExited(screenX, screenY);
                }
            }
        }

        private void loseFocus() {
            onFocusLost();

            hasFocus = false;
            for (Widget child : children) {
                if (child.hasFocus) {
                    child.loseFocus();
                }
            }
        }

        protected void updateTransform(float x, float y) {
            transformX = x + localX;
            transformY = y + localY;
            for (Widget child : children) {
                child.updateTransform(transformX, transformY);
            }
        }

        public final boolean checkAABB(float screenX, float screenY) {
            return screenX >= transformX && screenX <= (transformX + width) &&
                    screenY >= transformY && screenY <= (transformY + height);
        }

        public final void setColor(float r, float g, float b, float a) {
            color.set(r, g, b, a);
            for (Widget child : children) {
                child.setColor(r, g, b, a);
            }
        }

        public final float getTransformX() {
            return transformX;
        }

        public final float getTransformY() {
            return transformY;
        }
    }

    private Widget root;

    public UI() {
        root = new Widget(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    public void addWidget(Widget widget) {
        root.addChild(widget);
    }

    public void touchDown(float screenX, float screenY) {
        root.touchDown(screenX, screenY);
    }

    public void touchUp(float screenX, float screenY) {
        root.touchUp(screenX, screenY);
        root.loseFocus();
    }

    public void touchDragged(float screenX, float screenY) {
        root.touchDragged(screenX, screenY);
    }

    public void render(Batch batch) {
        root.updateTransform(0, 0);
        root.draw(batch);
    }

    public boolean hasFocus() {
        return checkForFocus(root);
    }

    private boolean checkForFocus(Widget widget) {
        for (Widget child : widget.children) {
            if(child.hasFocus || checkForFocus(child)) {
                return true;
            }
        }
        return false;
    }
}
