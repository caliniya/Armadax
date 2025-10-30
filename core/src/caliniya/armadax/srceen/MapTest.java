package caliniya.armadax.screen;

import caliniya.armadax.system.render.GameRender;
import caliniya.armadax.world.World;
import com.badlogic.gdx.Screen;

public class MapTest implements Screen {
    
    public static GameRender gamerender;
    public World world;
    
    @Override
    public void show() {
        world = new World(200 , 200);
        gamerender = GameRender.getInstance();
        gamerender.start();
        
    }
    @Override
    public void render(float ar) {
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