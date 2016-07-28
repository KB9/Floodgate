package com.kavanbickerstaff.floodgate.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.TimeUtils;
import com.kavanbickerstaff.floodgate.GameScreen;
import com.kavanbickerstaff.floodgate.ViewportUtils;
import com.kavanbickerstaff.floodgate.components.LiquidComponent;
import com.kavanbickerstaff.floodgate.components.LiquidSpawnComponent;
import com.uwsoft.editor.renderer.components.DimensionsComponent;
import com.uwsoft.editor.renderer.components.MainItemComponent;
import com.uwsoft.editor.renderer.components.TransformComponent;
import com.uwsoft.editor.renderer.physics.PhysicsBodyLoader;

import finnstr.libgdx.liquidfun.ParticleDef;
import finnstr.libgdx.liquidfun.ParticleGroupDef;
import finnstr.libgdx.liquidfun.ParticleSystem;

public class LiquidSpawnSystem extends IteratingSystem implements EntityListener {

    private ComponentMapper<LiquidSpawnComponent> spawnM = ComponentMapper.getFor(LiquidSpawnComponent.class);
    private ComponentMapper<TransformComponent> transformM = ComponentMapper.getFor(TransformComponent.class);
    private ComponentMapper<DimensionsComponent> dimensionsM = ComponentMapper.getFor(DimensionsComponent.class);
    private ComponentMapper<MainItemComponent> mainM = ComponentMapper.getFor(MainItemComponent.class);

    private IntMap<Long> spawnTrackerMap;
    private IntMap<Vector2> spawnVelocityMap;

    private ParticleSystem particleSystem;
    private float gravityX, gravityY;

    @SuppressWarnings("unchecked")
    public LiquidSpawnSystem(ParticleSystem particleSystem, float gravityX, float gravityY) {
        super(Family.all(LiquidSpawnComponent.class,
                TransformComponent.class,
                DimensionsComponent.class,
                MainItemComponent.class).get());

        spawnTrackerMap = new IntMap<Long>();
        spawnVelocityMap = new IntMap<Vector2>();

        this.particleSystem = particleSystem;
        this.gravityX = gravityX;
        this.gravityY = gravityY;
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        engine.addEntityListener(getFamily(), this);
    }

    @Override
    public void entityAdded(Entity entity) {
        MainItemComponent main = mainM.get(entity);
        LiquidSpawnComponent spawn = spawnM.get(entity);

        spawnTrackerMap.put(main.uniqueId, TimeUtils.millis());
        spawnVelocityMap.put(main.uniqueId, new Vector2(spawn.spawnVelocityX, spawn.spawnVelocityY));
    }

    @Override
    public void entityRemoved(Entity entity) {
        MainItemComponent main = mainM.get(entity);

        spawnTrackerMap.remove(main.uniqueId);
        spawnVelocityMap.remove(main.uniqueId);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        LiquidSpawnComponent spawn = spawnM.get(entity);
        TransformComponent transform = transformM.get(entity);
        DimensionsComponent dimensions = dimensionsM.get(entity);
        MainItemComponent main = mainM.get(entity);

        if (spawn.on) {

            // Immediately spawns liquid when this flag is set
            if (spawn.spawnOnNextUpdate) {
                // Spawns liquid in the specified area
                float actualWidth = dimensions.width * transform.scaleX;
                float actualHeight = dimensions.height * transform.scaleY;
                spawnLiquid(transform.x, transform.y, actualWidth, actualHeight, spawn.spawnVelocityX, spawn.spawnVelocityY);
            }

            // If the spawner is finite and spawn count > 0, or if the spawner is infinite
            if ((spawn.isFinite && spawn.spawnCount > 0) || !spawn.isFinite) {

                // Spawns liquid at intervals of length specified by spawnTimeMillis
                if (spawn.spawnTimeMillis > 0) {
                    tryIntervalSpawn(main, spawn, dimensions, transform);
                }

                // Spawns liquid so as to make it appear like a continuous stream
                if (spawn.spawnContinuous) {
                    tryContinuousSpawn(main, spawn, dimensions, transform);

                    // Reset "next velocity" tracker map if no longer spawning continuously
                    if (spawn.isFinite && spawn.spawnCount == 0) {
                        spawnVelocityMap.get(main.uniqueId).set(spawn.spawnVelocityX, spawn.spawnVelocityY);
                    }
                }
            }
        }
    }

    private void tryIntervalSpawn(MainItemComponent main, LiquidSpawnComponent spawn,
                                  DimensionsComponent dimensions, TransformComponent transform) {
        // Checks whether enough time has elapsed for another spawn
        long lastSpawnTime = spawnTrackerMap.get(main.uniqueId);
        long currentTime = TimeUtils.millis();
        if (currentTime - lastSpawnTime >= spawn.spawnTimeMillis) {
            // Updates the spawn timer
            spawnTrackerMap.put(main.uniqueId, currentTime);

            // Spawns liquid in the specified area
            float actualWidth = dimensions.width * transform.scaleX;
            float actualHeight = dimensions.height * transform.scaleY;
            spawnLiquid(transform.x, transform.y, actualWidth, actualHeight, spawn.spawnVelocityX, spawn.spawnVelocityY);

            // Decrement the spawn count
            spawn.spawnCount--;
        }
    }

