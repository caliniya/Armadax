package caliniya.armadax.screen;

import caliniya.armadax.system.render.GameRender;
import caliniya.armadax.world.*;
import com.badlogic.gdx.Screen;

public class MapTest implements Screen {
    
    public static GameRender gamerender;
    
    @Override
    public void show() {
        gamerender = GameRender.getInstance();
        gamerender.start();
        
    }
    @Override
    public void render(float delte) {
        gamerender.updateManual();
    }
    
    
    @Override
    public void dispose() {
        // TODO: Implement this method
    }
    
    @Override
    public void hide() {
        // TODO: Implement this method
    }
    
    @Override
    public void resume() {
        // TODO: Implement this method
    }
    
    @Override
    public void pause() {
        // TODO: Implement this method
    }
    
    @Override
    public void resize(int arg0, int arg1) {
        // TODO: Implement this method
    }
    
    
	
}