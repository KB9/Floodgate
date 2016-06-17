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
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.LongArray;
import com.kavanbickerstaff.floodgate.components.LiquidDetectorComponent;
import com.uwsoft.editor.renderer.components.physics.PhysicsBodyComponent;

import finnstr.libgdx.liquidfun.ParticleBodyContact;
import finnstr.libgdx.liquidfun.ParticleContact;
import finnstr.libgdx.liquidfun.ParticleSystem;

public class LiquidDetectorSystem extends IteratingSystem {

    private ComponentMapper<LiquidDetectorComponent> detectorM = ComponentMapper.getFor(LiquidDetectorComponent.class);
    private ComponentMapper<PhysicsBodyComponent> physicsM = ComponentMapper.getFor(PhysicsBodyComponent.class);

    private class ParticleContactResolver implements ContactListener {

        /** Called when two fixtures begin to touch. */
        @Override
        public void beginContact(Contact contact) {

        }

        /** Called when two fixtures cease to touch. */
        @Override
        public void endContact(Contact contact) {

        }

        /** Called when a particle and a fixture begin to touch. */
        @Override
        public void beginParticleBodyContact(ParticleSystem system, ParticleBodyContact contact) {
            long bodyAddress = contact.getBody().getAddress();

            if (detectorBodyAddresses.contains(bodyAddress)) {
                Gdx.app.log("DETECTOR", "Body: " + bodyAddress + " Particle: " + contact.getIndex());
            }

            if (despawnerBodyAddresses.contains(bodyAddress)) {
                system.destroyParticle(contact.getIndex());
            }
        }

        /** Called when a particle and a fixture cease to touch. */
        @Override
        public void endParticleBodyContact(Fixture fixture, ParticleSystem system, int index) {

        }

        /** Called when two particles begin to touch. */
        @Override
        public void beginParticleContact(ParticleSystem system, ParticleContact contact) {

        }

        /** Called when two particles cease to touch. */
        @Override
        public void endParticleContact(ParticleSystem system, int indexA, int indexB) {

        }

        @Override
        public void preSolve(Contact contact, Manifold oldManifold) {

        }

        @Override
        public void postSolve(Contact contact, ContactImpulse impulse) {

        }
    }

    private LongArray detectorBodyAddresses;
    private LongArray despawnerBodyAddresses;

    @SuppressWarnings("unchecked")
    public LiquidDetectorSystem(World world, ParticleSystem particleSystem) {
        super(Family.all(LiquidDetectorComponent.class,
                PhysicsBodyComponent.class).get());

        world.setContactListener(new ParticleContactResolver());
        detectorBodyAddresses = new LongArray();
        despawnerBodyAddresses = new LongArray();
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        LiquidDetectorComponent detector = detectorM.get(entity);
        PhysicsBodyComponent physicsBody = physicsM.get(entity);

        long bodyAddress = physicsBody.body.getAddress();

        if (!detectorBodyAddresses.contains(bodyAddress)) {
            detectorBodyAddresses.add(bodyAddress);
        }

        if (detector.destroyOnContact && !despawnerBodyAddresses.contains(bodyAddress)) {
            despawnerBodyAddresses.add(bodyAddress);
        }
    }
}
