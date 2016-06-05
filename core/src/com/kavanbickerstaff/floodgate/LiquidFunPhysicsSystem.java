package com.kavanbickerstaff.floodgate;

import com.badlogic.gdx.physics.box2d.World;
import com.uwsoft.editor.renderer.systems.PhysicsSystem;

import finnstr.libgdx.liquidfun.ParticleSystem;

public class LiquidFunPhysicsSystem extends PhysicsSystem {

    private World world;
    private boolean isPhysicsOn = true;

    private ParticleSystem particleSystem;

    public LiquidFunPhysicsSystem(World world, ParticleSystem particleSystem) {
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
            // This has changed substantially from the old PhysicsSystem in which they
            // were using a fixed time-step. This step() seems to simulate particles
            // best. Refer to PhysicsSystem for old code.
            world.step(deltaTime, 10, 6,
                    particleSystem.calculateReasonableParticleIterations(deltaTime));
        }
    }

    @Override
    public void setPhysicsOn(boolean isPhysicsOn) {
        this.isPhysicsOn = isPhysicsOn;
        super.setPhysicsOn(isPhysicsOn);
    }
}
