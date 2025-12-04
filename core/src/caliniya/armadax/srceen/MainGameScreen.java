package caliniya.armadax.screen;

import caliniya.armadax.base.system.*;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class MainGameScreen implements Screen {
    private Stage uistage;
    private Text text;

    @Override
    public void render(float delta) {
        uistage.act(delta);
        uistage.draw();
        
    }
    
    @Override
    public void show() {
        text = Text.getInstance();   
    }
    
    @Override
    public void resize(int width, int height) {
        uistage.getViewport().update(width, height, true);
    }
    
    @Override
    public void dispose() {
        uistage.dispose();
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