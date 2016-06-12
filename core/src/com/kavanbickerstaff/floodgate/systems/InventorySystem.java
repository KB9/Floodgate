package com.kavanbickerstaff.floodgate.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.kavanbickerstaff.floodgate.HUD;
import com.kavanbickerstaff.floodgate.components.InventoryComponent;
import com.uwsoft.editor.renderer.components.DimensionsComponent;
import com.uwsoft.editor.renderer.components.MainItemComponent;
import com.uwsoft.editor.renderer.components.TextureRegionComponent;
import com.uwsoft.editor.renderer.components.TransformComponent;
import com.uwsoft.editor.renderer.components.physics.PhysicsBodyComponent;

public class InventorySystem extends IteratingSystem implements EntityListener {

    private ComponentMapper<TextureRegionComponent> textureM = ComponentMapper.getFor(TextureRegionComponent.class);
    private ComponentMapper<PhysicsBodyComponent> physicsM = ComponentMapper.getFor(PhysicsBodyComponent.class);
    private ComponentMapper<MainItemComponent> mainM = ComponentMapper.getFor(MainItemComponent.class);
    private ComponentMapper<DimensionsComponent> dimensionsM = ComponentMapper.getFor(DimensionsComponent.class);
    private ComponentMapper<TransformComponent> transformM = ComponentMapper.getFor(TransformComponent.class);

    private HUD hud;

    @SuppressWarnings("unchecked")
    public InventorySystem(HUD hud) {
        super(Family.all(InventoryComponent.class,
                TextureRegionComponent.class,
                PhysicsBodyComponent.class).get());
        this.hud = hud;
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
        item.textureRegion = textureRegion.region;
        item.width = dimensions.width * transform.scaleX;
        item.height = dimensions.height * transform.scaleY;
        hud.addItem(main.uniqueId, item);
    }

    @Override
    public void entityRemoved(Entity entity) {
        MainItemComponent main = mainM.get(entity);
        PhysicsBodyComponent physicsBody = physicsM.get(entity);

        main.visible = true;
        physicsBody.body.setActive(true);

        hud.removeItem(main.uniqueId);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        PhysicsBodyComponent physicsBody = physicsM.get(entity);
        MainItemComponent main = mainM.get(entity);

        if (hud.getItem(main.uniqueId) != null && physicsBody.body.isActive()) {
            physicsBody.body.setActive(false);
        }
    }
}
