package com.kavanbickerstaff.floodgate.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kavanbickerstaff.floodgate.ViewportUtils;
import com.kavanbickerstaff.floodgate.components.LiquidComponent;
import com.uwsoft.editor.renderer.physics.PhysicsBodyLoader;

import finnstr.libgdx.liquidfun.ParticleSystem;

public class LiquidRenderSystem extends IteratingSystem implements EntityListener {

    private ComponentMapper<LiquidComponent> liquidM = ComponentMapper.getFor(LiquidComponent.class);

    private SpriteBatch batch;
    private FrameBuffer fbo;
    private Viewport targetView;

    private ParticleSystem particleSystem;

    private float BOX_TO_WORLD;

    @SuppressWarnings("unchecked")
    public LiquidRenderSystem(Viewport viewport, ParticleSystem particleSystem) {
        super(Family.all(LiquidComponent.class).get());

        targetView = viewport;
        this.particleSystem = particleSystem;

        batch = new SpriteBatch();
        fbo = new FrameBuffer(Pixmap.Format.RGBA8888,
                Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);

        BOX_TO_WORLD = 1f / PhysicsBodyLoader.getScale();
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        engine.addEntityListener(getFamily(), this);
    }

    @Override
    public void entityAdded(Entity entity) {
        LiquidComponent liquid = liquidM.get(entity);
        if (liquid.isGroup) {
            particleSystem.createParticleGroup(liquid.particleGroupDef);
        } else {
            particleSystem.createParticle(liquid.particleDef);
        }
    }

    @Override
    public void entityRemoved(Entity entity) {

    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {

    }

    // TODO: This drawing isn't right. begin() and the entity loop are not in the correct logical order.
    @Override
    public void update(float deltaTime) {
        if (getEntities() == null) return;

        for (Entity entity : getEntities()) {
            LiquidComponent liquid = liquidM.get(entity);

            // Start drawing to the FBO
            fbo.begin();

            Gdx.gl.glClearColor(0, 0, 0, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            batch.setShader(null);
            batch.begin();
            for (Vector2 pos : particleSystem.getParticlePositionBuffer()) {
                int screenX = ViewportUtils.worldToScreenX(targetView, (int)(pos.x * BOX_TO_WORLD));
                int screenY = ViewportUtils.worldToScreenY(targetView, (int)targetView.getWorldHeight() - (int)(pos.y * BOX_TO_WORLD));

                if (isParticleVisible(screenX, screenY,
                        liquid.particleTexture.getWidth(), liquid.particleTexture.getHeight())) {
                    batch.draw(liquid.particleTexture,
                            screenX - (liquid.particleTexture.getWidth() / 2),
                            screenY - (liquid.particleTexture.getHeight() / 2)
                    );
                }
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

    private boolean isParticleVisible(int screenX, int screenY, float width, float height) {
        float left = screenX - (width / 2);
        float top = screenY + (height / 2);
        float bottom = screenY - (height / 2);
        float right = screenX + (width / 2);

        return left <= Gdx.graphics.getWidth() &&
                top >= 0 &&
                bottom <= Gdx.graphics.getHeight() &&
                right >= 0;
    }
}
