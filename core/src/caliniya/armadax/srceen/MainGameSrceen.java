package caliniya.armadax.screen;

import caliniya.armadax.base.system.*;
import caliniya.armadax.system.render.GameRender;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class MainGameSrceen implements Screen {
    private Stage uistage;
    private ShapeRenderer shapeRenderer;
    private Text text;

    @Override
    public void render(float delta) {
        uistage.act(delta);
        uistage.draw();
        
    }
    
    @Override
    public void show() {
        shapeRenderer = new ShapeRenderer();
        text = Text.getInstance();   
    }
    
    @Override
    public void resize(int width, int height) {
        uistage.getViewport().update(width, height, true);
    }
    
    @Override
    public void dispose() {
        uistage.dispose();
        shapeRenderer.dispose();
        
    }
    
    @Override 
    public void hide() {
        
    }
    
    @Override 
    public void resume() {
        
    }
    
    @Override 
    public void pause() {
        
    }
}