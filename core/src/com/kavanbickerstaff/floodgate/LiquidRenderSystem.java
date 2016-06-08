package com.kavanbickerstaff.floodgate;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kavanbickerstaff.floodgate.components.LiquidComponent;
import com.uwsoft.editor.renderer.physics.PhysicsBodyLoader;

public class LiquidRenderSystem extends IteratingSystem {

    private ComponentMapper<LiquidComponent> liquidM = ComponentMapper.getFor(LiquidComponent.class);

    private Array<Entity> entities;
    private SpriteBatch batch;
    private FrameBuffer fbo;
    private Viewport targetView;

    private float BOX_TO_WORLD;

    @SuppressWarnings("unchecked")
    public LiquidRenderSystem(Viewport viewport) {
        super(Family.all(LiquidComponent.class).get());

        targetView = viewport;

        entities = new Array<Entity>();
        batch = new SpriteBatch();
        fbo = new FrameBuffer(Pixmap.Format.RGBA8888,
                Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);

        BOX_TO_WORLD = 1f / PhysicsBodyLoader.getScale();
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {

    }

    @Override
    public void update(float deltaTime) {
        for (Entity entity : getEntities()) {
            LiquidComponent liquid = liquidM.get(entity);

            // Start drawing to the FBO
            fbo.begin();

            Gdx.gl.glClearColor(0, 0, 0, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            batch.setShader(null);
            batch.begin();
            for (Vector2 pos : liquid.particleSystem.getParticlePositionBuffer()) {
                // X: scale position to world coordinates
                // Y: invert y-axis (for FBO rendering), scale position to world coordinates
                int sceneX = (int)(pos.x * BOX_TO_WORLD);
                int sceneY = (int)targetView.getWorldHeight() - (int)(pos.y * BOX_TO_WORLD);
                batch.draw(liquid.particleTexture,
                        ViewportUtils.sceneToScreenX(targetView, sceneX)
                                - (liquid.particleTexture.getWidth() / 2),
                        ViewportUtils.sceneToScreenY(targetView, sceneY)
                                - (liquid.particleTexture.getHeight() / 2)
                );
            }
            batch.end();

            // Stop drawing to the FBO
            fbo.end(targetView.getScreenX(), targetView.getScreenY(),
                    targetView.getScreenWidth(), targetView.getScreenHeight());

            batch.setShader(liquid.shader);
            batch.begin();
            batch.draw(fbo.getColorBufferTexture(), 0, 0);
            batch.end();
        }
    }
}
