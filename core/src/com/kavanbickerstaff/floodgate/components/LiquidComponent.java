package com.kavanbickerstaff.floodgate.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import finnstr.libgdx.liquidfun.ParticleDef;
import finnstr.libgdx.liquidfun.ParticleGroupDef;

public class LiquidComponent implements Component {

    public ParticleGroupDef particleGroupDef;
    public ParticleDef particleDef;
    public boolean isGroup;

    public LiquidComponent(boolean isGroup) {
        this.isGroup = isGroup;
    }
}
