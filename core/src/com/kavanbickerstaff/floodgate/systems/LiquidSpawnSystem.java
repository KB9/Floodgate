package com.kavanbickerstaff.floodgate.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
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

    private float WORLD_TO_BOX;

    private IntMap<Long> spawnTrackerMap;

    private ParticleSystem particleSystem;
    private float gravityX, gravityY;

    @SuppressWarnings("unchecked")
    public LiquidSpawnSystem(ParticleSystem particleSystem, float gravityX, float gravityY) {
        super(Family.all(LiquidSpawnComponent.class,
                TransformComponent.class,
                DimensionsComponent.class,
                MainItemComponent.class).get());

        // TODO: Find a way to get this globally for all systems
        WORLD_TO_BOX = PhysicsBodyLoader.getScale();

        spawnTrackerMap = new IntMap<Long>();

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
        spawnTrackerMap.put(main.uniqueId, TimeUtils.millis());
    }

    @Override
    public void entityRemoved(Entity entity) {
        MainItemComponent main = mainM.get(entity);
        spawnTrackerMap.remove(main.uniqueId);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        LiquidSpawnComponent spawn = spawnM.get(entity);
        TransformComponent transform = transformM.get(entity);
        DimensionsComponent dimensions = dimensionsM.get(entity);
        MainItemComponent main = mainM.get(entity);

        if (spawn.spawnTimeMillis > 0) {
            long lastSpawnTime = spawnTrackerMap.get(main.uniqueId);
            long currentTime = TimeUtils.millis();
            if (currentTime - lastSpawnTime >= spawn.spawnTimeMillis) {
                spawnTrackerMap.put(main.uniqueId, currentTime);

                if (spawn.on) {
                    spawnLiquid(transform.x, transform.y,
                            dimensions.width * transform.scaleX, dimensions.height * transform.scaleY,
                            spawn.spawnVelocityX, spawn.spawnVelocityY
                    );
                }
            }
        }

        if (spawn.spawnOnNextUpdate && spawn.on) {
            spawnLiquid(transform.x, transform.y,
                    dimensions.width * transform.scaleX, dimensions.height * transform.scaleY,
                    spawn.spawnVelocityX, spawn.spawnVelocityY
            );
            spawn.spawnOnNextUpdate = false;
        }

        if (spawn.spawnContinuous && spawn.on) {
            long lastSpawnTime = spawnTrackerMap.get(main.uniqueId);
            long timeDelta = TimeUtils.millis() - lastSpawnTime;

            // Incorporates the equation of motion "s = ut + 0.5at^2"
            float timeInSeconds = timeDelta / 1000f;
            float distanceX = 0, distanceY = 0;
            if (gravityX != 0) distanceX = Math.abs((spawn.spawnVelocityX * timeInSeconds) + (0.5f * gravityX * (timeInSeconds * timeInSeconds)));
            if (gravityY != 0) distanceY = Math.abs((spawn.spawnVelocityY * timeInSeconds) + (0.5f * gravityY * (timeInSeconds * timeInSeconds)));

            // Calculate Box2D dimensions of spawn area and compare to distance travelled
            float b2dWidth = dimensions.width * transform.scaleX * GameScreen.WORLD_TO_BOX;
            float b2dHeight = dimensions.height * transform.scaleY * GameScreen.WORLD_TO_BOX;
            if (distanceX > b2dWidth || distanceY > b2dHeight) {
                spawnLiquid(transform.x, transform.y,
                        dimensions.width * transform.scaleX, dimensions.height * transform.scaleY,
                        spawn.spawnVelocityX, spawn.spawnVelocityY
                );
                spawnTrackerMap.put(main.uniqueId, TimeUtils.millis());
            }
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
        groupDef.position.set(x * WORLD_TO_BOX, y * WORLD_TO_BOX);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox((width / 2) * WORLD_TO_BOX, (height / 2) * WORLD_TO_BOX);
        groupDef.shape = shape;

        groupDef.linearVelocity.set(velX, velY);

        return groupDef;
    }
}
