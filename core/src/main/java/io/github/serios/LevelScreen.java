package io.github.serios;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import com.badlogic.gdx.utils.Array;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;


public class LevelScreen implements Screen {
    private World world;
    private final Main game;
    private Box2DDebugRenderer debugRenderer;
    private OrthographicCamera camera;
    private Viewport viewport;
    private SpriteBatch batch;
    private Texture image;
    private Texture background;
    private Texture pauseButtonTexture;
    private Body redBody, pigBody;
    private Body groundBody;
    private Body pauseButtonBody;
    private MouseJoint mouseJoint;
    private MouseJointDef mouseJointDef;
    private Body dummyBody;
    private Body slingBody, woodboxBody;
    private Texture slingTexture, pigTexture;
    private Texture woodstickhorizontalTexture, woodboxTexture, woodtriangleTexture;
    private boolean redBodyLaunched = false;

    private boolean isPaused = false;
    private final String worldStateFile = "level1.json";


    public LevelScreen(Main game) {
        this.game = game;
        camera = new OrthographicCamera();
        viewport = new FitViewport(800 / 100f, 480 / 100f, camera);
        camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);
        camera.update();

        batch = new SpriteBatch();
        background = new Texture("levelBackground.jpg");
        image = new Texture("red.png");
        pauseButtonTexture = new Texture("pausebutton.png");
        slingTexture = new Texture("slingshot.png");
        pigTexture = new Texture("pig.png");
        woodstickhorizontalTexture = new Texture("woodstickhorizontal.png");
        woodboxTexture = new Texture("woodbox.png");
        woodtriangleTexture = new Texture("woodtriangle.png");

        world = new World(new Vector2(0, -9.8f), true);
        debugRenderer = new Box2DDebugRenderer();

        createFallingBody();
        createGroundBody();
        createSlingshotBody();
        createPauseButton();
        setupInputProcessor();

        // Create a dummy body for the MouseJoint
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        dummyBody = world.createBody(bodyDef);

