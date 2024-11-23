package io.github.serios;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class GameScreen implements Screen {
  private Main game;
  private Stage stage;
  private Skin skin;
  private SpriteBatch batch;
  private Texture background;

  public GameScreen(Main game) {
    this.game = game;
    batch = new SpriteBatch();
    background = new Texture("hero.png");

    stage = new Stage(new ScreenViewport());
    Gdx.input.setInputProcessor(stage);
    skin = new Skin(Gdx.files.internal("uiskin.json"));
    skin.addRegions(new TextureAtlas(Gdx.files.internal("uiskin.atlas")));
//      BitmapFont font = new BitmapFont(Gdx.files.internal("default.fnt"), Gdx.files.internal("default.png"), false);
//      font.getData().setScale(0.5f);
//      font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
//      skin.add("default-font", font);

    Button backButton = new Button(skin, "backbutton-style");
    backButton.setPosition(20, Gdx.graphics.getHeight() - 100);
    backButton.setSize(80, 80);

    backButton.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            game.setScreen(new MenuScreen(game));
          }
        });

    stage.addActor(backButton);
    int numColumns = 6;
    Table table = new Table();
    table.setFillParent(true);
    stage.addActor(table);

    for (int i = 1; i <= 30; i++) {
      TextButton levelButton = new TextButton("Level " + i, skin, (i == 1 ? "levelbutton-style" : "lockedlevel-style"));
      levelButton.getLabelCell().pad(10);

      final int level = i;
      if (i == 1)
        levelButton.addListener(
            new ClickListener() {
              @Override
              public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new LevelScreen(game));
              }
            });

      table.add(levelButton).width(110).height(110).pad(25);

      if (i % numColumns == 0) {
        table.row();
      }
    }
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
  public void resize(int width, int height) {}

  @Override
  public void pause() {}

  @Override
  public void resume() {}

  @Override
  public void hide() {}

  @Override
  public void dispose() {
    stage.dispose();
    skin.dispose();
  }
}
