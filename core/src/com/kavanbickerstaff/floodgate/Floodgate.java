package com.kavanbickerstaff.floodgate;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
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
import com.uwsoft.editor.renderer.systems.PhysicsSystem;

import finnstr.libgdx.liquidfun.ParticleDebugRenderer;
import finnstr.libgdx.liquidfun.ParticleDef;
import finnstr.libgdx.liquidfun.ParticleGroupDef;
import finnstr.libgdx.liquidfun.ParticleSystem;
import finnstr.libgdx.liquidfun.ParticleSystemDef;

public class Floodgate extends ApplicationAdapter implements InputProcessor {

    private static final float BOX_TO_WORLD = 120.0f;
    private static final float WORLD_TO_BOX = 1f / BOX_TO_WORLD;

    /*
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private Texture texture;
    private Sprite sprite;

    private World world;
    private ParticleSystem particleSystem;
    private ParticleDebugRenderer particleDebugRenderer;
    private Box2DDebugRenderer debugRenderer;

    private ParticleGroupDef particleGroupDef1;
    private ParticleGroupDef particleGroupDef2;

    @Override
    public void create() {
        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();

        camera = new OrthographicCamera(width, height);
        camera.position.set(width / 2, height / 2, 0);
        camera.update();
        Gdx.input.setInputProcessor(this);

        batch = new SpriteBatch();
        texture = new Texture(Gdx.files.internal("badlogic.jpg"));
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        TextureRegion region = new TextureRegion(texture, 0, 0, 512, 275);

        sprite = new Sprite(region);
        sprite.setSize(width, height);
        sprite.setOrigin(sprite.getWidth() / 2, sprite.getHeight() / 2);
        sprite.setPosition(0, 0);

        createBox2DWorld(width, height);
        createParticleStuff(width, height);

        // Render stuff
        debugRenderer = new Box2DDebugRenderer();
        particleDebugRenderer = new ParticleDebugRenderer(new Color(0, 1, 0, 1),
                particleSystem.getParticleCount());

        // Version
        Gdx.app.log("LIQUIDFUN_VERSION", particleSystem.getVersionString());
        updateLog();
    }

    private void createBox2DWorld(float width, float height) {
        world = new World(new Vector2(0, -9.8f), false);

        // Bottom
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(width * WORLD_TO_BOX / 2f, height * (2f / 100f) * WORLD_TO_BOX / 2f);
        //bodyDef.angle = (float)Math.toRadians(-30);
        Body ground = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width * WORLD_TO_BOX / 2f, height * (2f / 100f) * WORLD_TO_BOX / 2f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.friction = 0.2f;
        fixtureDef.shape = shape;
        ground.createFixture(fixtureDef);

        shape.dispose();

        // Walls
        BodyDef bodyDef1 = new BodyDef();
        bodyDef1.type = BodyDef.BodyType.StaticBody;
        bodyDef1.position.set(width * (2f / 100f) * WORLD_TO_BOX / 2f, height * WORLD_TO_BOX / 2);
        Body left = world.createBody(bodyDef1);

        bodyDef1.position.set(width * WORLD_TO_BOX - width * (2f / 100f) * WORLD_TO_BOX / 2f, height * WORLD_TO_BOX / 2);
        Body right = world.createBody(bodyDef1);

        shape = new PolygonShape();
        shape.setAsBox(width * (2f / 100f) * WORLD_TO_BOX / 2f, height * WORLD_TO_BOX / 2);
        fixtureDef.shape = shape;

        left.createFixture(fixtureDef);
        right.createFixture(fixtureDef);
        shape.dispose();
    }

    private void createParticleStuff(float width, float height) {
        // Create new ParticleSystem
        // Set radius of each particle to 6/120m (5cm)
        ParticleSystemDef particleSystemDef = new ParticleSystemDef();
        particleSystemDef.radius = 6f * WORLD_TO_BOX;
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

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        world.step(Gdx.graphics.getDeltaTime(), 10, 6,
                particleSystem.calculateReasonableParticleIterations(Gdx.graphics.getDeltaTime()));

//        batch.setProjectionMatrix(camera.combined);
//        batch.begin();
//        sprite.draw(batch);
//        batch.end();

        // Get combined matrix, scale down to Box2D size
        Matrix4 cameraCombined = camera.combined.cpy();
        cameraCombined.scale(BOX_TO_WORLD, BOX_TO_WORLD, 1);

        // Render particles then render Box2D world
        particleDebugRenderer.render(particleSystem, BOX_TO_WORLD, cameraCombined);
        debugRenderer.render(world, cameraCombined);
    }

    @Override
    public void dispose() {
        batch.dispose();
        texture.dispose();
        particleGroupDef1.shape.dispose();
        world.dispose();
        debugRenderer.dispose();
    }

    public void createParticles1(float pX, float pY) {
        particleGroupDef1.position.set(pX * WORLD_TO_BOX, pY * WORLD_TO_BOX);
        particleSystem.createParticleGroup(particleGroupDef1);
        updateParticleCount();
        updateLog();
    }

    private void createParticles2(float pX, float pY) {
        particleGroupDef2.position.set(pX * WORLD_TO_BOX, pY * WORLD_TO_BOX);
        particleSystem.createParticleGroup(particleGroupDef2);
        updateParticleCount();
        updateLog();
    }

    private void updateParticleCount() {
        if(particleSystem.getParticleCount() > particleDebugRenderer.getMaxParticleNumber()) {
            particleDebugRenderer.setMaxParticleNumber(particleSystem.getParticleCount() + 1000);
        }
    }

    public void updateLog() {
        //Here we log the total particle count and the f/s
        Gdx.app.log("LIQUIDFUN", "Total particles: " + particleSystem.getParticleCount() +
                " FPS: " + Gdx.graphics.getFramesPerSecond());
    }

    public void createCircleBody(float pX, float pY, float pRadius) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(pX * WORLD_TO_BOX, pY * WORLD_TO_BOX);
        Body body = world.createBody(bodyDef);

        CircleShape shape = new CircleShape();
        shape.setRadius(pRadius * WORLD_TO_BOX);

        FixtureDef fixDef = new FixtureDef();
        fixDef.density = 0.5f;
        fixDef.friction = 0.2f;
        fixDef.shape = shape;
        fixDef.restitution = 0.3f;

        body.createFixture(fixDef);
        updateLog();
    }
    */

    private SceneLoader sceneLoader;
    private Engine engine;
    private World world;

    @Override
    public void create() {
        Gdx.input.setInputProcessor(this);

        FitViewport viewport = new FitViewport(800, 480);
        sceneLoader = new SceneLoader();
        sceneLoader.loadScene("MainScene", viewport);

        engine = sceneLoader.getEngine();
        world = sceneLoader.world;
        Gdx.app.log("ENTITY_COUNT", "" + engine.getEntities().size());
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        sceneLoader.getEngine().update(Gdx.graphics.getDeltaTime());
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
        //createCircleBody(screenX, Gdx.graphics.getHeight() - screenY, MathUtils.random(10, 80));
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
