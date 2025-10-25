package caliniya.armadax.system.render;

import caliniya.armadax.system.render.*;
import caliniya.armadax.system.render.data.GameRenderData;
import caliniya.armadax.system.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;

public class GameRender extends BasicSystem {
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer; // 可选：用于调试绘制
    
    // 渲染处理器和渲染数据
    private RenderingProcessor renderingProcessor;
    private GameRenderData renderData;
    
    // 临时实体列表（等物理世界完成后替换）
    private Array<Object> temporaryEntities;
    
    public GameRender() {
        super(16); // 高优先级，确保最后渲染
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        
        // 初始化渲染数据和处理系统
        renderData = GameRenderData.instance;
        renderingProcessor = new RenderingProcessor(renderData);
        
        // 设置相机视野（根据你的游戏窗口大小调整）
        renderingProcessor.setViewport(1280, 720);
        renderingProcessor.setPosition(0, 0);
        renderingProcessor.setZoom(1f);
        
        // 初始化临时实体列表
        temporaryEntities = new Array<>();
        createTestEntities();
    }
    
    /**
     * 创建测试实体（临时使用）
     */
    private void createTestEntities() {
        // 添加一些测试实体
        for (int i = 0; i < 20; i++) {
            temporaryEntities.add(new Object()); // 这里用Object代替实体
        }
    }
    
    @Override
    protected void update() {
        // 清屏操作应该在主游戏循环中完成，这里只负责渲染
        
        // 1. 处理渲染数据（计算视野内物体）
        processRendering();
        
        // 2. 执行批量渲染
        renderBatch();
        
        // 3. 可选：调试绘制（视野边界等）
        renderDebug();
    }
    
    /**
     * 处理渲染数据
     */
    private void processRendering() {
        // 更新相机位置（这里可以添加相机跟随逻辑）
        // renderingProcessor.setPosition(playerX, playerY);
        
        // 处理渲染数据
        renderingProcessor.process(/* temporaryEntities */);
    }
    
    /**
     * 执行批量渲染
     */
    private void renderBatch() {
        batch.begin();
        
        final int count = renderData.getCount();
        for (int i = 0; i < count; i++) {
            // 获取渲染数据
            var region = renderData.getRegion(i);
            float x = renderData.getX(i);
            float y = renderData.getY(i);
            float width = renderData.getWidth(i);
            float height = renderData.getHeight(i);
            float originX = renderData.getOriginX(i);
            float originY = renderData.getOriginY(i);
            float scaleX = renderData.getScaleX(i);
            float scaleY = renderData.getScaleY(i);
            float rotation = renderData.getRotation(i);
            var color = renderData.getColor(i);
            
            // 设置颜色
            batch.setColor(color);
            
            // 如果有纹理就绘制纹理，否则绘制占位矩形
            if (region != null) {
                batch.draw(region,
                    x, y,
                    originX, originY,
                    width, height,
                    scaleX, scaleY,
                    rotation
                );
            } else {
                // 绘制占位矩形（调试用）
                batch.end(); // 结束纹理批次
                
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(color);
                shapeRenderer.rect(x, y, width, height);
                shapeRenderer.end();
                
                batch.begin(); // 重新开始纹理批次
            }
            
            // 重置颜色
            batch.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        }
        
        batch.end();
    }
    
    /**
     * 调试绘制
     */
    private void renderDebug() {
        // 绘制相机视野边界
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(com.badlogic.gdx.graphics.Color.RED);
        
        var viewBounds = renderingProcessor.getViewBounds();
        shapeRenderer.rect(viewBounds.x, viewBounds.y, viewBounds.width, viewBounds.height);
        
        // 绘制相机中心点
        shapeRenderer.setColor(com.badlogic.gdx.graphics.Color.GREEN);
        var camPos = renderingProcessor.getPosition();
        shapeRenderer.circle(camPos.x, camPos.y, 0.5f);
        
        shapeRenderer.end();
        
        // 绘制渲染统计信息
        drawDebugText();
    }
    
    /**
     * 绘制调试文本信息
     */
    private void drawDebugText() {
        // 如果你有BitmapFont，可以在这里绘制调试信息
        /*
        batch.begin();
        font.draw(batch, "Rendered: " + renderData.getCount(), 10, 700);
        font.draw(batch, "Camera: " + renderingProcessor.getPosition(), 10, 680);
        font.draw(batch, "Zoom: " + renderingProcessor.getZoom(), 10, 660);
        batch.end();
        */
    }
    
    /**
     * 获取渲染处理器，用于外部控制相机
     */
    public RenderingProcessor getRenderingProcessor() {
        return renderingProcessor;
    }
    
    /**
     * 获取SpriteBatch，用于其他系统绘制UI等
     */
    public SpriteBatch getBatch() {
        return batch;
    }
    
    @Override
    protected boolean requiresMainThread() {
        return true; // 渲染必须在主线程
    }
}