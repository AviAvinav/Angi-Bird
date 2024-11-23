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

public class MenuScreen implements Screen {
  private Main game;
  private Stage stage;
  private Skin skin;
  private SpriteBatch batch;
  private Texture background;

  public MenuScreen(Main game) {
    this.game = game;
    batch = new SpriteBatch();
    background = new Texture("hero.png");

    stage = new Stage(new ScreenViewport());
    Gdx.input.setInputProcessor(stage);

    skin = new Skin(Gdx.files.internal("uiskin.json"));

    // Create a button
    TextButton button = new TextButton("Start Game", skin);
    button.setPosition(Gdx.graphics.getWidth() / 2f - 100, Gdx.graphics.getHeight() / 2f);
    button.setSize(200, 60);

    button.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            game.setScreen(new GameScreen(game));
          }
        });

    stage.addActor(button);
  }

  @Override
  public void show() {}

  @Override
  public void render(float delta) {
    Gdx.gl.glClearColor(0, 0, 0, 1);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

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

    batch.draw(background, x, y, imageWidth * scale, imageHeight * scale);

    batch.end();

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
