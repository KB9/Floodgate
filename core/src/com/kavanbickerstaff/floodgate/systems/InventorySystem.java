package com.kavanbickerstaff.floodgate.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.kavanbickerstaff.floodgate.HUD;
import com.kavanbickerstaff.floodgate.ViewportUtils;
import com.kavanbickerstaff.floodgate.components.InventoryComponent;
import com.uwsoft.editor.renderer.components.DimensionsComponent;
import com.uwsoft.editor.renderer.components.MainItemComponent;
import com.uwsoft.editor.renderer.components.TextureRegionComponent;
import com.uwsoft.editor.renderer.components.TransformComponent;
import com.uwsoft.editor.renderer.components.physics.PhysicsBodyComponent;
import com.uwsoft.editor.renderer.utils.ComponentRetriever;

import java.util.HashMap;

public class InventorySystem extends IteratingSystem implements EntityListener {

    private ComponentMapper<TextureRegionComponent> textureM = ComponentMapper.getFor(TextureRegionComponent.class);
    private ComponentMapper<PhysicsBodyComponent> physicsM = ComponentMapper.getFor(PhysicsBodyComponent.class);
    private ComponentMapper<MainItemComponent> mainM = ComponentMapper.getFor(MainItemComponent.class);
    private ComponentMapper<DimensionsComponent> dimensionsM = ComponentMapper.getFor(DimensionsComponent.class);
    private ComponentMapper<TransformComponent> transformM = ComponentMapper.getFor(TransformComponent.class);

    private HUD.Item items[];
    private IntMap<HUD.Item> itemsByEntityId;

    private HUD hud;

    @SuppressWarnings("unchecked")
    public InventorySystem(HUD hud) {
        super(Family.all(InventoryComponent.class,
                TextureRegionComponent.class,
                PhysicsBodyComponent.class).get());
        this.hud = hud;

        items = new HUD.Item[hud.getSlotCount()];
        itemsByEntityId = new IntMap<HUD.Item>();
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        engine.addEntityListener(getFamily(), this);
    }

    @Override
    public void entityAdded(Entity entity) {
        TextureRegionComponent textureRegion = textureM.get(entity);
        MainItemComponent main = mainM.get(entity);
        DimensionsComponent dimensions = dimensionsM.get(entity);
        TransformComponent transform = transformM.get(entity);

        main.visible = false;

        HUD.Item item = new HUD.Item();
        item.id = main.uniqueId;
        item.textureRegion = textureRegion.region;
        item.width = dimensions.width * transform.scaleX;
        item.height = dimensions.height * transform.scaleY;

        addItem(item.id, item);
    }

    private void addItem(int key, HUD.Item item) {
        // Find an empty index to store the item
        for (int i = 0; i < items.length; i++) {
            if (items[i] == null) {
                items[i] = item;
                break;
            }
        }

        itemsByEntityId.put(key, item);
        hud.update(items);
    }

    @Override
    public void entityRemoved(Entity entity) {
        MainItemComponent main = mainM.get(entity);
        PhysicsBodyComponent physicsBody = physicsM.get(entity);

        main.visible = true;
        physicsBody.body.setActive(true);

        removeItem(main.uniqueId);
    }

    public void removeItem(int key) {
        // Find the item in the array and remove it
        HUD.Item item = itemsByEntityId.remove(key);
        for (int i = 0; i < items.length; i++) {
            if (items[i] == item) {
                items[i] = null;
                break;
            }
        }

        hud.update(items);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        PhysicsBodyComponent physicsBody = physicsM.get(entity);
        MainItemComponent main = mainM.get(entity);

        // If the item was added and the physics body is still active, deactivate it
        if (itemsByEntityId.containsKey(main.uniqueId) && physicsBody.body.isActive()) {
            physicsBody.body.setActive(false);
        }
    }
}
