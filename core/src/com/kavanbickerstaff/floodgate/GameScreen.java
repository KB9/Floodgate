package com.kavanbickerstaff.floodgate;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.kavanbickerstaff.floodgate.components.LiquidComponent;
import com.kavanbickerstaff.floodgate.components.InventoryComponent;
import com.kavanbickerstaff.floodgate.components.LiquidSpawnComponent;
import com.kavanbickerstaff.floodgate.systems.CompatibilityPhysicsSystem;
import com.kavanbickerstaff.floodgate.systems.InventorySystem;
import com.kavanbickerstaff.floodgate.systems.LiquidRenderSystem;
import com.kavanbickerstaff.floodgate.systems.LiquidSpawnSystem;
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

    // TODO: Moved from FitViewport to StretchViewport.
    // Viewport seems to affect SpriteBatch drawing coordinates. For example,
    // when I use FitViewport(800,480), black bars appear at the sides of the screen.
    // When attempting to draw to these black bars, the drawing is clamped to the viewport
    // coordinates.
    private StretchViewport viewport;
    private SceneLoader sceneLoader;
    private Engine engine;
    private World world;

    private ParticleSystem debugParticleSystem;
    private ParticleDebugRenderer particleDebugRenderer;
    private Box2DDebugRenderer debugRenderer;

    private SpriteBatch batch;
    private Texture backgroundTexture;

    private HUD hud;

    private int lastX, lastY;
    private float scrollSpeed;

    private BitmapFont font;

    private Entity heldEntity;

    public GameScreen(final Floodgate game) {
        this.game = game;

        // Load the scene using the specified viewport size
        viewport = new StretchViewport(800, 480);
        sceneLoader = new SceneLoader();
        sceneLoader.loadScene("MainScene", viewport);

        // Get the world -> Box2D scalar
        BOX_TO_WORLD = 1f / PhysicsBodyLoader.getScale();
        // Apply the ratio between screen viewport and scene viewport sizes
        //BOX_TO_WORLD *= (float)Gdx.graphics.getWidth() / sceneView.getWorldWidth();
        // Inverse of scale to get back to world coordinates
        WORLD_TO_BOX = 1f / BOX_TO_WORLD;

        // Retrieve pre-initialized engine and world from scene loader
        engine = sceneLoader.engine;
        world = sceneLoader.world;

        // Initialize particle system for liquid rendering and physics
        ParticleSystem particleSystem = createParticleSystem();
        debugParticleSystem = particleSystem;

        // Create the HUD
        hud = new HUD(new Texture(Gdx.files.internal("hud.png")), 5,
                Gdx.graphics.getWidth() - 200, 0, 200, Gdx.graphics.getHeight());

        // Inject modified PhysicsSystem to handle particles
        engine.removeSystem(engine.getSystem(PhysicsSystem.class));
        engine.addSystem(new CompatibilityPhysicsSystem(world, particleSystem));
        String vertexShader = Gdx.files.internal("water_shader.vert").readString();
        String fragmentShader = Gdx.files.internal("water_shader.frag").readString();
        engine.addSystem(new LiquidRenderSystem(viewport, particleSystem,
                new Texture(Gdx.files.internal("water_particle_alpha_64.png")),
                new ShaderProgram(vertexShader, fragmentShader)));
        engine.addSystem(new InventorySystem(hud));
        engine.addSystem(new LiquidSpawnSystem());

        // Add InventoryComponents to all entities marked with "placeable" tag
        for (Entity entity : getEntitiesByTag("placeable")) {
            entity.add(new InventoryComponent());
        }

        // Add LiquidSpawnComponents to all entities marker with "liquid_spawn" tag
        for (Entity entity : getEntitiesByTag("liquid_spawn")) {
            LiquidSpawnComponent liquidSpawn = new LiquidSpawnComponent();
            liquidSpawn.on = true;
            liquidSpawn.spawnTimeMillis = 5000;
            entity.add(liquidSpawn);
        }

        scrollSpeed = viewport.getWorldWidth() / (float)Gdx.graphics.getWidth();

        batch = new SpriteBatch();
        backgroundTexture = new Texture(Gdx.files.internal("sewer_background.png"));

        // FreeType font initialization
        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/arial.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter fontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        fontParameter.size = 50;
        font = fontGenerator.generateFont(fontParameter);
        fontGenerator.dispose();

        // Debug renderer initialization
        debugRenderer = new Box2DDebugRenderer();
        particleDebugRenderer = new ParticleDebugRenderer(new Color(0, 1, 0, 1),
                particleSystem.getParticleCount());
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

        debugRenderer.render(world, viewport.getCamera().combined);
        //particleDebugRenderer.render(debugParticleSystem, BOX_TO_WORLD, viewport.getCamera().combined);

        hud.render();

        batch.begin();
        font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 0, Gdx.graphics.getHeight());
        font.draw(batch, "Particles: " + debugParticleSystem.getParticleCount(), 0, Gdx.graphics.getHeight() - (font.getCapHeight() + 10));
        font.draw(batch, "Entities: " + engine.getEntities().size(), 0, Gdx.graphics.getHeight() - (font.getCapHeight() + 10) * 2);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, false);
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

    private ParticleSystem createParticleSystem() {
        ParticleSystemDef particleSystemDef = new ParticleSystemDef();
        particleSystemDef.radius = 6f * WORLD_TO_BOX;
        particleSystemDef.dampingStrength = 0.2f;
        particleSystemDef.density = 1.3f;   // Watch out for this!

        return new ParticleSystem(world, particleSystemDef);
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
        ItemWrapper child = item.getChild(id);
        if (child != null) {
            return child.getEntity();
        }
        return null;
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

        if (screenX >= hud.getX() && screenX <= (hud.getX() + hud.getWidth()) &&
                screenY >= hud.getY() && screenY <= (hud.getY() + hud.getHeight())) {

            int entityId = hud.getItemIdFromPosition(screenX, Gdx.graphics.getHeight() - screenY);
            if (entityId >= 0) {
                // Faster search if iterating on subset rather than iterating over all entities
                for (Entity e : engine.getSystem(InventorySystem.class).getEntities()) {
                    MainItemComponent main = e.getComponent(MainItemComponent.class);
                    InventoryComponent inventory = e.getComponent(InventoryComponent.class);
                    if (main != null && inventory != null && main.uniqueId == entityId) {
                        main.visible = true;
                        hud.setAlpha(0.25f);
                        heldEntity = e;
                        break;
                    }
                }
            }
        }

        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (heldEntity != null) {

            if (screenX >= hud.getX() && screenX <= (hud.getX() + hud.getWidth()) &&
                    screenY >= hud.getY() && screenY <= (hud.getY() + hud.getHeight())) {
                MainItemComponent main = ComponentRetriever.get(heldEntity, MainItemComponent.class);
                main.visible = false;
            } else {
                heldEntity.remove(InventoryComponent.class);
                heldEntity = null;
            }
            hud.setAlpha(1);
            heldEntity = null;
        }

        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (heldEntity != null) {
            PhysicsBodyComponent physicsBody = ComponentRetriever.get(heldEntity, PhysicsBodyComponent.class);
            physicsBody.body.setTransform(
                    ViewportUtils.screenToWorldX(viewport, screenX) * WORLD_TO_BOX,
                    ViewportUtils.screenToWorldY(viewport, Gdx.graphics.getHeight() - screenY) * WORLD_TO_BOX,
                    physicsBody.body.getAngle()
            );
        } else {
            viewport.getCamera().position.add(
                    (lastX - screenX) * scrollSpeed,
                    (screenY - lastY) * scrollSpeed,
                    0);
        }

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
