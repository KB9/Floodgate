package com.kavanbickerstaff.floodgate.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.physics.box2d.MassData;
import com.kavanbickerstaff.floodgate.components.ContactListenerComponent;
import com.kavanbickerstaff.floodgate.components.DrownableComponent;
import com.uwsoft.editor.renderer.components.TintComponent;
import com.uwsoft.editor.renderer.components.physics.PhysicsBodyComponent;

public class DrowningSystem extends IteratingSystem {

    private ComponentMapper<DrownableComponent> drownableM = ComponentMapper.getFor(DrownableComponent.class);
    private ComponentMapper<ContactListenerComponent> listenerM = ComponentMapper.getFor(ContactListenerComponent.class);
    private ComponentMapper<PhysicsBodyComponent> physicsM = ComponentMapper.getFor(PhysicsBodyComponent.class);
    private ComponentMapper<TintComponent> tintM = ComponentMapper.getFor(TintComponent.class);

    @SuppressWarnings("unchecked")
    public DrowningSystem() {
        super(Family.all(DrownableComponent.class,
                ContactListenerComponent.class,
                PhysicsBodyComponent.class,
                TintComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        DrownableComponent drownable = drownableM.get(entity);
        ContactListenerComponent listener = listenerM.get(entity);
        PhysicsBodyComponent physicsBody = physicsM.get(entity);
        TintComponent tint = tintM.get(entity);

        if (!drownable.hasDrowned && listener.particleContactsBegun.size > drownable.particleLimit) {
            MassData data = physicsBody.body.getMassData();
            data.mass /= 10;
            physicsBody.body.setMassData(data);

            tint.color.set(0.251f, 0.729f, 0.890f, 1);

            drownable.hasDrowned = true;
        }
    }
}
