package com.kavanbickerstaff.floodgate.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.utils.Array;
import com.kavanbickerstaff.floodgate.components.PlaceableComponent;
import com.uwsoft.editor.renderer.components.MainItemComponent;
import com.uwsoft.editor.renderer.components.TextureRegionComponent;
import com.uwsoft.editor.renderer.components.physics.PhysicsBodyComponent;
import com.uwsoft.editor.renderer.utils.ComponentRetriever;

import java.util.HashMap;

// TODO: New purpose for System.
// Instead of this system just controlling the visibility of entities, I could turn it into a
// PlacementSystem. This system would:
//      - Control the visibility of the entities.
//      - Control the position of the entity when it is being positioned before placement.
public class InventorySystem extends IteratingSystem implements EntityListener {

    private ComponentMapper<PlaceableComponent> placeableM = ComponentMapper.getFor(PlaceableComponent.class);
    private ComponentMapper<TextureRegionComponent> textureM = ComponentMapper.getFor(TextureRegionComponent.class);
    private ComponentMapper<PhysicsBodyComponent> physicsM = ComponentMapper.getFor(PhysicsBodyComponent.class);
    private ComponentMapper<MainItemComponent> mainM = ComponentMapper.getFor(MainItemComponent.class);

    private Array<Integer> storedIds;

    @SuppressWarnings("unchecked")
    public InventorySystem() {
        super(Family.all(PlaceableComponent.class,
                TextureRegionComponent.class,
                PhysicsBodyComponent.class).get());

        storedIds = new Array<Integer>();
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        engine.addEntityListener(this);
    }

    @Override
    public void entityAdded(Entity entity) {

    }

    @Override
    public void entityRemoved(Entity entity) {

    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        PlaceableComponent placeable = placeableM.get(entity);
        PhysicsBodyComponent physicsBody = physicsM.get(entity);
        MainItemComponent main = mainM.get(entity);

        if (placeable != null && physicsBody != null && main != null) {
            main.visible = !placeable.store;
            physicsBody.body.setActive(!placeable.store);

            if (placeable.store && !storedIds.contains(main.uniqueId, true)) {
                storedIds.add(main.uniqueId);
            }

            if (!placeable.store && storedIds.contains(main.uniqueId, true)) {
                storedIds.removeValue(main.uniqueId, true);
            }
        }
    }

    public ImmutableArray<Integer> getStoredIds() {
        return new ImmutableArray<Integer>(storedIds);
    }
}
