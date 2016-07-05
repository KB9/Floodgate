package com.kavanbickerstaff.floodgate;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.kavanbickerstaff.floodgate.ui.UIButton;
import com.kavanbickerstaff.floodgate.ui.UIImage;
import com.kavanbickerstaff.floodgate.ui.UIWidget;

public class HUD extends UIWidget {

    public static class Item {
        public int id;
        public float width, height;
        public TextureRegion textureRegion;
    }

    private UIImage background;
    private int slots;

    private Item[] itemsArray;
    private UIButton[] slotButtons;

    private float alpha;

    // TODO: This approach may be problematic with multiple placeables! Test it!
    private int selectedEntityId = -1;

    public HUD(TextureRegion background, int slots, int localX, int localY, int width, int height) {
        this.slots = slots;
        this.localX = localX;
        this.localY = localY;
        this.width = width;
        this.height = height;

        this.background = new UIImage(background, UIImage.ScaleType.FILL, 0, 0, width, height);
        addChild(this.background);

        itemsArray = new Item[slots];
        slotButtons = new UIButton[slots];

        int slotSize = width - 20;
        int currentX = 10;
        int currentY = height - slotSize - 10;
        for (int i = 0; i < slots; i++) {

            final int slotIndex = i;
            UIButton slotButton = new UIButton(null, currentX, currentY, slotSize, slotSize) {
                @Override
                public void onClick() {
                    Item item = itemsArray[slotIndex];
                    selectedEntityId = (item != null ? item.id : -1);

                    this.isPressed = false;
                }

                @Override
                public void onRelease() {

                }
            };
            addChild(slotButton);
            slotButtons[i] = slotButton;

            currentY -= (slotSize + 10);
        }

        alpha = 1;
    }

    public void update(Item[] items) {
        for (int i = 0; i < itemsArray.length; i++) {
            // Delete the old item displayed at the current element
            itemsArray[i] = null;

            // Place the items in slot positions identical to items array position
            if (i < items.length && items[i] != null) {
                itemsArray[i] = items[i];
                slotButtons[i].region = items[i].textureRegion;
                slotButtons[i].visible = true;
            } else {
                slotButtons[i].visible = false;
            }
        }
    }

    public int getSelectedEntityId() {
        return selectedEntityId;
    }

    public void resetSelectedEntityId() {
        selectedEntityId = -1;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public int getSlotCount() {
        return slots;
    }
}
