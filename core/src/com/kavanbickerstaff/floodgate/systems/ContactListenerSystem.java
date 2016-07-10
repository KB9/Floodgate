package com.kavanbickerstaff.floodgate.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.LongArray;
import com.badlogic.gdx.utils.LongMap;
import com.kavanbickerstaff.floodgate.components.ContactListenerComponent;
import com.uwsoft.editor.renderer.components.MainItemComponent;
import com.uwsoft.editor.renderer.components.physics.PhysicsBodyComponent;

import finnstr.libgdx.liquidfun.ParticleBodyContact;
import finnstr.libgdx.liquidfun.ParticleContact;
import finnstr.libgdx.liquidfun.ParticleSystem;

public class ContactListenerSystem extends IteratingSystem implements EntityListener {

    private ComponentMapper<ContactListenerComponent> listenerM = ComponentMapper.getFor(ContactListenerComponent.class);
    private ComponentMapper<PhysicsBodyComponent> physicsM = ComponentMapper.getFor(PhysicsBodyComponent.class);

    private LongMap<Array<Fixture>> fixtureBegins;
    private LongMap<Array<Fixture>> fixtureEnds;
    private LongMap<IntArray> particleBegins;
    private LongMap<IntArray> particleEnds;

    @SuppressWarnings("unchecked")
    public ContactListenerSystem(World world) {
        super(Family.all(ContactListenerComponent.class,
                PhysicsBodyComponent.class,
                MainItemComponent.class).get());

        world.setContactListener(new PhysicsContactListener());

        fixtureBegins = new LongMap<Array<Fixture>>();
        fixtureEnds = new LongMap<Array<Fixture>>();
        particleBegins = new LongMap<IntArray>();
        particleEnds = new LongMap<IntArray>();
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        engine.addEntityListener(getFamily(), this);
    }

    @Override
    public void entityAdded(Entity entity) {
        ContactListenerComponent listener = listenerM.get(entity);

        // Initialise the arrays within the component if they are null
        if (listener.fixtureContactsBegun == null) listener.fixtureContactsBegun = new Array<Fixture>();
        if (listener.fixtureContactsEnded == null) listener.fixtureContactsEnded = new Array<Fixture>();
        if (listener.particleContactsBegun == null) listener.particleContactsBegun = new IntArray();
        if (listener.particleContactsEnded == null) listener.particleContactsEnded = new IntArray();
    }

    @Override
    public void entityRemoved(Entity entity) {
        PhysicsBodyComponent physicsBody = physicsM.get(entity);

        // Remove the arrays within the maps associated with the entity
        long bodyAddress = physicsBody.body.getAddress();
        fixtureBegins.remove(bodyAddress);
        fixtureEnds.remove(bodyAddress);
        particleBegins.remove(bodyAddress);
        particleEnds.remove(bodyAddress);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        ContactListenerComponent listener = listenerM.get(entity);
        PhysicsBodyComponent physicsBody = physicsM.get(entity);

        long bodyAddress = physicsBody.body.getAddress();

        // If the physics body has just become active, make it a key
        if (physicsBody.body != null && !fixtureBegins.containsKey(bodyAddress)) {
            fixtureBegins.put(bodyAddress, new Array<Fixture>());
            fixtureEnds.put(bodyAddress, new Array<Fixture>());
            particleBegins.put(bodyAddress, new IntArray());
            particleEnds.put(bodyAddress, new IntArray());
        }

        // Adds the fixtures which began contact within the last step.
        // Duplicates may occur if the fixture made more than one contact within the step.
        listener.fixtureContactsBegun.clear();
        for (Fixture fixture : fixtureBegins.get(bodyAddress)) {
            listener.fixtureContactsBegun.add(fixture);
            listener.totalFixtureContacts++;
        }

        // Adds the fixtures which ended contact within the last step.
        // Duplicates may occur if the fixture ended more than one contact within the step.
        listener.fixtureContactsEnded.clear();
        for (Fixture fixture : fixtureEnds.get(bodyAddress)) {
            listener.fixtureContactsEnded.add(fixture);
        }

        // Adds the particles which began contact within the last step.
        // Duplicates may occur if the particle made more than one contact within the step.
        listener.particleContactsBegun.clear();
        for (int i = 0; i < particleBegins.get(bodyAddress).size; i++) {
            listener.particleContactsBegun.add(particleBegins.get(bodyAddress).get(i));
            listener.totalParticleContacts++;
        }

        // Adds the particles which ended contact within the last step.
        // Duplicates may occur if the particle ended more than one contact within the step.
        listener.particleContactsEnded.clear();
        for (int i = 0; i < particleEnds.get(bodyAddress).size; i++) {
            listener.particleContactsEnded.add(particleEnds.get(bodyAddress).get(i));
        }

        // Clear the arrays in the maps for the next step
        fixtureBegins.get(bodyAddress).clear();
        fixtureEnds.get(bodyAddress).clear();
        particleBegins.get(bodyAddress).clear();
        particleEnds.get(bodyAddress).clear();
    }

    private class PhysicsContactListener implements ContactListener {

        @Override
        public void beginContact(Contact contact) {
            if (contact.getFixtureA() != null) {
                long bodyAddressA = contact.getFixtureA().getBody().getAddress();
                if (fixtureBegins.containsKey(bodyAddressA)) {
                    fixtureBegins.get(bodyAddressA).add(contact.getFixtureA());
                }
            }

            if (contact.getFixtureB() != null) {
                long bodyAddressB = contact.getFixtureB().getBody().getAddress();
                if (fixtureBegins.containsKey(bodyAddressB)) {
                    fixtureBegins.get(bodyAddressB).add(contact.getFixtureB());
                }
            }
        }

        @Override
        public void endContact(Contact contact) {
            if (contact.getFixtureA() != null) {
                long bodyAddressA = contact.getFixtureA().getBody().getAddress();
                if (fixtureEnds.containsKey(bodyAddressA)) {
                    fixtureEnds.get(bodyAddressA).add(contact.getFixtureA());
                }
            }

            if (contact.getFixtureB() != null) {
                long bodyAddressB = contact.getFixtureB().getBody().getAddress();
                if (fixtureEnds.containsKey(bodyAddressB)) {
                    fixtureEnds.get(bodyAddressB).add(contact.getFixtureB());
                }
            }
        }

        @Override
        public void beginParticleBodyContact(ParticleSystem system, ParticleBodyContact contact) {
            if (contact.getBody() != null) {
                long bodyAddress = contact.getBody().getAddress();
                if (particleBegins.containsKey(bodyAddress)) {
                    particleBegins.get(bodyAddress).add(contact.getIndex());
                }
            }
        }

        @Override
        public void endParticleBodyContact(Fixture fixture, ParticleSystem system, int index) {
            if (fixture != null) {
                long bodyAddress = fixture.getBody().getAddress();
                if (particleEnds.containsKey(bodyAddress)) {
                    particleEnds.get(bodyAddress).add(index);
                }
            }
        }

        @Override
        public void beginParticleContact(ParticleSystem system, ParticleContact contact) {

        }

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
}
