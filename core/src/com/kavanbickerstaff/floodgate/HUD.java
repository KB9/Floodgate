package com.kavanbickerstaff.floodgate;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import java.util.HashMap;

public class HUD {

    public static class Item {
        public float width, height;
        public TextureRegion textureRegion;
    }

    private class Slot {
        public int index;
        public Item item;
        public int x, y;
        public int size;
    }

    private Texture background;
    private int x, y;
    private int width, height;

    private SpriteBatch batch;

    private Array<Slot> slotArray;
    private HashMap<Integer, Item> itemMap;
    private HashMap<Item, Integer> idMap;

    private float alpha;

    public HUD(Texture background, int slots,
               int x, int y, int width, int height) {
        this.background = background;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        batch = new SpriteBatch();
        slotArray = new Array<Slot>();
        itemMap = new HashMap<Integer, Item>();
        idMap = new HashMap<Item, Integer>();

        int currentX = x + 10;
        int currentY = height - (width - 20) - 10;
        for (int i = 0; i < slots; i++) {
            Slot slot = new Slot();
            slot.index = i;
            slot.x = currentX;
            slot.y = currentY;
            slot.size = width - 20;

            slotArray.add(slot);

            currentY -= (slot.size + 10);
        }

        alpha = 1;
    }

    public void addItem(int key, Item item) {
        for (Slot slot : slotArray) {
            if (slot.item == null) {
                slot.item = item;
                itemMap.put(key, item);
                idMap.put(item, key);
                break;
            }
        }
    }

    public Item getItem(int key) {
        return itemMap.get(key);
    }

    public void removeItem(int key) {
        Item item = itemMap.remove(key);
        idMap.remove(item);
        if (item != null) {
            for (Slot slot : slotArray) {
                if (slot.item != null && slot.item.equals(item)) {
                    slot.item = null;
                    break;
                }
            }
        }
    }

    public void render() {
        batch.begin();
        batch.setColor(1, 1, 1, alpha);
        batch.draw(background, x, y, width, height);
        for (Slot slot : slotArray) {
            if (slot.item != null) {
                float scale = getScaleFactor(slot);
                float scaledWidth = slot.item.width * scale;
                float scaledHeight = slot.item.height * scale;
                float itemX = slot.x + (slot.size - scaledWidth) / 2;
                float itemY = slot.y + (slot.size - scaledHeight) / 2;
                batch.draw(slot.item.textureRegion, itemX, itemY, scaledWidth, scaledHeight);
            }
        }
        batch.end();
    }

    private float getScaleFactor(Slot slot) {
        float itemSize = Math.max(slot.item.width, slot.item.height);
        return slot.size / itemSize;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int convertPositionToEntityId(int x, int y) {
        for (Slot slot : slotArray) {
            if (x >= slot.x && x <= (slot.x + slot.size) && y >= slot.y && y <= (slot.y + slot.size)) {
                if (idMap.containsKey(slot.item)) {
                    return idMap.get(slot.item);
                } else {
                    break;
                }
            }
        }
        return -1;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }
}
