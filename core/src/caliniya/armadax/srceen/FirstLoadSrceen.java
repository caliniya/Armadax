package caliniya.armadax.srceen;

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
import caliniya.armadax.base.text.Text;

public class FirstLoadSrceen implements Screen {
    private Stage uistage;
    private ShapeRenderer shapeRenderer;
    private Table rootTable;
    private Text textRenderer;
    private Button centerButton;
    private static final int BUTTON_WIDTH = 300;
    private static final int BUTTON_HEIGHT = 500;
    
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
        
        // 然后绘制文本元素
        drawTextElements();
        
        // 最后绘制验证框（在UI和文本之上）
        drawValidationBoxes();
        
        // 测试渲染系统 - 在UI之上绘制
        testGameRenderSystem();
    }
    
    /**
     * 测试游戏渲染系统
     */
    private void testGameRenderSystem() {
        if (gameRender != null) {
            // 更新渲染系统（这会处理渲染数据并绘制）
            
            // 可以添加一些简单的相机移动来测试
            moveCameraForTest();
        }
    }
    
    /**
     * 简单的相机移动测试
     */
    private void moveCameraForTest() {
        if (gameRender != null) {
            // 让相机缓慢移动，测试渲染效果
            float time = Gdx.graphics.getFrameId() * 0.01f;
            float moveX = (float) Math.sin(time) * 2f;
            float moveY = (float) Math.cos(time) * 1.5f;
            
            gameRender.getRenderingProcessor().move(moveX * Gdx.graphics.getDeltaTime(), 
                                                   moveY * Gdx.graphics.getDeltaTime());
        }
    }
    
    private void drawTextElements() {
        textRenderer.begin();
        
        textRenderer.draw("Top Left", 60, Gdx.graphics.getHeight() - 120);
        textRenderer.draw("Top Right", Gdx.graphics.getWidth() - 140, Gdx.graphics.getHeight() - 120);
        textRenderer.draw("Bottom Left", 60, 80);
        textRenderer.draw("Bottom Right", Gdx.graphics.getWidth() - 140, 80);
        
        // 中心文本
        textRenderer.draw("Center", 
                         Gdx.graphics.getWidth()/2 - textRenderer.getWidth("Center")/2, 
                         Gdx.graphics.getHeight()/2 - textRenderer.getHeight()/2, 
                         Color.YELLOW);
        
        // 添加渲染系统状态信息
        if (gameRender != null) {
            String renderInfo = "Rendering System: ACTIVE";
            textRenderer.draw(renderInfo, 20, 40, Color.GREEN);
            
            // 显示渲染的物体数量
            String countInfo = "Rendered Objects: " + gameRender.getRenderingProcessor().getRenderCount();
            textRenderer.draw(countInfo, 20, 20, Color.CYAN);
        } else {
            textRenderer.draw("Rendering System: NOT INITIALIZED", 20, 40, Color.RED);
        }
        
        textRenderer.end();
    }
    
    private void drawValidationBoxes() {
        shapeRenderer.setProjectionMatrix(uistage.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.RED);
        
        shapeRenderer.rect(50, Gdx.graphics.getHeight() - 150, 100, 100);
        shapeRenderer.rect(Gdx.graphics.getWidth() - 150, Gdx.graphics.getHeight() - 150, 100, 100);
        shapeRenderer.rect(50, 50, 100, 100);
        shapeRenderer.rect(Gdx.graphics.getWidth() - 150, 50, 100, 100);
        
        shapeRenderer.end();
    }
    
    @Override
    public void show() {
        uistage = new Stage(new ScreenViewport());
        shapeRenderer = new ShapeRenderer();
        textRenderer = Text.getInstance();
        
        // 初始化游戏渲染系统
        try {
            gameRender = new GameRender();
            // 设置相机初始位置
            gameRender.getRenderingProcessor().setPosition(0, 0);
            gameRender.getRenderingProcessor().setViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            gameRender.getRenderingProcessor().setZoom(1f);
            
            System.out.println("GameRender system initialized successfully");
        } catch (Exception e) {
            System.err.println("Failed to initialize GameRender: " + e.getMessage());
            e.printStackTrace();
            gameRender = null;
        }
        
        rootTable = new Table();
        rootTable.setFillParent(true);
        uistage.addActor(rootTable);
        
        // 创建中心按钮
        centerButton = new Button();
        centerButton.setSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        centerButton.setPosition(
            Gdx.graphics.getWidth()/2 - BUTTON_WIDTH/2, 
            Gdx.graphics.getHeight()/2 - BUTTON_HEIGHT/2
        );
        uistage.addActor(centerButton);
        
        Gdx.input.setInputProcessor(uistage);
    }
    
    @Override
    public void resize(int width, int height) {
        uistage.getViewport().update(width, height, true);
        
        // 更新按钮位置
        if (centerButton != null) {
            centerButton.setPosition(
                width/2 - BUTTON_WIDTH/2, 
                height/2 - BUTTON_HEIGHT/2
            );
        }
        
        // 更新文本渲染器
        if (textRenderer != null) {
            textRenderer.setProjectionMatrix(uistage.getCamera().combined);
        }
        
        // 更新渲染系统的相机视野
        if (gameRender != null) {
            gameRender.getRenderingProcessor().setViewport(width, height);
        }
    }
    
    @Override
    public void dispose() {
        uistage.dispose();
        shapeRenderer.dispose();
        
        // 释放按钮资源
        if (centerButton != null) {
            centerButton.dispose();
        }
        
        // 释放渲染系统资源
        if (gameRender != null) {
        }
    }
    
    @Override 
    public void hide() {
        System.out.println("FirstLoadScreen hidden");
    }
    
    @Override 
    public void resume() {
        System.out.println("FirstLoadScreen resumed");
    }
    
    @Override 
    public void pause() {
        System.out.println("FirstLoadScreen paused");
    }
}