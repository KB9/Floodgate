package com.kavanbickerstaff.floodgate;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.kavanbickerstaff.floodgate.components.LiquidComponent;
import com.kavanbickerstaff.floodgate.systems.CompatibilityPhysicsSystem;
import com.kavanbickerstaff.floodgate.systems.LiquidRenderSystem;
import com.uwsoft.editor.renderer.SceneLoader;
import com.uwsoft.editor.renderer.components.MainItemComponent;
import com.uwsoft.editor.renderer.components.physics.PhysicsBodyComponent;
import com.uwsoft.editor.renderer.physics.PhysicsBodyLoader;
import com.uwsoft.editor.renderer.systems.PhysicsSystem;
import com.uwsoft.editor.renderer.utils.ComponentRetriever;
import com.uwsoft.editor.renderer.utils.ItemWrapper;

import finnstr.libgdx.liquidfun.ParticleDebugRenderer;
import finnstr.libgdx.liquidfun.ParticleDef;
import finnstr.libgdx.liquidfun.ParticleGroupDef;
import finnstr.libgdx.liquidfun.ParticleSystem;
import finnstr.libgdx.liquidfun.ParticleSystemDef;

public class GameScreen implements Screen, InputProcessor {

    private final Floodgate game;

    private float BOX_TO_WORLD;
    private float WORLD_TO_BOX;

    private FitViewport viewport;
    private SceneLoader sceneLoader;
    private Engine engine;
    private World world;

    private ParticleDebugRenderer particleDebugRenderer;
    private Box2DDebugRenderer debugRenderer;
    private SpriteBatch batch;
    private Texture backgroundTexture;

    private int lastX, lastY;
    private float scrollSpeed;

    private Array<Entity> placeableEntities;

    public GameScreen(final Floodgate game) {
        this.game = game;

        viewport = new FitViewport(800, 480);
        sceneLoader = new SceneLoader();
        sceneLoader.loadScene("MainScene", viewport);

        // Get the world -> Box2D scalar
        BOX_TO_WORLD = 1f / PhysicsBodyLoader.getScale();
        // Apply the ratio between screen viewport and scene viewport sizes
        //BOX_TO_WORLD *= (float)Gdx.graphics.getWidth() / sceneView.getWorldWidth();
        // Inverse of scale to get back to world coordinates
        WORLD_TO_BOX = 1f / BOX_TO_WORLD;

        engine = sceneLoader.engine;
        world = sceneLoader.world;

        scrollSpeed = viewport.getWorldWidth() / (float)Gdx.graphics.getWidth();

        batch = new SpriteBatch();
        backgroundTexture = new Texture(Gdx.files.internal("sewer_background.png"));

        LiquidComponent liquid = new LiquidComponent();
        liquid.particleTexture = new Texture(Gdx.files.internal("water_particle_alpha_64.png"));
        String vertexShader = Gdx.files.internal("water_shader.vert").readString();
        String fragmentShader = Gdx.files.internal("water_shader.frag").readString();
        liquid.shader = new ShaderProgram(vertexShader, fragmentShader);
        liquid.particleSystem = createParticleStuff(viewport.getWorldWidth(), viewport.getWorldHeight());

        Entity liquidEntity = new Entity();
        liquidEntity.add(liquid);
        engine.addEntity(liquidEntity);

        // Inject modified PhysicsSystem to handle particles
        engine.removeSystem(engine.getSystem(PhysicsSystem.class));
        engine.addSystem(new CompatibilityPhysicsSystem(world, liquid.particleSystem));
        engine.addSystem(new LiquidRenderSystem(viewport));

        debugRenderer = new Box2DDebugRenderer();
        particleDebugRenderer = new ParticleDebugRenderer(new Color(0, 1, 0, 1),
                liquid.particleSystem.getParticleCount());
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Draw background with the batch
        batch.begin();
        batch.draw(backgroundTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.end();

        engine.update(Gdx.graphics.getDeltaTime());
        hidePlaceableEntities();

        debugRenderer.render(world, viewport.getCamera().combined);
        //particleDebugRenderer.render(particleSystem, BOX_TO_WORLD, sceneView.getCamera().combined);
        Gdx.app.log("FPS", Gdx.graphics.getFramesPerSecond() + "Hz"/* (" + particleSystem.getParticleCount() + " particles)"*/);
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        world.dispose();
        debugRenderer.dispose();
        batch.dispose();
    }

    private ParticleSystem createParticleStuff(float width, float height) {
        ParticleGroupDef particleGroupDef1;
        ParticleGroupDef particleGroupDef2;

        // Create new ParticleSystem
        // Set radius of each particle to 6/120m (5cm)
        ParticleSystemDef particleSystemDef = new ParticleSystemDef();
        particleSystemDef.radius = 6f * WORLD_TO_BOX;
        particleSystemDef.dampingStrength = 0.2f;

        ParticleSystem particleSystem = new ParticleSystem(world, particleSystemDef);
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

        return particleSystem;
    }

    private void createCircleBody(float pX, float pY, float pRadius) {
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

    private void placeEntity(Entity entity, float x, float y) {
        MainItemComponent main = entity.getComponent(MainItemComponent.class);
        main.visible = true;

        PhysicsBodyComponent physicsBody = entity.getComponent(PhysicsBodyComponent.class);
        physicsBody.body.setTransform(x * WORLD_TO_BOX, y * WORLD_TO_BOX, physicsBody.body.getAngle());
        physicsBody.body.setActive(true);
    }

    private void hidePlaceableEntities() {
        // TODO: Find a cleaner way to call this than from render()
        if (placeableEntities != null) return;
        placeableEntities = getEntitiesByTag("placeable");

        for (Entity entity : getEntitiesByTag("placeable")) {
            MainItemComponent main = ComponentRetriever.get(entity, MainItemComponent.class);
            main.visible = false;

            PhysicsBodyComponent physicsBody = ComponentRetriever.get(entity, PhysicsBodyComponent.class);
            if (physicsBody != null) {
                physicsBody.body.setActive(false);
            }
        }
    }

    private Array<Entity> getEntitiesByTag(String tag) {
        Array<Entity> found = new Array<Entity>();

        for (Entity entity : engine.getEntities()) {
            MainItemComponent main = ComponentRetriever.get(entity, MainItemComponent.class);
            if (main != null && main.tags.contains(tag)) {
                found.add(entity);
            }
        }

        return found;
    }

    private Entity getEntityById(String id) {
        ItemWrapper item = new ItemWrapper(sceneLoader.getRoot());
        return item.getChild(id).getEntity();
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

        placeEntity(placeableEntities.get(0),
                ViewportUtils.screenToSceneX(viewport, screenX),
                viewport.getWorldHeight() - ViewportUtils.screenToSceneY(viewport, screenY));

        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        viewport.getCamera().position.add(
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
}
