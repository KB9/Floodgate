package com.kavanbickerstaff.floodgate.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactFilter;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.LongArray;
import com.kavanbickerstaff.floodgate.GameScreen;
import com.kavanbickerstaff.floodgate.components.LiquidDetectorComponent;
import com.uwsoft.editor.renderer.components.DimensionsComponent;
import com.uwsoft.editor.renderer.components.TransformComponent;
import com.uwsoft.editor.renderer.components.physics.PhysicsBodyComponent;

import finnstr.libgdx.liquidfun.ParticleBodyContact;
import finnstr.libgdx.liquidfun.ParticleContact;
import finnstr.libgdx.liquidfun.ParticleSystem;

public class LiquidDetectorSystem extends IteratingSystem {

    private ComponentMapper<LiquidDetectorComponent> detectorM = ComponentMapper.getFor(LiquidDetectorComponent.class);
    private ComponentMapper<TransformComponent> transformM = ComponentMapper.getFor(TransformComponent.class);
    private ComponentMapper<DimensionsComponent> dimensionsM = ComponentMapper.getFor(DimensionsComponent.class);

    private World world;
    private ParticleSystem particleSystem;

    private FindParticlesCallback callback;

    @SuppressWarnings("unchecked")
    public LiquidDetectorSystem(World world, ParticleSystem particleSystem) {
        super(Family.all(LiquidDetectorComponent.class).get());

        this.world = world;
        this.particleSystem = particleSystem;

        callback = new FindParticlesCallback();
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        LiquidDetectorComponent detector = detectorM.get(entity);
        TransformComponent transform = transformM.get(entity);
        DimensionsComponent dimensions = dimensionsM.get(entity);

        callback.reset();

        float worldX = transform.x;
        float worldY = transform.y;
        float worldWidth = dimensions.width * transform.scaleX;
        float worldHeight = dimensions.height * transform.scaleY;

        float queryLX = worldX * GameScreen.WORLD_TO_BOX;
        float queryLY = worldY * GameScreen.WORLD_TO_BOX;
        float queryUX = (worldX + worldWidth) * GameScreen.WORLD_TO_BOX;
        float queryUY = (worldY + worldHeight) * GameScreen.WORLD_TO_BOX;
        world.QueryAABB(callback, queryLX, queryLY, queryUX, queryUY);

        detector.particleCount = callback.particleIndices.size;
        detector.totalParticleCount += callback.particleIndices.size;

        if (detector.destroyParticles) {
            int count = callback.particleIndices.size;
            for (int i = 0; i < count; i++) {
                int particleIndex = callback.particleIndices.get(i);
                particleSystem.destroyParticle(particleIndex);
            }
        }
    }

    private class FindParticlesCallback implements QueryCallback {

        private IntArray particleIndices = new IntArray();

        public void reset() {
            particleIndices.clear();
        }

        @Override
        public boolean reportFixture(Fixture fixture) {
            return true;
        }

        @Override
        public boolean reportParticle (ParticleSystem system, int index) {
            particleIndices.add(index);
            return true;
        }

        @Override
        public boolean shouldQueryParticleSystem(ParticleSystem system) {
            return true;
        }
    }
}
