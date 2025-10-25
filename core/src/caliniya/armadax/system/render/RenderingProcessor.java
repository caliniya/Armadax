package caliniya.armadax.system.render;

import caliniya.armadax.system.render.data.GameRenderData;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

/**
 * 渲染处理系统 - 充当相机角色，负责计算视野内的物体
 */
public class RenderingProcessor {
    
    private final GameRenderData renderData;
    
    // 相机属性
    private final Vector2 position = new Vector2(); // 相机中心位置
    private float viewportWidth = 10f;  // 视野宽度
    private float viewportHeight = 10f; // 视野高度
    private float zoom = 1f;            // 缩放级别
    
    // 视野边界（缓存计算）
    private final Rectangle viewBounds = new Rectangle();
    
    public RenderingProcessor(GameRenderData renderData) {
        this.renderData = renderData;
        updateViewBounds();
    }
    
    /**
     * 处理所有需要渲染的物体
     * 暂时注释掉实体遍历部分，等物理世界完成后再实现
     */
    public void process(/*Array<Entity> entities*/) {
        // 清空上一帧的数据
        renderData.clear();
        
        // 更新视野边界
        updateViewBounds();
        
        // TODO: 等物理世界完成后，这里遍历所有实体
        /*
        for (Entity entity : entities) {
            // 这里假设实体有获取位置和纹理的方法
            Vector2 entityPos = entity.getPosition();
            TextureRegion entityTexture = entity.getTexture();
            float entityWidth = entity.getWidth();
            float entityHeight = entity.getHeight();
            
            if (isInView(entityPos, entityWidth, entityHeight)) {
                // 计算屏幕相对坐标（相对于相机）
                float screenX = (entityPos.x - viewBounds.x) * zoom;
                float screenY = (entityPos.y - viewBounds.y) * zoom;
                
                // 添加到渲染数据
                renderData.add(
                    entityTexture,
                    screenX,
                    screenY,
                    entityWidth * zoom,
                    entityHeight * zoom,
                    entityWidth / 2f, // 默认原点在中心
                    entityHeight / 2f,
                    1f, 1f, 0f,      // 默认缩放和旋转
                    null              // 默认颜色
                );
            }
        }
        */
        
        // 临时测试代码：添加一些测试渲染数据
        addTestRenderData();
    }
    
    /**
     * 临时测试方法：添加一些测试渲染数据
     */
    private void addTestRenderData() {
        // 这里添加一些在视野内的测试物体
        if (true) { // 临时条件，确保至少有一些渲染内容
            // 假设我们有一个测试纹理，这里用null代替
            // 在实际使用中，这里应该传入真实的TextureRegion
            
            // 添加一个在相机中心的物体
            float centerX = viewportWidth / 2f;
            float centerY = viewportHeight / 2f;
            renderData.add(
                null, // 测试用null纹理
                centerX - 1f, centerY - 1f, // 位置
                2f, 2f,                     // 大小
                1f, 1f,                     // 原点（中心）
                1f, 1f, 0f,                 // 缩放和旋转
                null                        // 颜色
            );
            
            // 添加几个随机位置的测试物体
            for (int i = 0; i < 5; i++) {
                float x = (float) Math.random() * viewportWidth;
                float y = (float) Math.random() * viewportHeight;
                float size = 0.5f + (float) Math.random() * 1f;
                
                renderData.add(
                    null,
                    x, y,
                    size, size,
                    size / 2f, size / 2f,
                    1f, 1f, 0f,
                    null
                );
            }
        }
    }
    
    /**
     * 检查物体是否在视野内
     */
    private boolean isInView(Vector2 entityPos, float entityWidth, float entityHeight) {
        Rectangle entityBounds = new Rectangle(
            entityPos.x, entityPos.y, entityWidth, entityHeight
        );
        return viewBounds.overlaps(entityBounds);
    }
    
    /**
     * 更新视野边界
     */
    private void updateViewBounds() {
        float halfWidth = (viewportWidth / zoom) / 2f;
        float halfHeight = (viewportHeight / zoom) / 2f;
        
        viewBounds.set(
            position.x - halfWidth,    // x
            position.y - halfHeight,   // y
            viewportWidth / zoom,      // width
            viewportHeight / zoom      // height
        );
    }
    
    // ========== 相机控制方法 ==========
    
    public void setPosition(float x, float y) {
        position.set(x, y);
        updateViewBounds();
    }
    
    public void setPosition(Vector2 newPosition) {
        position.set(newPosition);
        updateViewBounds();
    }
    
    public Vector2 getPosition() {
        return new Vector2(position);
    }
    
    public void setViewport(float width, float height) {
        this.viewportWidth = width;
        this.viewportHeight = height;
        updateViewBounds();
    }
    
    public void setZoom(float zoom) {
        this.zoom = Math.max(0.1f, zoom); // 防止缩放到0或负数
        updateViewBounds();
    }
    
    public float getZoom() {
        return zoom;
    }
    
    public Rectangle getViewBounds() {
        return new Rectangle(viewBounds);
    }
    
    public void move(float dx, float dy) {
        position.add(dx, dy);
        updateViewBounds();
    }
    
    public void zoom(float delta) {
        setZoom(zoom + delta);
    }
    
    /**
     * 将世界坐标转换为屏幕坐标
     */
    public Vector2 worldToScreen(float worldX, float worldY) {
        return new Vector2(
            (worldX - viewBounds.x) * zoom,
            (worldY - viewBounds.y) * zoom
        );
    }
    
    /**
     * 将屏幕坐标转换为世界坐标
     */
    public Vector2 screenToWorld(float screenX, float screenY) {
        return new Vector2(
            viewBounds.x + (screenX / zoom),
            viewBounds.y + (screenY / zoom)
        );
    }
    
    public int getRenderCount() {
        return renderData.getCount();
    }
}