package com.kavanbickerstaff.floodgate.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.kavanbickerstaff.floodgate.GameScreen;
import com.kavanbickerstaff.floodgate.ViewportUtils;
import com.kavanbickerstaff.floodgate.components.PlacementComponent;
import com.uwsoft.editor.renderer.components.DimensionsComponent;
import com.uwsoft.editor.renderer.components.PolygonComponent;
import com.uwsoft.editor.renderer.components.TintComponent;
import com.uwsoft.editor.renderer.components.TransformComponent;
import com.uwsoft.editor.renderer.components.physics.PhysicsBodyComponent;

import finnstr.libgdx.liquidfun.ParticleSystem;

public class PlacementSystem extends IteratingSystem {

    private ComponentMapper<PlacementComponent> placementM = ComponentMapper.getFor(PlacementComponent.class);
    private ComponentMapper<PhysicsBodyComponent> physicsM = ComponentMapper.getFor(PhysicsBodyComponent.class);
    private ComponentMapper<TransformComponent> transformM = ComponentMapper.getFor(TransformComponent.class);
    private ComponentMapper<DimensionsComponent> dimensionsM = ComponentMapper.getFor(DimensionsComponent.class);
    private ComponentMapper<TintComponent> tintM = ComponentMapper.getFor(TintComponent.class);
    private ComponentMapper<PolygonComponent> polygonM = ComponentMapper.getFor(PolygonComponent.class);

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
        TintComponent tint = tintM.get(entity);

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

        // Mark and add color changes for valid/invalid positions
        if (canPlaceEntity(entity, placement.worldX, placement.worldY)) {
            placement.hasInvalidPosition = false;
            tint.color.set(1, 1, 1, 1);
        } else {
            placement.hasInvalidPosition = true;
            tint.color.set(1, 0, 0, 0.5f);
        }

        // If the entity is no longer held and its position has not been marked as invalid
        if (!placement.isHeld && !placement.hasInvalidPosition) {
            physicsBody.body.setActive(true);
            entity.remove(PlacementComponent.class);
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
        PolygonComponent p = polygonM.get(entity);

        if (d.polygon == null) d.setPolygon(p);
        d.polygon.setPosition(worldX - ((d.width * t.scaleX) / 2), worldY - ((d.height * t.scaleY) / 2));
        d.polygon.setRotation(t.rotation);
        d.polygon.setScale(t.scaleX, t.scaleY);

        return !isRayCastBlocked(d.polygon.getTransformedVertices());
    }

    private boolean isRayCastBlocked(float[] vertices) {
        CanPlaceCallback callback = new CanPlaceCallback();
        Vector2 start = new Vector2();
        Vector2 end = new Vector2();

        // Ray cast between all vertices
        for (int i = 0; i < vertices.length; i+= 2) {
            start.set(vertices[i], vertices[i + 1]);
            for (int j = (i + 2); j < vertices.length; j += 2) {
                end.set(vertices[j], vertices[j + 1]);

                // If the ray cast comes into contact with any fixture, return true
                world.rayCast(callback, start.cpy().scl(GameScreen.WORLD_TO_BOX), end.cpy().scl(GameScreen.WORLD_TO_BOX));
                if (callback.blocked) return true;
            }
        }

        return false;
    }

    /**
     * Query callback that saves all bodies that it reports. Ignores particles.
     * It is an AABB test, therefore it detects bounding boxes and not fixture shapes.
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
     * Query callback that detects fixtures and stops reporting after the first report, and
     * sets its blocked flag.
     */
    private class CanPlaceCallback implements RayCastCallback {

        public boolean blocked;

        @Override
        public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
            blocked = true;
            return 0;
        }

        @Override
        public float reportRayParticle(ParticleSystem system, int index, Vector2 point, Vector2 normal, float fraction) {
            return 0;
        }

        @Override
        public boolean shouldQueryParticleSystem(ParticleSystem system) {
            return false;
        }
    }
}
