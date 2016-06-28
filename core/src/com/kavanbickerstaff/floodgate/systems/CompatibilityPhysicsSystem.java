package com.kavanbickerstaff.floodgate.systems;

import com.badlogic.gdx.physics.box2d.World;
import com.uwsoft.editor.renderer.systems.PhysicsSystem;

import finnstr.libgdx.liquidfun.ParticleSystem;

public class CompatibilityPhysicsSystem extends PhysicsSystem {

    private final float TIME_STEP = 1f/60;
    private World world;
    private boolean isPhysicsOn = true;
    private float accumulator = 0;

    private ParticleSystem particleSystem;

    public CompatibilityPhysicsSystem(World world, ParticleSystem particleSystem) {
        super(world);
        this.world = world;
        this.particleSystem = particleSystem;
    }

    public void setParticleSystem(ParticleSystem particleSystem) {
        this.particleSystem = particleSystem;
    }

    public ParticleSystem getParticleSystem() {
        return particleSystem;
    }

    @Override
    public void update(float deltaTime) {
        for (int i = 0; i < getEntities().size(); ++i) {
            processEntity(getEntities().get(i), deltaTime);
        }

        if (world != null && isPhysicsOn) {
            float frameTime = Math.min(deltaTime, 0.25f);
            accumulator += frameTime;
            while (accumulator >= TIME_STEP) {
//                world.step(deltaTime, 10, 6,
//                        particleSystem.calculateReasonableParticleIterations(deltaTime));
                world.step(TIME_STEP, 10, 6, 1);
                accumulator -= TIME_STEP;
            }
        }
    }

    @Override
    public void setPhysicsOn(boolean isPhysicsOn) {
        this.isPhysicsOn = isPhysicsOn;
        super.setPhysicsOn(isPhysicsOn);
    }
}
