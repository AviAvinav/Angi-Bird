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
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class LevelScreen implements Screen {
    // basic box2d world
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

    public LevelScreen(Main game) {
        this.game = game;
        camera = new OrthographicCamera();
        viewport = new FitViewport(800, 480, camera);
        batch = new SpriteBatch();
        background = new Texture("levelBackground.jpg");
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        image = new Texture("red.png");
        world = new World(new Vector2(0, -9.8f), true);
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        debugRenderer = new Box2DDebugRenderer();

        // Create a dynamic body for the spinning texture
        BodyDef redBodyDef = new BodyDef();
        redBodyDef.type = BodyDef.BodyType.DynamicBody;
        redBodyDef.position.set(0, 0); // Place it at the center of the world

        redBody = world.createBody(redBodyDef);

// Define a shape for the body
        PolygonShape redShape = new PolygonShape();
        redShape.setAsBox(0.5f, 0.5f); // Create a box 1x1 in size

// Attach the shape to the body with a fixture
        redBody.createFixture(redShape, 1.0f);
        redShape.dispose();

// Set angular velocity to make it spin
        redBody.setAngularVelocity(1.0f); // Radians per second

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();

        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();

        int imageWidth = background.getWidth();
        int imageHeight = background.getHeight();

        float scaleX = (float) screenWidth / imageWidth;
        float scaleY = (float) screenHeight / imageHeight;
        float scale = Math.max(scaleX, scaleY);

        float x = (screenWidth - imageWidth * scale) / 2;
        float y = (screenHeight - imageHeight * scale) / 2;

        batch.draw(background,
            camera.position.x - camera.viewportWidth / 2,
            camera.position.y - camera.viewportHeight / 2,
            camera.viewportWidth,
            camera.viewportHeight);

        // Synchronize red.png with the Box2D body
        float x1 = redBody.getPosition().x - 0.5f; // Center the texture on the body
        float y1 = redBody.getPosition().y - 0.5f;
        float rotation = (float) Math.toDegrees(redBody.getAngle()); // Convert radians to degrees

        batch.draw(image, x, y, 0.5f, 0.5f, 5, 5, 5, 5, rotation, 0, 0, image.getWidth(), image.getHeight(), false, false);


        batch.end();

        debugRenderer.render(world, camera.combined);
        world.step(1/60f, 6, 2);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void show() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

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
