package com.kavanbickerstaff.floodgate.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class UIImage extends UI.Widget {

    public enum ScaleType {
        FILL,
        FIT
    }

    private TextureRegion region;
    private ScaleType scaleType;

    public UIImage(TextureRegion region, ScaleType scaleType, float localX, float localY, float width, float height) {
        super(localX, localY, width, height);

        this.region = region;
        this.scaleType = scaleType;
    }

    public UIImage(TextureRegion region, ScaleType scaleType, float x, float y) {
        this(region, scaleType, x, y, region.getRegionWidth(), region.getRegionHeight());
    }

    @Override
    public void onDraw(Batch batch) {
        if (region != null && visible) {
            switch (scaleType) {
                case FILL: {
                    batch.draw(region, getTransformX(), getTransformY(), width, height);
                }
                break;

                case FIT: {
                    float scale = getScaleFactor(width, height, region.getRegionWidth(), region.getRegionHeight());
                    float scaledWidth = width * scale;
                    float scaledHeight = height * scale;
                    batch.draw(region,
                            getTransformX() + ((width - scaledWidth) / 2), getTransformY() + ((height - scaledHeight) / 2),
                            scaledWidth, scaledHeight
                    );
                }
                break;
            }
        }
    }

    private float getScaleFactor(float width, float height, float realWidth, float realHeight) {
        return Math.min(width / realWidth, height / realHeight);
    }

    public void setRegion(TextureRegion region) {
        this.region = region;
    }
}
