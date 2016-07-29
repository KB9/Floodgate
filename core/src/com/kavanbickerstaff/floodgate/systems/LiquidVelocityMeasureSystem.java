package com.kavanbickerstaff.floodgate.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import com.kavanbickerstaff.floodgate.components.LiquidComponent;

public class LiquidVelocityMeasureSystem extends IteratingSystem {

    private ComponentMapper<LiquidComponent> liquidM = ComponentMapper.getFor(LiquidComponent.class);

    public Vector2 averageVelocity;

    @SuppressWarnings("unchecked")
    public LiquidVelocityMeasureSystem() {
        super(Family.all(LiquidComponent.class).get());

        averageVelocity = new Vector2();
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        LiquidComponent liquid = liquidM.get(entity);

        if (liquid.isGroup) {
            averageVelocity.add(liquid.particleGroup.getLinearVelocity());
        } else {
            // TODO: I have no idea what to do if it's a lone particle...
        }
    }

    @Override
    public void update(float deltaTime) {
        averageVelocity.setZero();
        super.update(deltaTime);
        averageVelocity.scl(1f / getEntities().size());
    }
}