    private void tryContinuousSpawn(MainItemComponent main, LiquidSpawnComponent spawn,
                                    DimensionsComponent dimensions, TransformComponent transform) {
        // Get the time delta between now and the last time a "continuous spawn" was completed
        long lastSpawnTime = spawnTrackerMap.get(main.uniqueId);
        long timeDelta = TimeUtils.millis() - lastSpawnTime;

        // Gets the spawn velocity (which was calculated after the last spawn)
        Vector2 spawnVelocity = spawnVelocityMap.get(main.uniqueId);

        // Gets the distance travelled by the last spawn since it was spawned
        float timeInSeconds = timeDelta / 1000f;
        float distanceX = 0, distanceY = 0;
        if (gravityX != 0)
            distanceX = Math.abs(getDistanceTravelled(spawnVelocity.x, gravityX, timeInSeconds));
        if (gravityY != 0)
            distanceY = Math.abs(getDistanceTravelled(spawnVelocity.y, gravityY, timeInSeconds));

        // Calculate Box2D dimensions of spawn area and compare to distance travelled
        float b2dWidth = dimensions.width * transform.scaleX * GameScreen.WORLD_TO_BOX;
        float b2dHeight = dimensions.height * transform.scaleY * GameScreen.WORLD_TO_BOX;
        if ((distanceX > b2dWidth || gravityX == 0) && (distanceY > b2dHeight || gravityY == 0)) {

            // Spawns liquid in the specified area
            float actualWidth = dimensions.width * transform.scaleX;
            float actualHeight = dimensions.height * transform.scaleY;
            spawnLiquid(transform.x, transform.y, actualWidth, actualHeight, spawnVelocity.x, spawnVelocity.y);

            // Calculates the velocity at which this spawn escaped from the spawn area.
            //
            // Final velocity is not calculated using the time since the last spawn. This is because
            // the time since the last spawn will increase regardless of whether it is spawning or
            // not.
            float escapeVelocityX = (float)getFinalVelocity(spawnVelocity.x, gravityX, b2dWidth);
            float escapeVelocityY = (float)getFinalVelocity(spawnVelocity.y, gravityY, b2dHeight);

            // Sets the next spawn's initial velocity to be equal to the escape velocity of the this spawn
            Vector2 nextSpawnVelocity = spawnVelocityMap.get(main.uniqueId);
            nextSpawnVelocity.set(escapeVelocityX, escapeVelocityY);

            // Updates the spawn timer
            spawnTrackerMap.put(main.uniqueId, TimeUtils.millis());

            // Decrement the spawn count
            spawn.spawnCount--;
        }
    }

    private void spawnLiquid(float x, float y, float width, float height, float velX, float velY) {
        ParticleGroupDef groupDef = createParticleGroupDef(x + (width / 2), y + (height / 2), width, height, velX, velY);

        LiquidComponent liquid = new LiquidComponent(true);
        liquid.particleGroup = particleSystem.createParticleGroup(groupDef);
        groupDef.shape.dispose();

        Entity entity = new Entity();
        entity.add(liquid);
        getEngine().addEntity(entity);
    }

    private ParticleGroupDef createParticleGroupDef(float x, float y, float width, float height, float velX, float velY) {
        ParticleGroupDef groupDef = new ParticleGroupDef();
        groupDef.color.set(1, 0, 0, 1);
        groupDef.flags.add(ParticleDef.ParticleType.b2_waterParticle);
        groupDef.flags.add(ParticleDef.ParticleType.b2_fixtureContactListenerParticle);
        groupDef.flags.add(ParticleDef.ParticleType.b2_fixtureContactFilterParticle);
        groupDef.position.set(x * GameScreen.WORLD_TO_BOX, y * GameScreen.WORLD_TO_BOX);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox((width / 2) * GameScreen.WORLD_TO_BOX, (height / 2) * GameScreen.WORLD_TO_BOX);
        groupDef.shape = shape;

        groupDef.linearVelocity.set(velX, velY);

        return groupDef;
    }

    /**
     * Implements the equation of motion "s = ut + 0.5at^2" to find the distance travelled by a
     * moving body.
     * @param startVelocity The initial velocity (t=0) of the body in m/s
     * @param acceleration The acceleration of the body in m/s^2 (can be positive or negative)
     * @param timeInSeconds The time taken for the body to travel this distance in s
     * @return The distance travelled by the moving body in m
     */
    private float getDistanceTravelled(float startVelocity, float acceleration, float timeInSeconds) {
        return (startVelocity * timeInSeconds) + (0.5f * acceleration * (timeInSeconds * timeInSeconds));
    }

    /**
     * Implements the equation of motion "v^2 = u^2 + 2as" to find the final velocity of a moving
     * body. Has a positive and negative form to prevent a NaN result.
     * @param startVelocity The initial velocity (t=0) of the body in m/s
     * @param acceleration The acceleration of the body in m/s^2
     * @param distance The distance travelled by the body in m
     * @return The final velocity of the moving body in m/s
     */
    private double getFinalVelocity(float startVelocity, float acceleration, float distance) {
        if (acceleration >= 0) {
            return Math.sqrt((startVelocity * startVelocity) + (2f * acceleration * distance));
        } else {
            return -Math.sqrt((startVelocity * startVelocity) + (2f * -acceleration * distance));
        }
    }
}