        // Set ContactListener
        world.setContactListener(
            new ContactListener() {
                @Override
                public void beginContact(Contact contact) {
                    Fixture fixtureA = contact.getFixtureA();
                    Fixture fixtureB = contact.getFixtureB();

                    // Check if one fixture is from redBody and the other from pigBody
                    if ((fixtureA.getBody() == redBody && fixtureB.getBody() == pigBody)
                        || (fixtureA.getBody() == pigBody && fixtureB.getBody() == redBody)) {
                        // Flag pigBody for removal
                        pigBody.setUserData("destroy");
                    }

                    // Check if one fixture is from redBody and the other from woodboxBody
                    if ((fixtureA.getBody() == redBody && fixtureB.getBody() == woodboxBody)
                        || (fixtureA.getBody() == woodboxBody && fixtureB.getBody() == redBody)) {
                        // Flag woodboxBody for removal
                        woodboxBody.setUserData("destroy");
                    }

                    // Check if one fixture is from pigBody and the other from groundBody
                    if ((fixtureA.getBody() == pigBody && fixtureB.getBody() == groundBody)
                        || (fixtureA.getBody() == groundBody && fixtureB.getBody() == pigBody)) {
                        // Flag pigBody for removal
                        pigBody.setUserData("destroy");
                    }
                }

                @Override
                public void endContact(Contact contact) {}

                @Override
                public void preSolve(Contact contact, Manifold oldManifold) {}

                @Override
                public void postSolve(Contact contact, ContactImpulse impulse) {}
            });
    }

    private void createPauseButton() {
        // Define the body for the pause button
        BodyDef buttonBodyDef = new BodyDef();
        buttonBodyDef.type = BodyDef.BodyType.StaticBody;
        buttonBodyDef.position.set(0.5f, viewport.getWorldHeight() - 0.5f); // Top left corner

        pauseButtonBody = world.createBody(buttonBodyDef);

        // Define the shape for the button
        PolygonShape buttonShape = new PolygonShape();
        buttonShape.setAsBox(0.5f, 0.5f); // Adjust size as needed

        // Define the fixture for the button
        FixtureDef buttonFixtureDef = new FixtureDef();
        buttonFixtureDef.shape = buttonShape;
        buttonFixtureDef.isSensor = true; // Make it a sensor to detect touch

        pauseButtonBody.createFixture(buttonFixtureDef);
        buttonShape.dispose();
    }

    private boolean isTouchingPauseButton(Vector2 touchPoint) {
        for (Fixture fixture : pauseButtonBody.getFixtureList()) {
            if (fixture.testPoint(touchPoint)) {
                return true;
            }
        }
        return false;
    }

    private void createFallingBody() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(viewport.getWorldWidth() / 4.5f, 1.6f);
        redBody = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        float halfWidth = image.getWidth() / 2f / 100f / 30f;
        float halfHeight = image.getHeight() / 2f / 100f / 30f;
        shape.setAsBox(halfWidth, halfHeight);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1f;
        fixtureDef.friction = 0.5f;
        fixtureDef.restitution = 0.2f;

        redBody.createFixture(fixtureDef);
        shape.dispose();

        // Create a pig
        BodyDef pigBodyDef = new BodyDef();
        pigBodyDef.type = BodyDef.BodyType.DynamicBody;
        pigBodyDef.position.set(viewport.getWorldWidth() / 1.5f, viewport.getWorldHeight() - 1);
        pigBody = world.createBody(pigBodyDef);

        PolygonShape pigShape = new PolygonShape();
        float pigHalfWidth = pigTexture.getWidth() / 2f / 100f / 30f;
        float pigHalfHeight = pigTexture.getHeight() / 2f / 100f / 30f;
        pigShape.setAsBox(pigHalfWidth, pigHalfHeight);

        FixtureDef pigFixtureDef = new FixtureDef();
        pigFixtureDef.shape = pigShape;
        pigFixtureDef.density = 1f;
        pigFixtureDef.friction = 0.5f;
        pigFixtureDef.restitution = 0.2f;

        pigBody.createFixture(pigFixtureDef);
        pigShape.dispose();

        BodyDef woodboxBodyDef = new BodyDef();
        woodboxBodyDef.type = BodyDef.BodyType.DynamicBody;
        woodboxBodyDef.position.set(viewport.getWorldWidth() / 1.5f, viewport.getWorldHeight() - 1);
        woodboxBody = world.createBody(woodboxBodyDef);

        PolygonShape woodboxShape = new PolygonShape();
        float woodboxHalfWidth = woodboxTexture.getWidth() / 2f / 100f / 5f;
        float woodboxHalfHeight = woodboxTexture.getHeight() / 2f / 100f / 5f;
        woodboxShape.setAsBox(woodboxHalfWidth, woodboxHalfHeight);

        FixtureDef woodboxFixtureDef = new FixtureDef();
        woodboxFixtureDef.shape = woodboxShape;
        woodboxFixtureDef.density = 1f;
        woodboxFixtureDef.friction = 0.5f;
        woodboxFixtureDef.restitution = 0.2f;

        woodboxBody.createFixture(woodboxFixtureDef);
        woodboxShape.dispose();
    }

    private void createGroundBody() {
        float groundY = 1f;

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(viewport.getWorldWidth() / 2, groundY);

        groundBody = world.createBody(bodyDef);

        EdgeShape groundShape = new EdgeShape();
        groundShape.set(-viewport.getWorldWidth() / 2, 0, viewport.getWorldWidth() / 2, 0);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = groundShape;
        fixtureDef.friction = 0.8f;

        groundBody.createFixture(fixtureDef);
        groundShape.dispose();
    }

    private void createSlingshotBody() {
        BodyDef slingBodyDef = new BodyDef();
        slingBodyDef.type = BodyDef.BodyType.StaticBody;
        slingBodyDef.position.set(
            viewport.getWorldWidth() / 4.5f, 1.4f); // Set the position of the slingshot

        slingBody = world.createBody(slingBodyDef);

//    PolygonShape slingshotShape = new PolygonShape();
//    float slingshotWidth = (slingTexture.getWidth() / 100f / 30f) * 0.05f;
//    float slingshotHeight = (slingTexture.getHeight() / 100f / 30f) * 0.9f;
//    slingshotShape.setAsBox(slingshotWidth / 2, slingshotHeight / 2);
//
//    FixtureDef fixtureDef = new FixtureDef();
//    fixtureDef.shape = slingshotShape;
//
//    slingBody.createFixture(fixtureDef);
//    slingshotShape.dispose();
    }

    private Vector2 dragStart = new Vector2(); // To store the drag start position
    private Vector2 slingshotAnchor; // Slingshot anchor point
    private float maxDragRadius = 2.0f;

    private void setupInputProcessor() {
        slingshotAnchor = new Vector2(viewport.getWorldWidth() / 4.5f, 1.4f);
        Gdx.input.setInputProcessor(
            new InputAdapter() {
                @Override
                public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                    Vector2 worldCoordinates = viewport.unproject(new Vector2(screenX, screenY));
                    if (!redBodyLaunched) {// Allow touch only if not launched
                        redBody.setType(BodyDef.BodyType.DynamicBody);
                        if (redBody.getFixtureList().first().testPoint(worldCoordinates)) {
                            if (mouseJoint == null) {
                                mouseJointDef = new MouseJointDef();
                                mouseJointDef.bodyA = dummyBody;
                                mouseJointDef.bodyB = redBody;
                                mouseJointDef.collideConnected = true;
                                mouseJointDef.target.set(worldCoordinates);
                                mouseJointDef.maxForce = 1000.0f * redBody.getMass();
                                mouseJoint = (MouseJoint) world.createJoint(mouseJointDef);
                                dragStart.set(worldCoordinates); // Store the start position
                            }
                            return true;
                        }
                    }
                    if (isTouchingPauseButton(worldCoordinates)) {
//                        saveWorldState();
                        game.setScreen(new PauseScreen(game, 1));
                        return true;
                    }
                    return false;
                }

                private static final float DRAG_RADIUS = 0.5f;

                @Override
                public boolean touchDragged(int screenX, int screenY, int pointer) {
                    Vector2 worldCoordinates = viewport.unproject(new Vector2(screenX, screenY));
                    if (mouseJoint != null) {
                        if (worldCoordinates.dst(dragStart) <= DRAG_RADIUS) {
                            mouseJoint.setTarget(worldCoordinates);
                        }
                    }
                    return true;
                }

                @Override
                public boolean keyDown(int keycode) {
                    if (keycode == Input.Keys.ESCAPE) {// Check if the "P" key is pressed

                        game.setScreen(new PauseScreen(game, 1)); // Switch to PauseScreen
                        return true; // Prevent further processing of the key
                    }
                    return false;
                }

                @Override
                public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                    if (mouseJoint != null && !redBodyLaunched) {
                        // Calculate release velocity
                        Vector2 releasePosition = viewport.unproject(new Vector2(screenX, screenY));
                        Vector2 dragDirection =
                            dragStart.sub(releasePosition); // Opposite direction of the drag
                        float velocityMultiplier = 7f; // Adjust this for speed control
                        Vector2 releaseVelocity = dragDirection.scl(velocityMultiplier);

                        // Apply the velocity to the redBody
                        redBody.setLinearVelocity(releaseVelocity);

                        // Destroy the mouse joint
                        world.destroyJoint(mouseJoint);
                        mouseJoint = null;

                        // Mark the redBody as launched
                        redBodyLaunched = true;
                    }
                    return true;
                }
            });
    }

