package caliniya.armadax.system.render;

import caliniya.armadax.system.render.data.GameRenderData;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;

/**
 * 渲染处理系统 - 充当相机角色，负责计算视野内的物体
 */
public class RenderingProcessor {
    
    private final GameRenderData renderData;
    
    // 相机属性
    private final Vector2 position = new Vector2(); // 相机中心位置
    private float viewportWidth = 1280f;  // 视野宽度
    private float viewportHeight = 720f; // 视野高度
    private float zoom = 1f;            // 缩放级别
    
    // 视野边界（缓存计算）
    private final Rectangle viewBounds = new Rectangle();
    
    // 测试用颜色
    private final Color whiteColor = new Color(1, 1, 1, 1);
    
    public RenderingProcessor(GameRenderData renderData) {
        this.renderData = renderData;
        updateViewBounds();
    }
    
    /**
     * 处理所有需要渲染的物体
     * 修改：始终在屏幕正中心渲染一个白色矩形
     */
    public void process(/*Array<Entity> entities*/) {
        // 清空上一帧的数据
        renderData.clear();
        
        // 更新视野边界
        updateViewBounds();
        
        // 测试代码：在屏幕正中心渲染一个白色矩形
        addCenteredTestRectangle();
        
        // 移除之前的随机测试物体，专注于中心矩形测试
    }
    
    /**
     * 在屏幕正中心添加一个测试矩形
     */
    private void addCenteredTestRectangle() {
        // 计算屏幕中心位置（屏幕坐标）
        float screenCenterX = viewportWidth / 2f;
        float screenCenterY = viewportHeight / 2f;
        
        // 矩形大小（屏幕坐标）
        float rectWidth = 500f;
        float rectHeight = 500f;
        
        // 计算矩形位置（使其中心在屏幕中心）
        float rectX = screenCenterX - rectWidth / 2f;
        float rectY = screenCenterY - rectHeight / 2f;
        
        // 添加到渲染数据 - 使用null纹理和白色颜色
        renderData.add(
            null,           // 纹理（null表示使用颜色填充）
            rectX,          // 屏幕X坐标
            rectY,          // 屏幕Y坐标
            rectWidth,      // 宽度
            rectHeight,     // 高度
            0f,             // 原点X（左上角）
            0f,             // 原点Y（左上角）
            1f, 1f, 0f,     // 缩放和旋转
            whiteColor      // 白色颜色
        );
        
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
    
    /**
     * 获取当前视口尺寸
     */
    public Vector2 getViewportSize() {
        return new Vector2(viewportWidth, viewportHeight);
    }
}