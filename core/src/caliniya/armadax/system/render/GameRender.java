package caliniya.armadax.system.render;

import caliniya.armadax.base.text.Text;
import caliniya.armadax.system.render.*;
import caliniya.armadax.system.render.data.GameRenderData;
import caliniya.armadax.system.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;

public class GameRender extends BasicSystem {
    private static GameRender instance;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer; // 用于几何图形和调试绘制
    
    // 渲染处理器和渲染数据
    private RenderingProcessor renderingProcessor;
    private GameRenderData renderData;
    
    static{
        instance = new GameRender();
    }
    
    public static GameRender getInstance(){
        return instance;
    }
    
    protected GameRender() {
        super(1); //在当前阶段，以尽可能快的速度渲染
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        
        // 初始化渲染数据和处理系统
        renderData = GameRenderData.instance;
        renderingProcessor = new RenderingProcessor(renderData);
        
        // 设置相机视野（根据游戏窗口大小调整）
        renderingProcessor.setViewport(1280, 720);
        renderingProcessor.setPosition(0, 0);
        renderingProcessor.setZoom(1f);
    }
    
    @Override//重写系统的更新方法
    protected void update() {
        // 清屏操作应该在主游戏循环中完成，这里只负责渲染
        // 1. 处理渲染数据（计算视野内物体）
        processRendering();
        // 2. 执行批量渲染
        renderBatch();
        // 3. 调试绘制（视野边界等）
        renderDebug();
    }
    
    /**
     * 处理渲染数据
     */
    private void processRendering() {
        // 更新相机位置（这里可以添加相机跟随逻辑）
        // renderingProcessor.setPosition(playerX, playerY);
        
        // 处理渲染数据
        renderingProcessor.process();
    }
    
    /**
     * 执行批量渲染
     */
    private void renderBatch() {
        // 先渲染所有带纹理的物体
        renderTexturedItems();
        // 再渲染所有几何图形
        renderGeometryItems();
    }
    
    /**
     * 渲染带纹理的物体
     */
    private void renderTexturedItems() {
        batch.begin();
        
        final int count = renderData.getCount();
        
        for (int i = 0; i < count; i++) {
            var region = renderData.getRegion(i);
            if (region != null) {
                batch.setColor(renderData.getColor(i));
                batch.draw(region,
                    renderData.getX(i), renderData.getY(i),
                    renderData.getOriginX(i), renderData.getOriginY(i),
                    renderData.getWidth(i), renderData.getHeight(i),
                    renderData.getScaleX(i), renderData.getScaleY(i),
                    renderData.getRotation(i)
                );
            }
        }
        
        // 重置颜色
        batch.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        batch.end();
    }
    
    /**
     * 渲染几何图形
     */
    private void renderGeometryItems() {
        final int count = renderData.getCount();
        boolean hasGeometryItems = false;
        
        // 检查是否有几何图形需要渲染
        if (countGeometryItems() != 0) {
            hasGeometryItems = true;
        // 批量渲染所有填充的几何图形
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        for (int i = 0; i < count; i++) {
            var region = renderData.getRegion(i);
            if (region == null) {
                shapeRenderer.setColor(renderData.getColor(i));
                shapeRenderer.rect(
                    renderData.getX(i), renderData.getY(i),
                    renderData.getWidth(i), renderData.getHeight(i)
                    );
                }
            }
        }
        shapeRenderer.end();
    }
    /**
     * 计算几何图形数量
     */
    private int countGeometryItems() {
        int count = 0;
        final int total = renderData.getCount();
        for (int i = 0; i < total; i++) {
            if (renderData.getRegion(i) == null) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * 调试绘制
     */
    private void renderDebug() {
        // 使用单独的ShapeRenderer绘制调试信息
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.end();
        
        // 绘制渲染统计信息
        drawDebugText();
    }
    
    /**
     * 绘制调试文本信息
     */
    private void drawDebugText() {
        
        // 如果需要绘制文本，可以在这里实现
        batch.begin();
        batch.end();
        
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
    
    /**
     * 获取ShapeRenderer，用于其他系统绘制几何图形
     */
    public ShapeRenderer getShapeRenderer() {
        return shapeRenderer;
    }
    
    @Override
    protected boolean requiresMainThread() {
        return true; // 渲染必须在主线程
    }
    
}