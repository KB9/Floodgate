package com.kavanbickerstaff.floodgate.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.kavanbickerstaff.floodgate.GameScreen;
import com.kavanbickerstaff.floodgate.components.PlacementComponent;
import com.uwsoft.editor.renderer.components.DimensionsComponent;
import com.uwsoft.editor.renderer.components.TransformComponent;
import com.uwsoft.editor.renderer.components.physics.PhysicsBodyComponent;

import finnstr.libgdx.liquidfun.ParticleSystem;

public class PlacementSystem extends IteratingSystem {

    private ComponentMapper<PlacementComponent> placementM = ComponentMapper.getFor(PlacementComponent.class);
    private ComponentMapper<PhysicsBodyComponent> physicsM = ComponentMapper.getFor(PhysicsBodyComponent.class);
    private ComponentMapper<TransformComponent> transformM = ComponentMapper.getFor(TransformComponent.class);
    private ComponentMapper<DimensionsComponent> dimensionsM = ComponentMapper.getFor(DimensionsComponent.class);

    private World world;

    @SuppressWarnings("unchecked")
    public PlacementSystem(World world) {
        super(Family.all(PlacementComponent.class, PhysicsBodyComponent.class).get());

        this.world = world;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        PlacementComponent placement = placementM.get(entity);
        PhysicsBodyComponent physicsBody = physicsM.get(entity);

        // If the physics body is active, deactivate it
        if (physicsBody.body.isActive()) {
            physicsBody.body.setActive(false);
        }

        // Transform the physics body by its placement position
        physicsBody.body.setTransform(
                placement.worldX * GameScreen.WORLD_TO_BOX,
                placement.worldY * GameScreen.WORLD_TO_BOX,
                physicsBody.body.getAngle()
        );

        // If the entity is no longer held and its position has not been marked as invalid
        if (!placement.isHeld && !placement.hasInvalidPosition) {
            // Check that area in which it was placed is unobstructed
            if (canPlaceEntity(entity, placement.worldX, placement.worldY)) {
                // Activate physics on the body and remove placement component
                physicsBody.body.setActive(true);
                entity.remove(PlacementComponent.class);
            } else {
                // Entity cannot be placed therefore its position is invalid
                placement.hasInvalidPosition = true;
            }

        } else if (placement.isHeld && placement.hasInvalidPosition) {
            // If it is held, remove its invalid position flag
            placement.hasInvalidPosition = false;
        }
    }

    public Entity getPlaceableAt(float worldX, float worldY) {
        Array<Body> activatedBodies = new Array<Body>();

        // Temporarily activate all entities' physics bodies so that the query will find them
        for (Entity entity : getEntities()) {
            PhysicsBodyComponent physicsBody = physicsM.get(entity);
            if (!physicsBody.body.isActive()) {
                physicsBody.body.setActive(true);
                activatedBodies.add(physicsBody.body);
            }
        }

        // Run the query
        FindPlaceableCallback callback = new FindPlaceableCallback();
        float areaSize = 0.5f;
        float queryLX = (worldX - areaSize) * GameScreen.WORLD_TO_BOX;
        float queryLY = (worldY - areaSize) * GameScreen.WORLD_TO_BOX;
        float queryUX = (worldX + areaSize) * GameScreen.WORLD_TO_BOX;
        float queryUY = (worldY + areaSize) * GameScreen.WORLD_TO_BOX;
        world.QueryAABB(callback, queryLX, queryLY, queryUX, queryUY);

        // Loop through all placeable entities to find out if one was found by the query
        for (Entity entity : getEntities()) {
            PhysicsBodyComponent physicsBody = physicsM.get(entity);
            if (callback.foundBodies.contains(physicsBody.body, true)) {

                // Deactivate all bodies that were temporarily activated
                for (Body body : activatedBodies) {
                    body.setActive(false);
                }

                return entity;
            }
        }

        // Deactivate all bodies that were temporarily activated
        for (Body body : activatedBodies) {
            body.setActive(false);
        }

        return null;
    }

    private boolean canPlaceEntity(Entity entity, float worldX, float worldY) {
        TransformComponent t = transformM.get(entity);
        DimensionsComponent d = dimensionsM.get(entity);
        PhysicsBodyComponent p = physicsM.get(entity);

        // TODO: Unsure as to the correctness of this calculation.
        // TODO: Seems to struggle with rotated physics bodies.
        float lowerX = (worldX - ((d.width * t.scaleX) / 2)) * GameScreen.WORLD_TO_BOX;
        float lowerY = (worldY - ((d.height * t.scaleY) / 2)) * GameScreen.WORLD_TO_BOX;
        float upperX = (worldX + ((d.width * t.scaleX) / 2)) * GameScreen.WORLD_TO_BOX;
        float upperY = (worldY + ((d.height * t.scaleY) / 2)) * GameScreen.WORLD_TO_BOX;

        CanPlaceCallback callback = new CanPlaceCallback();
        world.QueryAABB(callback, lowerX, lowerY, upperX, upperY);
        return !callback.blocked;
    }

    /**
     * Query callback that saves all bodies that it reports. Ignores particles.
     * Slower compared to AreaCallback
     */
    private class FindPlaceableCallback implements QueryCallback {

        private Array<Body> foundBodies = new Array<Body>();

        @Override
        public boolean reportFixture(Fixture fixture) {
            foundBodies.add(fixture.getBody());
            return true;
        }

        @Override
        public boolean reportParticle (ParticleSystem system, int index) {
            return false;
        }

        @Override
        public boolean shouldQueryParticleSystem(ParticleSystem system) {
            return false;
        }
    }

    /**
     * Query callback that detects fixtures and particles and stops reporting after the
     * first report, and sets its blocked flag.
     * Faster compared to FindPlaceableCallback.
     */
    private class CanPlaceCallback implements QueryCallback {

        private boolean blocked;

        @Override
        public boolean reportFixture(Fixture fixture) {
            blocked = true;
            return false;
        }

        @Override
        public boolean reportParticle (ParticleSystem system, int index) {
            blocked = true;
            return true;
        }

        @Override
        public boolean shouldQueryParticleSystem(ParticleSystem system) {
            return true;
        }
    }
}
