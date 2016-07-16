package com.kavanbickerstaff.floodgate.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;

public class UIText extends UI.Widget {

    private BitmapFont font;
    private GlyphLayout layout;
    private String text;

    public UIText(BitmapFont font, String text, float localX, float localY) {
        super(localX, localY, 0, 0);
        this.font = font;
        this.text = text;

        layout = new GlyphLayout();
        layout.setText(font, text);

        this.width = layout.width;
        this.height = layout.height;
    }

    public void setText(String text) {
        this.text = text;

        layout.setText(font, text);
        this.width = layout.width;
        this.height = layout.height;
    }

    @Override
    protected void onDraw(Batch batch) {
        font.draw(batch, text, getTransformX() - (width / 2f), getTransformY() + (height / 2f));
    }
}