//    private void saveWorldState() {
//        Json json = new Json(JsonWriter.OutputType.json);
//        Array<BodyState> bodyStates = new Array<>();
//
//        for (Body body : world.getBodies(new Array<>())) {
//            BodyState state = new BodyState();
//            state.position = body.getPosition();
//            state.angle = body.getAngle();
//            state.type = body.getType();
//            bodyStates.add(state);
//        }
//
//        FileHandle file = Gdx.files.local("level1.json");
//        file.writeString(json.toJson(bodyStates), false);
//    }
//    // Helper class to store body state
//    static class BodyState {
//        public Vector2 position;
//        public float angle;
//        public BodyDef.BodyType type;
//    }
//
//    private void loadWorldState() {
//        FileHandle file = Gdx.files.local("level1.json");
//        if (file.exists() && file.length() > 0) {
//            Json json = new Json();
//            Array<BodyState> bodyStates = json.fromJson(Array.class, BodyState.class, file);
//
//            for (BodyState state : bodyStates) {
//                BodyDef bodyDef = new BodyDef();
//                bodyDef.position.set(state.position);
//                bodyDef.angle = state.angle;
//                bodyDef.type = state.type;
//
//                Body body = world.createBody(bodyDef);
//
//                PolygonShape shape = new PolygonShape();
//                shape.setAsBox(0.5f, 0.5f); // Adjust size based on your objects
//                body.createFixture(shape, 1f);
//                shape.dispose();
//            }
//        }
//    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        batch.draw(
            background,
            camera.position.x - camera.viewportWidth / 2,
            camera.position.y - camera.viewportHeight / 2,
            camera.viewportWidth,
            camera.viewportHeight);

        float slingshotWidth = (slingTexture.getWidth() / 100f / 30f) * 4;
        float slingshotHeight = (slingTexture.getHeight() / 100f / 30f) * 4;
        batch.draw(
            slingTexture,
            slingBody.getPosition().x - slingshotWidth / 2,
            slingBody.getPosition().y - slingshotHeight / 2,
            slingshotWidth,
            slingshotHeight);

        Vector2 position = redBody.getPosition();
        float angle = redBody.getAngle();
        float bodyWidth = image.getWidth() / 100f / 30f;
        float bodyHeight = image.getHeight() / 100f / 30f;
        batch.draw(
            image,
            position.x - bodyWidth / 2,
            position.y - bodyHeight / 2,
            bodyWidth / 2,
            bodyHeight / 2,
            bodyWidth,
            bodyHeight,
            1f,
            1f,
            (float) Math.toDegrees(angle),
            0,
            0,
            image.getWidth(),
            image.getHeight(),
            false,
            false);

        if (woodboxBody != null && woodboxBody.getUserData() == null) {
            Vector2 woodboxPosition = woodboxBody.getPosition();
            float woodboxAngle = woodboxBody.getAngle();
            float woodboxWidth = woodboxTexture.getWidth() / 100f / 5f;
            float woodboxHeight = woodboxTexture.getHeight() / 100f / 5f;
            batch.draw(
                woodboxTexture,
                woodboxPosition.x - woodboxWidth / 2,
                woodboxPosition.y - woodboxHeight / 2,
                woodboxWidth / 2,
                woodboxHeight / 2,
                woodboxWidth,
                woodboxHeight,
                1f,
                1f,
                (float) Math.toDegrees(woodboxAngle),
                0,
                0,
                woodboxTexture.getWidth(),
                woodboxTexture.getHeight(),
                false,
                false);
        }
        if (pigBody != null && pigBody.getUserData() == null) {
            Vector2 pigPosition = pigBody.getPosition();
            float pigAngle = pigBody.getAngle();
            float pigWidth = pigTexture.getWidth() / 100f / 30f;
            float pigHeight = pigTexture.getHeight() / 100f / 30f;
            batch.draw(
                pigTexture,
                pigPosition.x - pigWidth / 2,
                pigPosition.y - pigHeight / 2,
                pigWidth / 2,
                pigHeight / 2,
                pigWidth,
                pigHeight,
                1f,
                1f,
                (float) Math.toDegrees(pigAngle),
                0,
                0,
                pigTexture.getWidth(),
                pigTexture.getHeight(),
                false,
                false);
        }

        batch.draw(
            pauseButtonTexture,
            pauseButtonBody.getPosition().x - 0.5f,
            pauseButtonBody.getPosition().y,
            0.5f,
            0.5f);

        batch.end();

        if (pigBody != null && "destroy".equals(pigBody.getUserData())) {
            world.destroyBody(pigBody);
            pigBody = null;
            game.setScreen(new GameOver(game, 1));
        }

        if (woodboxBody != null && "destroy".equals(woodboxBody.getUserData())) {
            world.destroyBody(woodboxBody);
            woodboxBody = null;
        }

        world.step(1 / 60f, 6, 2);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        world.dispose();
        debugRenderer.dispose();
        batch.dispose();
        image.dispose();
        background.dispose();
        slingTexture.dispose();
        pigTexture.dispose();
        woodstickhorizontalTexture.dispose();
        woodboxTexture.dispose();
        woodtriangleTexture.dispose();
        pauseButtonTexture.dispose();
    }

    @Override
    public void show() {
//        loadWorldState(worldStateFile);
    }

    public InputProcessor getInputProcessor() {
        return Gdx.input.getInputProcessor();
    }
}
