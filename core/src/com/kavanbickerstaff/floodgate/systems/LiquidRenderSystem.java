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
import com.badlogic.gdx.math.Affine2;
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

    // Boundaries for bounding box check against particle textures (screen dimensions)
    private float minX, maxX, minY, maxY;

    // Defines how downscaled the FBO should be with respect to screen size
    private final int FBO_DIVISOR = 5;

    @SuppressWarnings("unchecked")
    public LiquidRenderSystem(OrthographicCamera camera, ParticleSystem particleSystem) {
        super(Family.all(LiquidComponent.class).get());

        this.camera = camera;
        this.particleSystem = particleSystem;

        particleTexture = new Texture(Gdx.files.internal("water_particle_alpha.png"));

        String vertexShader = Gdx.files.internal("water_shader.vert").readString();
        String fragmentShader = Gdx.files.internal("water_shader.frag").readString();
        shader = new ShaderProgram(vertexShader, fragmentShader);
        Gdx.app.log("SHADER", shader.getLog());

        batch = new SpriteBatch();

        fbo = new FrameBuffer(Pixmap.Format.RGB888,
                Gdx.graphics.getWidth() / FBO_DIVISOR, Gdx.graphics.getHeight() / FBO_DIVISOR, false);

        minX = 0;
        maxX = camera.viewportWidth;
        minY = Gdx.graphics.getHeight() - camera.viewportHeight;
        maxY = Gdx.graphics.getHeight();
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

        float viewportWidth = camera.viewportWidth;
        float viewportHeight = camera.viewportHeight;
        float halfViewportWidth = viewportWidth / 2f;
        float halfViewportHeight = viewportHeight / 2f;

        // Parameters for offsetting the particle positions according to camera position
        float cameraOffsetX = (camera.position.x - halfViewportWidth);
        float cameraOffsetY = (camera.position.y - halfViewportHeight);

        // Parameters for bounding box check
        float halfTexWidth = (particleTexture.getWidth() / 2f) / camera.zoom;
        float halfTexHeight = (particleTexture.getHeight() / 2f) / camera.zoom;

        // Constants for position scaling algorithm
        float invZoomMinusOne = (1f / camera.zoom) - 1;
        float reverseViewportHeight = Gdx.graphics.getHeight() - halfViewportHeight;

        for (Vector2 pos : particleSystem.getParticlePositionBuffer()) {
            // Convert Box2D coordinates to world coordinates
            float worldX = pos.x * GameScreen.BOX_TO_WORLD;
            float worldY = Gdx.graphics.getHeight() - (pos.y * GameScreen.BOX_TO_WORLD);

            // Adjust screen position according to camera positioning
            worldX -= cameraOffsetX;
            worldY += cameraOffsetY;

            /*
            float distFromCenterX = worldX - halfViewportWidth;
            float distFromCenterY = worldY - (Gdx.graphics.getHeight() - halfViewportHeight);

            float scaledDistFromCenterX = (1f / camera.zoom) * distFromCenterX;
            float scaledDistFromCenterY = (1f / camera.zoom) * distFromCenterY;

            float drawX = worldX + (scaledDistFromCenterX - distFromCenterX);
            float drawY = worldY + (scaledDistFromCenterY - distFromCenterY);
            */

            // Simplified position scaling calculation (expanded calculation shown above)
            float drawX = worldX + (worldX - halfViewportWidth) * invZoomMinusOne;
            float drawY = worldY + (worldY - reverseViewportHeight) * invZoomMinusOne;

            // TODO: Should really take into account particle texture width here
            // Bounding box check to ensure particle is inside screen bounds before drawing
            if (isParticleVisible(drawX, drawY, halfTexWidth, halfTexHeight)) {

                // This is, for some reason, required as the shader inverts the framebuffer texture
                drawY = Gdx.graphics.getHeight() - drawY;

                batch.draw(particleTexture,
                        drawX - halfTexWidth,
                        drawY - halfTexHeight,
                        particleTexture.getWidth() / camera.zoom,
                        particleTexture.getHeight() / camera.zoom
                );
            }
        }

        batch.end();

        fbo.end();

        batch.setShader(shader);
        batch.begin();

        float scaleX = (Gdx.graphics.getWidth() / viewportWidth);
        float scaleY = (Gdx.graphics.getHeight() / viewportHeight);
        batch.getShader().setUniformf("u_size", Gdx.graphics.getWidth() * scaleX, Gdx.graphics.getHeight() * scaleY);

        batch.draw(fbo.getColorBufferTexture(), 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        batch.end();
    }

    private boolean isParticleVisible(float screenX, float screenY, float halfWidth, float halfHeight) {
        float left = screenX - halfWidth;
        float top = screenY + halfHeight;
        float bottom = screenY - halfHeight;
        float right = screenX + halfWidth;

        return left <= maxX && right >= minX && top >= minY && bottom <= maxY;
    }
}
