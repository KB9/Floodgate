package com.kavanbickerstaff.floodgate.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.IntArray;
import com.kavanbickerstaff.floodgate.components.LiquidDespawnComponent;
import com.uwsoft.editor.renderer.components.MainItemComponent;
import com.uwsoft.editor.renderer.components.physics.PhysicsBodyComponent;

import finnstr.libgdx.liquidfun.ParticleBodyContactListener;
import finnstr.libgdx.liquidfun.ParticleSystem;

public class LiquidDespawnSystem extends IteratingSystem implements EntityListener {

    private ComponentMapper<PhysicsBodyComponent> physicsM = ComponentMapper.getFor(PhysicsBodyComponent.class);
    private ComponentMapper<MainItemComponent> mainM = ComponentMapper.getFor(MainItemComponent.class);

    private class ParticleDespawner implements ParticleBodyContactListener {

        private ParticleSystem particleSystem;

        public ParticleDespawner(ParticleSystem particleSystem) {
            this.particleSystem = particleSystem;
        }

        @Override
        public void beginContact(long address, int index) {
            particleSystem.destroyParticle(index);
        }
    }

    private IntArray particleListeners;

    @SuppressWarnings("unchecked")
    public LiquidDespawnSystem(World world, ParticleSystem particleSystem) {
        super(Family.all(LiquidDespawnComponent.class,
                PhysicsBodyComponent.class,
                MainItemComponent.class).get());

        world.setParticleBodyContactListener(new ParticleDespawner(particleSystem));
        particleListeners = new IntArray();
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
        PhysicsBodyComponent physicsBody = physicsM.get(entity);
        MainItemComponent main = mainM.get(entity);

        if (!particleListeners.contains(main.uniqueId)) {
            physicsBody.body.setUseParticleBodyContactListener(true);
        }
    }
}
