package io.github.serios;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Main extends Game {
  private SpriteBatch batch;
  private Texture image;
  private OrthographicCamera camera;
  private Viewport viewport;

  @Override
  public void create() {
    batch = new SpriteBatch();
    DisplayMode displayMode = Gdx.graphics.getDisplayMode();

    Gdx.graphics.setFullscreenMode(displayMode);
    this.setScreen(new MenuScreen(this));
  }

  @Override
  public void render() {
    super.render();
  }

  @Override
  public void dispose() {
    batch.dispose();
  }
}
