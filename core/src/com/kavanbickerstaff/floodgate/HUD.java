package com.kavanbickerstaff.floodgate;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.kavanbickerstaff.floodgate.ui.UIImage;
import com.kavanbickerstaff.floodgate.ui.UIWidget;

public class HUD extends UIWidget {

    public static class Item {
        public int id;
        public float width, height;
        public TextureRegion textureRegion;
    }

    private Item[] itemsArray;
    private UIWidget[] slotsArray;
    private UIImage[] slotImages;

    private TextureRegion background;

    // TODO: This approach may be problematic with multiple placeables! Test it!
    private int selectedEntityId = -1;

    public HUD(TextureRegion background, int slots, int localX, int localY, int width, int height) {
        this.background = background;
        this.localX = localX;
        this.localY = localY;
        this.width = width;
        this.height = height;

        itemsArray = new Item[slots];
        slotsArray = new UIWidget[slots];
        slotImages = new UIImage[slots];

        int slotSize = width - 20;
        int currentX = 10;
        int currentY = height - slotSize - 10;
        for (int i = 0; i < slots; i++) {

            final int slotIndex = i;
            UIWidget slot = new UIWidget() {
                @Override
                protected void onTouchDown(float screenX, float screenY) {
                    Item item = itemsArray[slotIndex];
                    selectedEntityId = (item != null ? item.id : -1);
                }
            };
            slot.localX = currentX;
            slot.localY = currentY;
            slot.width = slotSize;
            slot.height = slotSize;
            slotsArray[i] = slot;

            UIImage slotImage = new UIImage(null, UIImage.ScaleType.FILL, 0, 0, 0, 0);
            slotImages[i] = slotImage;

            slot.addChild(slotImage);
            this.addChild(slot);

            currentY -= (slotSize + 10);
        }

        setAlpha(1);
    }

    public void update(Item[] items) {
        for (int i = 0; i < itemsArray.length; i++) {
            // Delete the old item displayed at the current element
            itemsArray[i] = null;

            // Place the items in slot positions identical to items array position
            if (i < items.length && items[i] != null) {
                itemsArray[i] = items[i];

                float scale = slotsArray[i].width / Math.max(itemsArray[i].width, itemsArray[i].height);
                slotImages[i].width = itemsArray[i].width * scale;
                slotImages[i].height = itemsArray[i].height * scale;
                slotImages[i].localX = (slotsArray[i].width - slotImages[i].width) / 2;
                slotImages[i].localY = (slotsArray[i].height - slotImages[i].height) / 2;

                slotImages[i].setRegion(items[i].textureRegion);
                slotImages[i].visible = true;
            } else {
                slotImages[i].visible = false;
            }
        }
    }

    @Override
    protected void onDraw(Batch batch) {
        batch.draw(background, getTransformX(), getTransformY(), width, height);
    }

    public int getSelectedEntityId() {
        return selectedEntityId;
    }

    public void resetSelectedEntityId() {
        selectedEntityId = -1;
    }

    public int getSlotCount() {
        return slotsArray.length;
    }
}
