package com.kavanbickerstaff.floodgate.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import finnstr.libgdx.liquidfun.ParticleDef;
import finnstr.libgdx.liquidfun.ParticleGroup;
import finnstr.libgdx.liquidfun.ParticleGroupDef;

public class LiquidComponent implements Component {

    public ParticleGroup particleGroup;
    public int particle;
    public boolean isGroup;

    public LiquidComponent(boolean isGroup) {
        this.isGroup = isGroup;
    }
}
