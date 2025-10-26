package caliniya.armadax.system.render;

import caliniya.armadax.system.render.*;
import caliniya.armadax.system.render.data.GameRenderData;
import caliniya.armadax.system.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;

public class GameRender extends BasicSystem {
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer; // 用于几何图形和调试绘制
    
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
     * 执行批量渲染 - 优化版本
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
        boolean hasTexturedItems = false;
        
        for (int i = 0; i < count; i++) {
            var region = renderData.getRegion(i);
            if (region != null) {
                hasTexturedItems = true;
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
        
        // 调试信息：如果有纹理物体被渲染
        if (hasTexturedItems) {
            System.out.println("渲染了 " + countTexturedItems() + " 个带纹理的物体");
        }
    }
    
    /**
     * 渲染几何图形
     */
    private void renderGeometryItems() {
        final int count = renderData.getCount();
        boolean hasGeometryItems = false;
        
        // 检查是否有几何图形需要渲染
        for (int i = 0; i < count; i++) {
            if (renderData.getRegion(i) == null) {
                hasGeometryItems = true;
                break;
            }
        }
        
        if (!hasGeometryItems) {
            return; // 没有几何图形需要渲染
        }
        
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
        
        shapeRenderer.end();
        
        // 调试信息
        System.out.println("渲染了 " + countGeometryItems() + " ���几何图形");
    }
    
    /**
     * 计算带纹理的物体数量
     */
    private int countTexturedItems() {
        int count = 0;
        final int total = renderData.getCount();
        for (int i = 0; i < total; i++) {
            if (renderData.getRegion(i) != null) {
                count++;
            }
        }
        return count;
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
     * 调试绘制 - 优化版本
     */
    private void renderDebug() {
        // 使用单独的ShapeRenderer会话绘制调试信息
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        
        // 绘制相机视野边界
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
        /*
        // 如果需要绘制文本，可以在这里实现
        batch.begin();
        // font.draw(batch, "Rendered: " + renderData.getCount(), 10, 700);
        // font.draw(batch, "Textured: " + countTexturedItems(), 10, 680);
        // font.draw(batch, "Geometry: " + countGeometryItems(), 10, 660);
        // font.draw(batch, "Camera: " + renderingProcessor.getPosition(), 10, 640);
        // font.draw(batch, "Zoom: " + renderingProcessor.getZoom(), 10, 620);
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