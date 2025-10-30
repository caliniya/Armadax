package caliniya.armadax.screen;

import caliniya.armadax.base.text.Text;
import caliniya.armadax.base.ui.Button;
import caliniya.armadax.system.render.GameRender;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class MainGameSrceen implements Screen {
    private Stage uistage;
    private ShapeRenderer shapeRenderer;
    private Text text;
    
    // 添加渲染系统
    private GameRender gameRender;

    @Override
    public void render(float delta) {
        
        // 清屏
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // 先更新和绘制UI
        uistage.act(delta);
        uistage.draw();
        
        // 渲染系统
        GameRenderSystem();
    }
    
    /**
     * 测试游戏渲染系统
     */
    private void GameRenderSystem() {
        if (gameRender == null) {
            
        }else{
            gameRender.start();
            gameRender.updateManual();
        }
        
    }
    
    @Override
    public void show() {
        shapeRenderer = new ShapeRenderer();
        text = Text.getInstance();
        
        // 初始化游戏渲染
            gameRender = GameRender.getInstance();
            // 设置相机初始位置
            gameRender.getRenderingProcessor().setPosition(0, 0);
            gameRender.getRenderingProcessor().setViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            gameRender.getRenderingProcessor().setZoom(1f);
        
    }
    
    @Override
    public void resize(int width, int height) {
        uistage.getViewport().update(width, height, true);
        
        // 更新渲染系统的相机视野
        if (gameRender != null) {
            gameRender.getRenderingProcessor().setViewport(width, height);
        }
    }
    
    @Override
    public void dispose() {
        uistage.dispose();
        shapeRenderer.dispose();
        
        // 释放渲染系统资源
        if (gameRender != null) {
        }
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