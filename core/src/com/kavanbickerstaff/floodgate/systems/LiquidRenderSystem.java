package com.kavanbickerstaff.floodgate.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
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
    private OrthographicCamera camera;

    private ParticleSystem particleSystem;
    private Texture particleTexture;
    private ShaderProgram shader;

    private float BOX_TO_WORLD;

    @SuppressWarnings("unchecked")
    public LiquidRenderSystem(OrthographicCamera camera, ParticleSystem particleSystem,
                              Texture particleTexture, ShaderProgram shader) {
        super(Family.all(LiquidComponent.class).get());

        this.camera = camera;
        this.particleSystem = particleSystem;
        this.particleTexture = particleTexture;
        this.shader = shader;

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
            liquid.particleGroupDef.shape.dispose();
        } else {
            particleSystem.createParticle(liquid.particleDef);
            // TODO: Dispose of the particle shape
        }
    }

    @Override
    public void entityRemoved(Entity entity) {

    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {

    }

    @Override
    public void update(float deltaTime) {
        if (getEntities().size() == 0) return;

        // Start drawing to the FBO
        fbo.begin();

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setShader(null);
        camera.update();
        batch.begin();

        for (Vector2 pos : particleSystem.getParticlePositionBuffer()) {
            int screenX = ViewportUtils.worldToScreenX(camera, (int)(pos.x * BOX_TO_WORLD));
            int screenY = Gdx.graphics.getHeight() - ViewportUtils.worldToScreenY(camera, (int)(pos.y * BOX_TO_WORLD));
            float scaledWidth = particleTexture.getWidth() / camera.zoom;
            float scaledHeight = particleTexture.getHeight() / camera.zoom;

            if (isParticleVisible(screenX, screenY,
                    particleTexture.getWidth(), particleTexture.getHeight())) {
                batch.draw(particleTexture,
                        screenX - (scaledWidth / 2),
                        screenY - (scaledHeight / 2),
                        scaledWidth,
                        scaledHeight
                );
            }
        }

        batch.end();

        // Stop drawing to the FBO
        fbo.end();

        batch.setShader(shader);
        batch.begin();
        batch.draw(fbo.getColorBufferTexture(), 0, 0);
        batch.end();
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
