package com.kavanbickerstaff.floodgate.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.kavanbickerstaff.floodgate.components.LiquidComponent;

public class LiquidComponentCleanupSystem extends IteratingSystem {

    private ComponentMapper<LiquidComponent> liquidM = ComponentMapper.getFor(LiquidComponent.class);

    @SuppressWarnings("unchecked")
    public LiquidComponentCleanupSystem() {
        super(Family.all(LiquidComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        LiquidComponent liquid = liquidM.get(entity);

        if (liquid.particleGroup.getParticleCount() == 0) {
            entity.remove(LiquidComponent.class);
        }
    }
}
