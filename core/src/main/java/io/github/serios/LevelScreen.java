package io.github.serios;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class LevelScreen implements Screen {
  // Basic Box2D world
  private World world;
  private Box2DDebugRenderer debugRenderer;
  private OrthographicCamera camera;
  private Viewport viewport;
  private SpriteBatch batch;
  private Texture image;
  private Main game;
  private Stage stage;
  private Texture background;
  private Skin skin;
  private Body redBody;
  private Body groundBody;

  public LevelScreen(Main game) {
    this.game = game;
    camera = new OrthographicCamera();
    viewport = new FitViewport(800 / 100f, 480 / 100f, camera); // Convert pixels to meters
    camera.position.set(
        viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0); // Center the camera
    camera.update();

    batch = new SpriteBatch();
    background = new Texture("levelBackground.jpg");
    skin = new Skin(Gdx.files.internal("uiskin.json"));
    image = new Texture("red.png");
    world = new World(new Vector2(0, -9.8f), true);
    stage = new Stage(new FitViewport(800, 480));
    Gdx.input.setInputProcessor(stage);

    debugRenderer = new Box2DDebugRenderer();

    createFallingBody();
    createGroundBody();
  }

  private void createFallingBody() {
    // Define the body
    BodyDef bodyDef = new BodyDef();
    bodyDef.type = BodyDef.BodyType.DynamicBody; // Falling due to gravity
    bodyDef.position.set(
        viewport.getWorldWidth() / 2, viewport.getWorldHeight() - 1); // Place above ground

    redBody = world.createBody(bodyDef);

    // Define the shape
    PolygonShape shape = new PolygonShape();
    float halfWidth = image.getWidth() / 2f / 100f; // Convert pixels to meters
    float halfHeight = image.getHeight() / 2f / 100f;
    shape.setAsBox(halfWidth, halfHeight);

    // Define the fixture
    FixtureDef fixtureDef = new FixtureDef();
    fixtureDef.shape = shape;
    fixtureDef.density = 1f;
    fixtureDef.friction = 0.5f;
    fixtureDef.restitution = 0.2f; // Slight bounciness

    redBody.createFixture(fixtureDef);
    shape.dispose();
  }

  private void createGroundBody() {
    // Position the ground at the bottom of the screen
    float groundY = 1f; // Half meter above the bottom in world units

    // Define the body
    BodyDef bodyDef = new BodyDef();
    bodyDef.type = BodyDef.BodyType.StaticBody; // Does not move
    bodyDef.position.set(viewport.getWorldWidth() / 2, groundY);

    groundBody = world.createBody(bodyDef);

    // Define the shape
    EdgeShape groundShape = new EdgeShape();
    groundShape.set(
        -viewport.getWorldWidth() / 2, 0, viewport.getWorldWidth() / 2, 0); // Line across the width

    // Define the fixture
    FixtureDef fixtureDef = new FixtureDef();
    fixtureDef.shape = groundShape;
    fixtureDef.friction = 0.8f;

    groundBody.createFixture(fixtureDef);
    groundShape.dispose();
  }

  @Override
  public void render(float delta) {
    Gdx.gl.glClearColor(0, 0, 0, 1);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    camera.update();
    batch.setProjectionMatrix(camera.combined);

    batch.begin();

    // Draw the background
    batch.draw(
        background,
        camera.position.x - camera.viewportWidth / 2,
        camera.position.y - camera.viewportHeight / 2,
        camera.viewportWidth,
        camera.viewportHeight);

    // Get the red body's position
    Vector2 position = redBody.getPosition();
    float angle = redBody.getAngle();

    // Convert position from meters to pixels and draw the texture to fit the body's size
    float bodyWidth = image.getWidth() / 100f; // Convert pixels to meters
    float bodyHeight = image.getHeight() / 100f;
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

    batch.end();

    debugRenderer.render(world, camera.combined);
    world.step(1 / 60f, 6, 2);
    stage.act(delta);
    stage.draw();
  }

  @Override
  public void resize(int width, int height) {
    viewport.update(width, height);
    camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);
    camera.update();
  }

  @Override
  public void show() {}

  @Override
  public void hide() {}

  @Override
  public void pause() {}

  @Override
  public void resume() {}

  @Override
  public void dispose() {
    batch.dispose();
    image.dispose();
    world.dispose();
    debugRenderer.dispose();
    stage.dispose();
    skin.dispose();
  }
}
