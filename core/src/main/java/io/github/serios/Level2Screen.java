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
import com.badlogic.gdx.utils.viewport.Viewport;import com.badlogic.gdx.utils.Timer;


public class Level2Screen implements Screen {
    private World world;
    private final Main game;
    private Box2DDebugRenderer debugRenderer;
    private OrthographicCamera camera;
    private Viewport viewport;
    private SpriteBatch batch;
    private Texture image;
    private Texture background;
    private Body chuckBody, pigBody, pigBody2;
    private Body groundBody;
    private MouseJoint mouseJoint, mouseJoint2;
    private MouseJointDef mouseJointDef, mouseJointDef2;
    private Body dummyBody, dummyBody2;
    private Body slingBody, woodboxBody, woodboxBody1, woodboxBody2;
    private Texture slingTexture, pigTexture;
    private Texture woodstickhorizontalTexture, woodboxTexture, woodtriangleTexture;
    private boolean chuckBodyLaunched = false, redBodyLaunched = false;
    private Body redBody;
    private Texture redBodyTexture;
    private boolean redBodyCreated = false;
    private Texture pauseButtonTexture;
    private Body pauseButtonBody;

    public Level2Screen(Main game) {
        this.game = game;
        camera = new OrthographicCamera();
        viewport = new FitViewport(800 / 100f, 480 / 100f, camera);
        camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);
        camera.update();

        batch = new SpriteBatch();
        background = new Texture("levelBackground.jpg");
        image = new Texture("chuck.png");
        pauseButtonTexture = new Texture("pausebutton.png");
        slingTexture = new Texture("slingshot.png");
        pigTexture = new Texture("pig.png");
        woodstickhorizontalTexture = new Texture("woodstickhorizontal.png");
        woodboxTexture = new Texture("woodbox.png");
        woodtriangleTexture = new Texture("woodtriangle.png");
        redBodyTexture = new Texture("red.png");

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

        BodyDef dummyBodyDef = new BodyDef();
        dummyBodyDef.type = BodyDef.BodyType.StaticBody;
        dummyBody2 = world.createBody(dummyBodyDef);

