package com.kavanbickerstaff.floodgate;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.IntArray;

public class CameraController {

    private OrthographicCamera camera;

    private int lastX, lastY;
    private int panPointer = -1;
    private float scrollSpeed;

    private int pointerCount;
    private boolean[] activePointers = new boolean[20];

    private IntArray zoomPointers = new IntArray();
    private float zoomSpeed;
    private float zoomInLimit, zoomOutLimit;
    private float lastDistance;

    public CameraController(OrthographicCamera camera) {
        this.camera = camera;

        scrollSpeed = 1;

        activePointers = new boolean[20];

        zoomPointers = new IntArray();
        zoomSpeed = 1;
        zoomInLimit = 0;
        zoomOutLimit = 1;
    }

    public void touchDown(int screenX, int screenY, int pointer) {
        activePointers[pointer] = true;
        pointerCount++;

        // If there is space for another zoom pointer, use the current pointer index
        if (zoomPointers.size < 2) {
            zoomPointers.add(pointer);

            // If two zoom pointers have now been added, calculated the distance between them
            if (zoomPointers.size == 2) {
                lastDistance = Vector2.dst(
                        Gdx.input.getX(zoomPointers.get(0)), Gdx.input.getY(zoomPointers.get(0)),
                        Gdx.input.getX(zoomPointers.get(1)), Gdx.input.getY(zoomPointers.get(1))
                );
            }
        }

        if (pointerCount == 1) {
            // Make this pointer index the pan pointer
            panPointer = pointer;
            lastX = screenX;
            lastY = screenY;
        }
    }

    public void touchUp(int screenX, int screenY, int pointer) {
        activePointers[pointer] = false;
        pointerCount--;

        // If this pointer was used for zooming, swap it for another available pointer
        if (zoomPointers.contains(pointer)) {
            if (pointerCount > 1) swapZoomPointer(pointer);
            zoomPointers.removeValue(pointer);
        }

        // If this pointer was used for panning, swap it for another available pointer
        if (pointer == panPointer && pointerCount >= 1) swapPanPointer();
    }

    public void touchDragged(int screenX, int screenY, int pointer) {
        switch (pointerCount) {
            case 1: {
                float deltaX = lastX - Gdx.input.getX(panPointer);
                float deltaY = Gdx.input.getY(panPointer) - lastY;
                camera.position.add(
                        deltaX * scrollSpeed * camera.zoom,
                        deltaY * scrollSpeed * camera.zoom,
                        0
                );
            }
            break;

            case 2: {
                // If the number of zoom pointers is not 2, no zooming should be performed.
                if (zoomPointers.size >= 2) {
                    // Get the distance between the two zooming pointers
                    float distance = Vector2.dst(
                            Gdx.input.getX(zoomPointers.get(0)), Gdx.input.getY(zoomPointers.get(0)),
                            Gdx.input.getX(zoomPointers.get(1)), Gdx.input.getY(zoomPointers.get(1))
                    );

                    // Move the camera by the calculated delta
                    float delta = (lastDistance - distance) * zoomSpeed;
                    camera.zoom += delta;
                    if (camera.zoom < zoomInLimit) camera.zoom = zoomInLimit;
                    if (camera.zoom > zoomOutLimit) camera.zoom = zoomOutLimit;

                    lastDistance = distance;
                }
            }
            break;
        }

        // Record the last position of the panning pointer
        lastX = Gdx.input.getX(panPointer);
        lastY = Gdx.input.getY(panPointer);
    }

    private void swapPanPointer() {
        for (int i = 0; i < 20; i++) {
            if (activePointers[i]) {
                panPointer = i;
                lastX = Gdx.input.getX(i);
                lastY = Gdx.input.getY(i);
                break;
            }
        }
    }

    private void swapZoomPointer(int pointer) {
        for (int i = 0; i < 20; i++) {
            if (activePointers[i] && !zoomPointers.contains(i)) {
                zoomPointers.set(zoomPointers.indexOf(pointer), i);

                lastDistance = Vector2.dst(
                        Gdx.input.getX(zoomPointers.get(0)), Gdx.input.getY(zoomPointers.get(0)),
                        Gdx.input.getX(zoomPointers.get(1)), Gdx.input.getY(zoomPointers.get(1))
                );
                break;
            }
        }
    }

    public void setScrollSpeed(float scrollSpeed) {
        this.scrollSpeed = scrollSpeed;
    }

    public void setZoomLimits(float zoomInLimit, float zoomOutLimit) {
        this.zoomInLimit = zoomInLimit;
        this.zoomOutLimit = zoomOutLimit;
    }

    public void setZoomSpeed(float zoomSpeed) {
        this.zoomSpeed = zoomSpeed;
    }

    public int getPointerCount() {
        return pointerCount;
    }
}
