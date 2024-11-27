package io.github.serios;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class PauseScreen implements Screen {
  private final Main game;
  private final SpriteBatch batch;
  private final Texture background;
  private final Stage stage;
  private final Skin skin;

  public PauseScreen(Main game, int level) {
    this.game = game;

    // Initialize rendering tools
    batch = new SpriteBatch();
    background = new Texture("pauseBackground.png"); // Add your pause background image

    // Initialize Stage and UI skin
    stage = new Stage(new ScreenViewport());
    Gdx.input.setInputProcessor(stage);
    skin = new Skin(Gdx.files.internal("uiskin.json"));

    // Create the Resume button
    TextButton resumeButton = new TextButton("Resume", skin);
    resumeButton.setSize(200, 60);
    resumeButton.setPosition(
        Gdx.graphics.getWidth() / 2f - 100, Gdx.graphics.getHeight() / 2f + 50);
    resumeButton.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            if (level == 1) game.setScreen(new LevelScreen(game));
            if (level == 2) game.setScreen(new Level2Screen(game)); // Replace with your game screen
          }
        });

    // Create the Exit button
    TextButton exitButton = new TextButton("Exit", skin);
    exitButton.setSize(200, 60);
    exitButton.setPosition(Gdx.graphics.getWidth() / 2f - 100, Gdx.graphics.getHeight() / 2f - 50);
    exitButton.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            // Gdx.app.exit(); // Exit the application (or return to MenuScreen if needed)
            game.setScreen(new MenuScreen(game)); // Uncomment to go back to menu instead of exiting
          }
        });

    // Add buttons to the stage
    stage.addActor(resumeButton);
    stage.addActor(exitButton);
  }

  @Override
  public void show() {}

  @Override
  public void render(float delta) {
    // Clear the screen
    Gdx.gl.glClearColor(0, 0, 0, 1);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    // Render the pause background
    batch.begin();
    batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    batch.end();

    // Draw the stage (buttons)
    stage.act(delta);
    stage.draw();
  }

  @Override
  public void resize(int width, int height) {
    stage.getViewport().update(width, height, true);
  }

  @Override
  public void pause() {}

  @Override
  public void resume() {}

  @Override
  public void hide() {}

  @Override
  public void dispose() {
    batch.dispose();
    background.dispose();
    stage.dispose();
    skin.dispose();
  }
}