        // Set ContactListener
        world.setContactListener(
            new ContactListener() {
                @Override
                public void beginContact(Contact contact) {
                    Fixture fixtureA = contact.getFixtureA();
                    Fixture fixtureB = contact.getFixtureB();

                    // Check if one fixture is from chuckBody and the other from pigBody
                    if ((fixtureA.getBody() == chuckBody && fixtureB.getBody() == pigBody)
                        || (fixtureA.getBody() == pigBody && fixtureB.getBody() == chuckBody)) {
                        // Flag pigBody for removal
                        pigBody.setUserData("destroy");
                    }

                    // Check if one fixture is from chuckBody and the other from woodboxBody
                    if ((fixtureA.getBody() == chuckBody && fixtureB.getBody() == woodboxBody)
                        || (fixtureA.getBody() == woodboxBody && fixtureB.getBody() == chuckBody)) {
                        // Flag woodboxBody for removal
                        woodboxBody.setUserData("destroy");
                    }

                    // Check if one fixture is from chuckBody and the other from woodboxBody1
                    if ((fixtureA.getBody() == chuckBody && fixtureB.getBody() == woodboxBody1)
                        || (fixtureA.getBody() == woodboxBody1 && fixtureB.getBody() == chuckBody)) {
                        // Flag woodboxBody1 for removal
                        woodboxBody1.setUserData("destroy");
                    }

                    // Check if one fixture is from chuckBody and the other from woodboxBody2
                    if ((fixtureA.getBody() == chuckBody && fixtureB.getBody() == woodboxBody2)
                        || (fixtureA.getBody() == woodboxBody2 && fixtureB.getBody() == chuckBody)) {
                        // Flag woodboxBody2 for removal
                        woodboxBody2.setUserData("destroy");
                    }

                    // Check if one fixture is from pigBody and the other from groundBody
                    if ((fixtureA.getBody() == pigBody && fixtureB.getBody() == groundBody)
                        || (fixtureA.getBody() == groundBody && fixtureB.getBody() == pigBody)) {
                        // Flag pigBody for removal
                        pigBody.setUserData("destroy");
                    }
                    // Check if one fixture is from chuckBody and the other from pigBody2
                    if ((fixtureA.getBody() == chuckBody && fixtureB.getBody() == pigBody2)
                        || (fixtureA.getBody() == pigBody2 && fixtureB.getBody() == chuckBody)) {
                        // Flag pigBody2 for removal
                        pigBody2.setUserData("destroy");
                    }

                    // Check if one fixture is from pigBody2 and the other from groundBody
                    if ((fixtureA.getBody() == pigBody2 && fixtureB.getBody() == groundBody)
                        || (fixtureA.getBody() == groundBody && fixtureB.getBody() == pigBody2)) {
                        // Flag pigBody2 for removal
                        pigBody2.setUserData("destroy");
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

    private void createRedBody() {
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

        redBodyCreated = true;
    }

    private void createFallingBody() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(viewport.getWorldWidth() / 4.5f, 1.6f);
        chuckBody = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        float halfWidth = image.getWidth() / 2f / 100f / 30f;
        float halfHeight = image.getHeight() / 2f / 100f / 30f;
        shape.setAsBox(halfWidth, halfHeight);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1f;
        fixtureDef.friction = 0.5f;
        fixtureDef.restitution = 0.2f;

        chuckBody.createFixture(fixtureDef);
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

        // Create the first wood box
        BodyDef woodboxBodyDef1 = new BodyDef();
        woodboxBodyDef1.type = BodyDef.BodyType.DynamicBody;
        woodboxBodyDef1.position.set(viewport.getWorldWidth() / 1.2f, viewport.getWorldHeight() - 2.5f);
        woodboxBody1 = world.createBody(woodboxBodyDef1);

        PolygonShape woodboxShape1 = new PolygonShape();
        float woodboxHalfWidth1 = woodboxTexture.getWidth() / 2f / 100f / 5f;
        float woodboxHalfHeight1 = woodboxTexture.getHeight() / 2f / 100f / 5f;
        woodboxShape1.setAsBox(woodboxHalfWidth1, woodboxHalfHeight1);

        FixtureDef woodboxFixtureDef1 = new FixtureDef();
        woodboxFixtureDef1.shape = woodboxShape1;
        woodboxFixtureDef1.density = 1f;
        woodboxFixtureDef1.friction = 0.5f;
        woodboxFixtureDef1.restitution = 0.2f;

        woodboxBody1.createFixture(woodboxFixtureDef1);
        woodboxShape1.dispose();

        // Create the second wood box
        BodyDef woodboxBodyDef2 = new BodyDef();
        woodboxBodyDef2.type = BodyDef.BodyType.DynamicBody;
        woodboxBodyDef2.position.set(viewport.getWorldWidth() / 1.2f, viewport.getWorldHeight() - 1.5f);
        woodboxBody2 = world.createBody(woodboxBodyDef2);

        PolygonShape woodboxShape2 = new PolygonShape();
        float woodboxHalfWidth2 = woodboxTexture.getWidth() / 2f / 100f / 5f;
        float woodboxHalfHeight2 = woodboxTexture.getHeight() / 2f / 100f / 5f;
        woodboxShape2.setAsBox(woodboxHalfWidth2, woodboxHalfHeight2);

        FixtureDef woodboxFixtureDef2 = new FixtureDef();
        woodboxFixtureDef2.shape = woodboxShape2;
        woodboxFixtureDef2.density = 1f;
        woodboxFixtureDef2.friction = 0.5f;
        woodboxFixtureDef2.restitution = 0.2f;

        woodboxBody2.createFixture(woodboxFixtureDef2);
        woodboxShape2.dispose();

        // Create the second pig
        BodyDef pigBodyDef2 = new BodyDef();
        pigBodyDef2.type = BodyDef.BodyType.DynamicBody;
        pigBodyDef2.position.set(viewport.getWorldWidth() / 1.2f, viewport.getWorldHeight() - 0.8f);
        pigBody2 = world.createBody(pigBodyDef2);

        PolygonShape pigShape2 = new PolygonShape();
        float pigHalfWidth2 = pigTexture.getWidth() / 2f / 100f / 30f;
        float pigHalfHeight2 = pigTexture.getHeight() / 2f / 100f / 30f;
        pigShape2.setAsBox(pigHalfWidth2, pigHalfHeight2);

        FixtureDef pigFixtureDef2 = new FixtureDef();
        pigFixtureDef2.shape = pigShape2;
        pigFixtureDef2.density = 1f;
        pigFixtureDef2.friction = 0.5f;
        pigFixtureDef2.restitution = 0.1f;

        pigBody2.createFixture(pigFixtureDef2);
        pigShape2.dispose();
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
//
//    PolygonShape slingshotShape = new PolygonShape();
//    float slingshotWidth = (slingTexture.getWidth() / 100f / 30f) * 0.5f;
//    float slingshotHeight = (slingTexture.getHeight() / 100f / 30f) * 0.8f;
//    slingshotShape.setAsBox(slingshotWidth / 2, slingshotHeight / 2);
//
//    FixtureDef fixtureDef = new FixtureDef();
//    fixtureDef.shape = slingshotShape;
//
//    slingBody.createFixture(fixtureDef);
//    slingshotShape.dispose();
    }

    private Vector2 dragStart = new Vector2(); // To store the drag start position

    private void setupInputProcessor() {
        Gdx.input.setInputProcessor(
            new InputAdapter() {
                @Override
                public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                    Vector2 worldCoordinates = null;
                    worldCoordinates = viewport.unproject(new Vector2(screenX, screenY));
                    if (!chuckBodyLaunched) {
                        chuckBody.setType(BodyDef.BodyType.DynamicBody);
                        if (chuckBody.getFixtureList().first().testPoint(worldCoordinates)) {
                            if (mouseJoint == null) {
                                mouseJointDef = new MouseJointDef();
                                mouseJointDef.bodyA = dummyBody;
                                mouseJointDef.bodyB = chuckBody;
                                mouseJointDef.collideConnected = true;
                                mouseJointDef.target.set(worldCoordinates);
                                mouseJointDef.maxForce = 1000.0f * chuckBody.getMass();
                                mouseJoint = (MouseJoint) world.createJoint(mouseJointDef);
                                dragStart.set(worldCoordinates); // Store the start position
                            }
                            return true;
                        }
                    } else if (redBodyCreated && !redBodyLaunched) {
                        redBody.setType(BodyDef.BodyType.DynamicBody);
                       if (redBody.getFixtureList().first().testPoint(worldCoordinates)) {
                         if (mouseJoint2 == null) {
                           mouseJointDef2 = new MouseJointDef();
                           mouseJointDef2.bodyA = dummyBody2;
                           mouseJointDef2.bodyB = redBody;
                           mouseJointDef2.collideConnected = true;
                           mouseJointDef2.target.set(worldCoordinates);
                           mouseJointDef2.maxForce = 1000.0f * redBody.getMass();
                           mouseJoint2 = (MouseJoint) world.createJoint(mouseJointDef2);
                           dragStart.set(worldCoordinates); // Store the start position
                         }
                         return true;
                       }
                     }

                    if (isTouchingPauseButton(worldCoordinates)) {
                        game.setScreen(new PauseScreen(game, 2));
                        return true;
                    }
                    return false;
                }

                @Override
                public boolean keyDown(int keycode) {
                    if (keycode == Input.Keys.ESCAPE) { // Check if the "P" key is pressed

                        game.setScreen(new PauseScreen(game, 2)); // Switch to PauseScreen
                        return true; // Prevent further processing of the key
                    }
                    return false;
                }

                @Override
                public boolean touchDragged(int screenX, int screenY, int pointer) {
                    if (mouseJoint != null) {
                        Vector2 worldCoordinates = viewport.unproject(new Vector2(screenX, screenY));
                        mouseJoint.setTarget(worldCoordinates);
                    } else if (mouseJoint2 != null){
                        Vector2 worldCoordinates2 = viewport.unproject(new Vector2(screenX, screenY));
                        mouseJoint2.setTarget(worldCoordinates2);
                    }
                    return true;
                }

                @Override
                public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                    if (mouseJoint != null && chuckBody != null) {
                        // Calculate release velocity
                        Vector2 releasePosition = viewport.unproject(new Vector2(screenX, screenY));
                        Vector2 dragDirection =
                            dragStart.sub(releasePosition); // Opposite direction of the drag
                        float velocityMultiplier = 5f; // Adjust this for speed control
                        Vector2 releaseVelocity = dragDirection.scl(velocityMultiplier);

                        // Apply the velocity to the chuckBody
                        chuckBody.setLinearVelocity(releaseVelocity);

                        // Destroy the mouse joint
                        world.destroyJoint(mouseJoint);
                        mouseJoint = null;

                        chuckBodyLaunched = true;
                    }
                             else if (mouseJoint2 != null && redBody != null) {
                       // Calculate release velocity
                       Vector2 releasePosition2 = viewport.unproject(new Vector2(screenX, screenY));
                       Vector2 dragDirection2 =
                           dragStart.sub(releasePosition2); // Opposite direction of the drag
                       float velocityMultiplier2 = 5f; // Adjust this for speed control
                       Vector2 releaseVelocity2 = dragDirection2.scl(velocityMultiplier2);

                       // Apply the velocity to the redBody
                       redBody.setLinearVelocity(releaseVelocity2);

                       // Destroy the mouse joint
                       world.destroyJoint(mouseJoint2);
                       mouseJoint2 = null;

                       redBodyLaunched = true;
                     }
                    return true;
                }
            });
    }

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

        Vector2 position = chuckBody.getPosition();
        float angle = chuckBody.getAngle();
        float bodyWidth = (image.getWidth() / 100f / 30f) * 0.7f;
        float bodyHeight = (image.getHeight() / 100f / 30f) * 0.7f;
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
        if (woodboxBody1 != null && woodboxBody1.getUserData() == null) {
            Vector2 woodboxPosition1 = woodboxBody1.getPosition();
            float woodboxAngle1 = woodboxBody1.getAngle();
            float woodboxWidth1 = woodboxTexture.getWidth() / 100f / 5f;
            float woodboxHeight1 = woodboxTexture.getHeight() / 100f / 5f;
            batch.draw(
                woodboxTexture,
                woodboxPosition1.x - woodboxWidth1 / 2,
                woodboxPosition1.y - woodboxHeight1 / 2,
                woodboxWidth1 / 2,
                woodboxHeight1 / 2,
                woodboxWidth1,
                woodboxHeight1,
                1f,
                1f,
                (float) Math.toDegrees(woodboxAngle1),
                0,
                0,
                woodboxTexture.getWidth(),
                woodboxTexture.getHeight(),
                false,
                false);
        }

        if (woodboxBody2 != null && woodboxBody2.getUserData() == null) {
            Vector2 woodboxPosition2 = woodboxBody2.getPosition();
            float woodboxAngle2 = woodboxBody2.getAngle();
            float woodboxWidth2 = woodboxTexture.getWidth() / 100f / 5f;
            float woodboxHeight2 = woodboxTexture.getHeight() / 100f / 5f;
            batch.draw(
                woodboxTexture,
                woodboxPosition2.x - woodboxWidth2 / 2,
                woodboxPosition2.y - woodboxHeight2 / 2,
                woodboxWidth2 / 2,
                woodboxHeight2 / 2,
                woodboxWidth2,
                woodboxHeight2,
                1f,
                1f,
                (float) Math.toDegrees(woodboxAngle2),
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
        if (pigBody2 != null && pigBody2.getUserData() == null) {
            Vector2 pigPosition2 = pigBody2.getPosition();
            float pigAngle2 = pigBody2.getAngle();
            float pigWidth2 = pigTexture.getWidth() / 100f / 30f;
            float pigHeight2 = pigTexture.getHeight() / 100f / 30f;
            batch.draw(
                pigTexture,
                pigPosition2.x - pigWidth2 / 2,
                pigPosition2.y - pigHeight2 / 2,
                pigWidth2 / 2,
                pigHeight2 / 2,
                pigWidth2,
                pigHeight2,
                1f,
                1f,
                (float) Math.toDegrees(pigAngle2),
                0,
                0,
                pigTexture.getWidth(),
                pigTexture.getHeight(),
                false,
                false);
        }

        if (chuckBodyLaunched && !redBodyCreated) {

            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    createRedBody(); // Call createRedBody() after the delay
                }
            }, 1.5f);
        }

