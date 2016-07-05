package com.kavanbickerstaff.floodgate.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class UIImage extends UIWidget {

    public enum ScaleType {
        FILL,
        FIT
    }

    private TextureRegion region;
    private ScaleType scaleType;

    public UIImage(TextureRegion region, ScaleType scaleType, float localX, float localY, float width, float height) {
        this.region = region;
        this.scaleType = scaleType;
        this.localX = localX;
        this.localY = localY;
        this.width = width;
        this.height = height;
    }

    @Override
    public void draw(Batch batch) {
        if (region != null) {
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
        super.draw(batch);
    }

    private float getScaleFactor(float width, float height, float realWidth, float realHeight) {
        return Math.min(width / realWidth, height / realHeight);
    }
}
