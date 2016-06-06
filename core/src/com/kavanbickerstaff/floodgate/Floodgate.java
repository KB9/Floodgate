package com.kavanbickerstaff.floodgate;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.uwsoft.editor.renderer.SceneLoader;
import com.uwsoft.editor.renderer.physics.PhysicsBodyLoader;
import com.uwsoft.editor.renderer.systems.PhysicsSystem;

import finnstr.libgdx.liquidfun.ParticleDebugRenderer;
import finnstr.libgdx.liquidfun.ParticleDef;
import finnstr.libgdx.liquidfun.ParticleGroupDef;
import finnstr.libgdx.liquidfun.ParticleSystem;
import finnstr.libgdx.liquidfun.ParticleSystemDef;

public class Floodgate extends ApplicationAdapter implements InputProcessor {

    private float BOX_TO_WORLD;
    private float WORLD_TO_BOX;

    private SceneLoader sceneLoader;
    private FitViewport sceneViewport;
    private World world;

    private ParticleSystem particleSystem;
    private ParticleDebugRenderer particleDebugRenderer;
    private Box2DDebugRenderer debugRenderer;
    private SpriteBatch particleBatch;
    private Texture particleTexture;
    private ShaderProgram waterShader;
    private FrameBuffer fbo;

    private ParticleGroupDef particleGroupDef1;
    private ParticleGroupDef particleGroupDef2;

    private int lastX, lastY;
    private float scrollSpeed;

    @Override
    public void create() {
        Gdx.input.setInputProcessor(this);

        sceneViewport = new FitViewport(800, 480);
        sceneLoader = new SceneLoader();
        sceneLoader.loadScene("MainScene", sceneViewport);

        scrollSpeed = sceneViewport.getWorldWidth() / (float)Gdx.graphics.getWidth();

        debugRenderer = new Box2DDebugRenderer();
        particleBatch = new SpriteBatch();
        particleTexture = new Texture(Gdx.files.internal("water_particle_block.png"));
        String vertexShader = Gdx.files.internal("water_shader.vert").readString();
        String fragmentShader = Gdx.files.internal("water_shader.frag").readString();
        waterShader = new ShaderProgram(vertexShader, fragmentShader);
        Gdx.app.log("SHADER", waterShader.getLog());

        world = sceneLoader.world;

        // Get the world -> Box2D scalar
        BOX_TO_WORLD = 1f / PhysicsBodyLoader.getScale();
        // Apply the ratio between screen viewport and scene viewport sizes
        //BOX_TO_WORLD *= (float)Gdx.graphics.getWidth() / sceneViewport.getWorldWidth();
        // Inverse of scale to get back to world coordinates
        WORLD_TO_BOX = 1f / BOX_TO_WORLD;

        createParticleStuff(800, 480);//(width, height);

        // Inject modified PhysicsSystem to handle particles
        Engine engine = sceneLoader.getEngine();
        engine.removeSystem(engine.getSystem(PhysicsSystem.class));
        engine.addSystem(new LiquidFunPhysicsSystem(world, particleSystem));

        particleDebugRenderer = new ParticleDebugRenderer(new Color(0, 1, 0, 1),
                particleSystem.getParticleCount());

        fbo = new FrameBuffer(Pixmap.Format.RGBA8888,
                Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
    }

    @Override
    public void render() {
        /////////////////////////////// FBO STUFF ///////////////////////////////
        fbo.begin();

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        particleBatch.begin();
        for (Vector2 pos : particleSystem.getParticlePositionBuffer()) {
            particleBatch.draw(particleTexture,
                    sceneToScreenX((int)(pos.x * BOX_TO_WORLD)) - (particleTexture.getWidth() / 2),
                    sceneToScreenY(480 - (int)(pos.y * BOX_TO_WORLD)) - (particleTexture.getHeight() / 2)
            );
        }
        particleBatch.end();

        fbo.end(sceneViewport.getScreenX(), sceneViewport.getScreenY(),
                sceneViewport.getScreenWidth(), sceneViewport.getScreenHeight());
        /////////////////////////////// FBO STUFF ///////////////////////////////

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // TODO: Read more of this source code to improve on rendering times by reducing coordinate calculations.
        sceneLoader.getEngine().update(Gdx.graphics.getDeltaTime());

        SpriteBatch fboBatch = new SpriteBatch();
        fboBatch.setShader(waterShader);
        fboBatch.begin();
        fboBatch.draw(fbo.getColorBufferTexture(), 0, 0);
        fboBatch.end();

        debugRenderer.render(world, sceneViewport.getCamera().combined);
        Gdx.app.log("FPS", Gdx.graphics.getFramesPerSecond() + "Hz (" + particleSystem.getParticleCount() + " particles)");
    }

    private void createParticleStuff(float width, float height) {
        // Create new ParticleSystem
        // Set radius of each particle to 6/120m (5cm)
        ParticleSystemDef particleSystemDef = new ParticleSystemDef();
        particleSystemDef.radius = 7.5f * WORLD_TO_BOX;
        particleSystemDef.dampingStrength = 0.2f;

        particleSystem = new ParticleSystem(world, particleSystemDef);
        particleSystem.setParticleDensity(1.3f);

        // Create new ParticleGroupDef and set properties
        particleGroupDef1 = new ParticleGroupDef();
        particleGroupDef1.color.set(1f, 0, 0, 1);
        particleGroupDef1.flags.add(ParticleDef.ParticleType.b2_waterParticle);
        particleGroupDef1.position.set(width * (30f / 100f) * WORLD_TO_BOX, height * (80f / 100f) * WORLD_TO_BOX);

        // Create Shape, give it to definition and create ParticleGroup in ParticleSystem
        PolygonShape particleShape = new PolygonShape();
        particleShape.setAsBox(width * (20f / 100f) * WORLD_TO_BOX / 2f, width * (20f / 100f) * WORLD_TO_BOX / 2f);
        particleGroupDef1.shape = particleShape;
        particleSystem.createParticleGroup(particleGroupDef1);

        // Second group with different color and shifted on x-axis
        particleGroupDef2 = new ParticleGroupDef();
        particleGroupDef2.shape = particleGroupDef1.shape;
        particleGroupDef2.flags = particleGroupDef1.flags;
        particleGroupDef2.groupFlags = particleGroupDef1.groupFlags;
        particleGroupDef2.position.set(width * (70f / 100f) * WORLD_TO_BOX, height * (80f / 100f) * WORLD_TO_BOX);
        particleGroupDef2.color.set(0.2f, 1f, 0.3f, 1);
        particleSystem.createParticleGroup(particleGroupDef2);

        // Create new Shape and set linear velocity
        // Used in createParticles1() and createParticles2()
        CircleShape circleShape = new CircleShape();
        circleShape.setRadius(18.5f * WORLD_TO_BOX);

        particleGroupDef1.shape = circleShape;
        particleGroupDef2.shape = circleShape;

        particleGroupDef1.linearVelocity.set(new Vector2(0, -10f));
        particleGroupDef2.linearVelocity.set(new Vector2(0, -10f));
    }

    public void createCircleBody(float pX, float pY, float pRadius) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(pX * WORLD_TO_BOX, pY * WORLD_TO_BOX);
        Body body = world.createBody(bodyDef);

        CircleShape shape = new CircleShape();
        shape.setRadius(pRadius * WORLD_TO_BOX);

        FixtureDef fixDef = new FixtureDef();
        fixDef.density = 1.0f;
        fixDef.friction = 0.5f;
        fixDef.shape = shape;
        fixDef.restitution = 0.5f;

        body.createFixture(fixDef);
    }