        // Render redBody if created
        if (redBodyCreated) {
            Vector2 position2 = redBody.getPosition();
            float angle2 = redBody.getAngle();
            float bodyWidth2 = (redBodyTexture.getWidth() / 100f / 30f);
            float bodyHeight2 = (redBodyTexture.getHeight() / 100f / 30f);
            batch.draw(
                redBodyTexture,
                position2.x - bodyWidth2 / 2,
                position2.y - bodyHeight2 / 2,
                bodyWidth2 / 2,
                bodyHeight2 / 2,
                bodyWidth2,
                bodyHeight2,
                1f,
                1f,
                (float) Math.toDegrees(angle2),
                0,
                0,
                redBodyTexture.getWidth(),
                redBodyTexture.getHeight(),
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
        }

        if (woodboxBody != null && "destroy".equals(woodboxBody.getUserData())) {
            world.destroyBody(woodboxBody);
            woodboxBody = null;
        }

        if (pigBody2 != null && "destroy".equals(pigBody2.getUserData())) {
            world.destroyBody(pigBody2);
            pigBody2 = null;
        }

        if (woodboxBody1 != null && "destroy".equals(woodboxBody1.getUserData())) {
            world.destroyBody(woodboxBody1);
            woodboxBody1 = null;
        }

        if (woodboxBody2 != null && "destroy".equals(woodboxBody2.getUserData())) {
            world.destroyBody(woodboxBody2);
            woodboxBody2 = null;
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
        redBodyTexture.dispose();
    }

    @Override
    public void show() {}

    public InputProcessor getInputProcessor() {
        return Gdx.input.getInputProcessor();
    }
}
