package io.github.serios;

<<<<<<< HEAD
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private Texture image;
=======
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
>>>>>>> f885817 (everything)

    @Override
    public void create() {
        batch = new SpriteBatch();
<<<<<<< HEAD
        image = new Texture("libgdx.png");
=======
        DisplayMode displayMode = Gdx.graphics.getDisplayMode();

        Gdx.graphics.setFullscreenMode(displayMode);
        this.setScreen(new MenuScreen(this));
>>>>>>> f885817 (everything)
    }

    @Override
    public void render() {
<<<<<<< HEAD
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        batch.begin();
        batch.draw(image, 140, 210);
        batch.end();
=======
        super.render();
>>>>>>> f885817 (everything)
    }

    @Override
    public void dispose() {
        batch.dispose();
<<<<<<< HEAD
        image.dispose();
=======
>>>>>>> f885817 (everything)
    }
}
