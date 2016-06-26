package com.kavanbickerstaff.floodgate.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.TimeUtils;
import com.kavanbickerstaff.floodgate.components.LiquidComponent;
import com.kavanbickerstaff.floodgate.components.LiquidSpawnComponent;
import com.uwsoft.editor.renderer.components.DimensionsComponent;
import com.uwsoft.editor.renderer.components.MainItemComponent;
import com.uwsoft.editor.renderer.components.TransformComponent;
import com.uwsoft.editor.renderer.physics.PhysicsBodyLoader;

import finnstr.libgdx.liquidfun.ParticleDef;
import finnstr.libgdx.liquidfun.ParticleGroupDef;

public class LiquidSpawnSystem extends IteratingSystem implements EntityListener {

    private ComponentMapper<LiquidSpawnComponent> spawnM = ComponentMapper.getFor(LiquidSpawnComponent.class);
    private ComponentMapper<TransformComponent> transformM = ComponentMapper.getFor(TransformComponent.class);
    private ComponentMapper<DimensionsComponent> dimensionsM = ComponentMapper.getFor(DimensionsComponent.class);
    private ComponentMapper<MainItemComponent> mainM = ComponentMapper.getFor(MainItemComponent.class);

    private float WORLD_TO_BOX;

    private IntMap<Long> spawnTrackerMap;

    @SuppressWarnings("unchecked")
    public LiquidSpawnSystem() {
        super(Family.all(LiquidSpawnComponent.class,
                TransformComponent.class,
                DimensionsComponent.class,
                MainItemComponent.class).get());

        // TODO: Find a way to get this globally for all systems
        WORLD_TO_BOX = PhysicsBodyLoader.getScale();

        spawnTrackerMap = new IntMap<Long>();
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
                    spawnLiquid(transform.x, transform.y, dimensions.width * transform.scaleX, dimensions.height * transform.scaleY);
                }
            }
        }

        if (spawn.spawnOnNextUpdate && spawn.on) {
            spawnLiquid(transform.x, transform.y, dimensions.width * transform.scaleX, dimensions.height * transform.scaleY);
            spawn.spawnOnNextUpdate = false;
        }
    }

    private void spawnLiquid(float x, float y, float width, float height) {
        ParticleGroupDef groupDef = createParticleGroupDef(x + (width / 2), y + (height / 2), width, height);

        LiquidComponent liquid = new LiquidComponent(true);
        liquid.particleGroupDef = groupDef;

        Entity entity = new Entity();
        entity.add(liquid);
        getEngine().addEntity(entity);
    }

    private ParticleGroupDef createParticleGroupDef(float x, float y, float width, float height) {
        ParticleGroupDef groupDef = new ParticleGroupDef();
        groupDef.color.set(1, 0, 0, 1);
        groupDef.flags.add(ParticleDef.ParticleType.b2_waterParticle);
        groupDef.flags.add(ParticleDef.ParticleType.b2_fixtureContactListenerParticle);
        groupDef.flags.add(ParticleDef.ParticleType.b2_fixtureContactFilterParticle);
        groupDef.position.set(x * WORLD_TO_BOX, y * WORLD_TO_BOX);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox((width / 2) * WORLD_TO_BOX, (height / 2) * WORLD_TO_BOX);
        groupDef.shape = shape;

        return groupDef;
    }
}
