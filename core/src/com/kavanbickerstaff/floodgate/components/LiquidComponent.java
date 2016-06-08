package com.kavanbickerstaff.floodgate.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import finnstr.libgdx.liquidfun.ParticleSystem;

public class LiquidComponent implements Component {

    public Texture particleTexture;
    public ShaderProgram shader;
    public ParticleSystem particleSystem;
}