    @Override
    public void dispose() {
        particleGroupDef1.shape.dispose();
        world.dispose();
        debugRenderer.dispose();
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        lastX = screenX;
        lastY = screenY;

        createCircleBody(screenToSceneX(screenX),
                sceneViewport.getWorldHeight() - screenToSceneY(screenY),
                MathUtils.random(10, 80));
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        sceneViewport.getCamera().position.add(
                (lastX - screenX) * scrollSpeed,
                (screenY - lastY) * scrollSpeed,
                0);

        lastX = screenX;
        lastY = screenY;
        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

    public int screenToSceneX(int x) {
        int newX = (int)(((float)x / (float)Gdx.graphics.getWidth()) * sceneViewport.getWorldWidth());
        newX += sceneViewport.getCamera().position.x - (sceneViewport.getWorldWidth() / 2f);
        return newX;
    }

    public int screenToSceneY(int y) {
        int newY = (int)(((float)y / (float)Gdx.graphics.getHeight()) * sceneViewport.getWorldHeight());
        newY -= sceneViewport.getCamera().position.y - (sceneViewport.getWorldHeight() / 2f);
        return newY;
    }

    public int sceneToScreenX(int x) {
        x -= sceneViewport.getCamera().position.x - (sceneViewport.getWorldWidth() / 2f);
        return (int)(((float)x / sceneViewport.getWorldWidth()) * (float)Gdx.graphics.getWidth());
    }

    public int sceneToScreenY(int y) {
        y += sceneViewport.getCamera().position.y - (sceneViewport.getWorldHeight() / 2f);
        return (int)(((float)y / sceneViewport.getWorldHeight()) * (float)Gdx.graphics.getHeight());
    }
}
