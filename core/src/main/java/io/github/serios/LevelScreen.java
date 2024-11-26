package io.github.serios;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
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

public class LevelScreen implements Screen {
  private World world;
  private Box2DDebugRenderer debugRenderer;
  private OrthographicCamera camera;
  private Viewport viewport;
  private SpriteBatch batch;
  private Texture image;
  private Texture background;
  private Body redBody, pigBody;
  private Body groundBody;
  private MouseJoint mouseJoint;
  private MouseJointDef mouseJointDef;
  private Body dummyBody;
  private Body slingBody;
  private Texture slingTexture, pigTexture;
  private Texture woodstickhorizontalTexture, woodboxTexture, woodtriangleTexture;

  public LevelScreen(Main game) {
    camera = new OrthographicCamera();
    viewport = new FitViewport(800 / 100f, 480 / 100f, camera);
    camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);
    camera.update();

    batch = new SpriteBatch();
    background = new Texture("levelBackground.jpg");
    image = new Texture("red.png");
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
          }

          @Override
          public void endContact(Contact contact) {}

          @Override
          public void preSolve(Contact contact, Manifold oldManifold) {}

          @Override
          public void postSolve(Contact contact, ContactImpulse impulse) {}
        });
  }

  private void createFallingBody() {
    BodyDef bodyDef = new BodyDef();
    bodyDef.type = BodyDef.BodyType.DynamicBody;
    bodyDef.position.set(viewport.getWorldWidth() / 4.5f, viewport.getWorldHeight() - 1);
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

    PolygonShape slingshotShape = new PolygonShape();
    float slingshotWidth = (slingTexture.getWidth() / 100f / 30f) * 0.5f;
    float slingshotHeight = (slingTexture.getHeight() / 100f / 30f) * 0.8f;
    slingshotShape.setAsBox(slingshotWidth / 2, slingshotHeight / 2);

    FixtureDef fixtureDef = new FixtureDef();
    fixtureDef.shape = slingshotShape;

    slingBody.createFixture(fixtureDef);
    slingshotShape.dispose();
  }

  private Vector2 dragStart = new Vector2(); // To store the drag start position

  private void setupInputProcessor() {
    Gdx.input.setInputProcessor(
        new InputAdapter() {
          @Override
          public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            Vector2 worldCoordinates = viewport.unproject(new Vector2(screenX, screenY));
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
            return false;
          }

          @Override
          public boolean touchDragged(int screenX, int screenY, int pointer) {
            if (mouseJoint != null) {
              Vector2 worldCoordinates = viewport.unproject(new Vector2(screenX, screenY));
              mouseJoint.setTarget(worldCoordinates);
            }
            return true;
          }

          @Override
          public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            if (mouseJoint != null) {
              // Calculate release velocity
              Vector2 releasePosition = viewport.unproject(new Vector2(screenX, screenY));
              Vector2 dragDirection =
                  dragStart.sub(releasePosition); // Opposite direction of the drag
              float velocityMultiplier = 5f; // Adjust this for speed control
              Vector2 releaseVelocity = dragDirection.scl(velocityMultiplier);

              // Apply the velocity to the redBody
              redBody.setLinearVelocity(releaseVelocity);

              // Destroy the mouse joint
              world.destroyJoint(mouseJoint);
              mouseJoint = null;
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
    batch.end();

    if (pigBody != null && "destroy".equals(pigBody.getUserData())) {
      world.destroyBody(pigBody);
      pigBody = null;
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
  }

  @Override
  public void show() {}
}
