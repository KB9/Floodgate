package com.kavanbickerstaff.floodgate;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.kavanbickerstaff.floodgate.ui.UI;
import com.kavanbickerstaff.floodgate.ui.UIImage;
import com.kavanbickerstaff.floodgate.ui.UIScroller;

public class HUD extends UI.Widget {

    public static class Item {
        public int id;
        public float width, height;
        public TextureRegion textureRegion;
    }

    private Item[] itemsArray;
    private UI.Widget[] slotsArray;
    private UIImage[] slotImages;

    private TextureRegion background;

    // TODO: This approach may be problematic with multiple placeables! Test it!
    private int selectedEntityId = -1;

    public HUD(TextureRegion background, int slots, int localX, int localY, int width, int height) {
        super(localX, localY, width, height);

        this.background = background;

        itemsArray = new Item[slots];
        slotsArray = new UI.Widget[slots];
        slotImages = new UIImage[slots];

        // Create a scroller which only scrolls if an inventory item is not selected
        UIScroller scroller = new UIScroller(UIScroller.ScrollType.VERTICAL, 0, 0, width, height) {
            @Override
            protected void onTouchDragged(float screenX, float screenY) {
                this.enabled = selectedEntityId == -1;
                super.onTouchDragged(screenX, screenY);
            }

            @Override
            protected void onTouchUp(float screenX, float screenY) {
                this.enabled = true;
                super.onTouchUp(screenX, screenY);
            }

            @Override
            protected void onFocusLost() {
                this.enabled = true;
            }
        };
        addChild(scroller);

        int slotPadding = 10;
        int slotSize = width - (slotPadding * 2);
        int currentX = slotPadding;
        int currentY = height - slotSize - slotPadding;
        for (int i = 0; i < slots; i++) {

            // Create a widget which sets the selected entity ID when touched down
            final int slotIndex = i;
            UI.Widget slot = new UI.Widget(currentX, currentY, slotSize, slotSize) {
                @Override
                protected void onTouchDown(float screenX, float screenY) {
                    Item item = itemsArray[slotIndex];
                    selectedEntityId = (item != null ? item.id : -1);
                }
            };
            slotsArray[i] = slot;

            // Create an image for slot - image can be scaled according to its physical dimensions
            UIImage slotImage = new UIImage(null, UIImage.ScaleType.FILL, 0, 0, 0, 0);
            slotImages[i] = slotImage;

            // Add image to slot, add slot to scroller
            slot.addChild(slotImage);
            scroller.addChild(slot);

            currentY -= (slotSize + slotPadding);
        }

        // Set the max scrolling limit of the slots
        scroller.setVScrollBounds(-height + (slotSize + slotPadding) * slots, 0);

        setColor(1, 1, 1, 1);
    }

    public void update(Item[] items) {
        for (int i = 0; i < itemsArray.length; i++) {
            // Delete the old item displayed at the current element
            itemsArray[i] = null;

            // Place the items in slot positions identical to items array position
            if (i < items.length && items[i] != null) {
                itemsArray[i] = items[i];

                // Calculate size that slot image should be displayed at
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
