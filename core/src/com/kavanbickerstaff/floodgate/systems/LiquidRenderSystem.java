package com.kavanbickerstaff.floodgate.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
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
import com.kavanbickerstaff.floodgate.GameScreen;
import com.kavanbickerstaff.floodgate.ViewportUtils;
import com.kavanbickerstaff.floodgate.components.LiquidComponent;
import com.uwsoft.editor.renderer.physics.PhysicsBodyLoader;

import finnstr.libgdx.liquidfun.ParticleDebugRenderer;
import finnstr.libgdx.liquidfun.ParticleSystem;

public class LiquidRenderSystem extends IteratingSystem implements EntityListener {

    private ComponentMapper<LiquidComponent> liquidM = ComponentMapper.getFor(LiquidComponent.class);

    private SpriteBatch batch;
    private FrameBuffer fbo;
    private OrthographicCamera camera;

    private ParticleSystem particleSystem;
    private Texture particleTexture;

    private ShaderProgram shader;

    // Defines how downscaled the FBO should be with respect to screen size
    private final int FBO_DIVISOR = 25;

    @SuppressWarnings("unchecked")
    public LiquidRenderSystem(OrthographicCamera camera, ParticleSystem particleSystem) {
        super(Family.all(LiquidComponent.class).get());

        this.camera = camera;
        this.particleSystem = particleSystem;

        particleTexture = new Texture(Gdx.files.internal("water_particle_alpha_64.png"));

        String vertexShader = Gdx.files.internal("water_shader.vert").readString();
        String fragmentShader = Gdx.files.internal("water_shader.frag").readString();
        shader = new ShaderProgram(vertexShader, fragmentShader);
        Gdx.app.log("SHADER", shader.getLog());

        batch = new SpriteBatch();

        fbo = new FrameBuffer(Pixmap.Format.RGB888,
                Gdx.graphics.getWidth() / FBO_DIVISOR, Gdx.graphics.getHeight() / FBO_DIVISOR, false);
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

    // TODO: Ensure that particles are displayed the same size regardless of the resolution.
    // Particles look bigger on a screen with a smaller resolution.

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

        // Transform and draw the particles to the FBO batch
        for (Vector2 pos : particleSystem.getParticlePositionBuffer()) {
            int screenX = ViewportUtils.worldToScreenX(camera, (int)(pos.x * GameScreen.BOX_TO_WORLD));
            int screenY = Gdx.graphics.getHeight() - ViewportUtils.worldToScreenY(camera, (int)(pos.y * GameScreen.BOX_TO_WORLD));
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
//                batch.draw(particleTexture,
//                        screenX - (particleTexture.getWidth() / 2),
//                        screenY - (particleTexture.getHeight() / 2)
//                );
            }
        }

        batch.end();

        // Stop drawing to the FBO
        fbo.end();

        // TODO: May want to look into bilinear interpolation when scaling the FBO up
        //fbo.getColorBufferTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        // Set the uniforms used in Gaussian blur calculation
        batch.setShader(shader);
        shader.setUniformf("dir", 0f, 0f);
        shader.setUniformf("resolution", 1.0f);//Gdx.graphics.getHeight() / (float)FBO_DIVISOR);
        shader.setUniformf("radius", 1920.0f);//2.5f);

        // Draw the FBO to the batch
        batch.begin();
        batch.draw(fbo.getColorBufferTexture(), 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

//        float originX = (float)Gdx.graphics.getWidth() / 2.0f;
//        float originY = (float)Gdx.graphics.getHeight() / 2.0f;
//
//        float frameWidth = (float)Gdx.graphics.getWidth() / camera.zoom;
//        float frameHeight = (float)Gdx.graphics.getHeight() / camera.zoom;
//        float frameX = originX - (frameWidth / 2.0f);
//        float frameY = originY - (frameHeight / 2.0f);
//
//        Gdx.app.log("ZOOM", "x: " + frameX + " y: " + frameY + " w: " + frameWidth + " h: " + frameHeight + " zoom: " + camera.zoom);
//        if (DEBUG_zoom_up) {
//            if (camera.zoom < 2.0) {
//                camera.zoom += 0.01;
//            } else {
//                DEBUG_zoom_up = false;
//            }
//        } else {
//            if (camera.zoom > 0.75) {
//                camera.zoom -= 0.01;
//            } else {
//                DEBUG_zoom_up = true;
//            }
//        }
//
//        batch.draw(fbo.getColorBufferTexture(), frameX, frameY, frameWidth, frameHeight);

        batch.end();
    }

//    private boolean DEBUG_zoom_up;

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
