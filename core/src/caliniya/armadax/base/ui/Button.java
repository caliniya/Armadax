package caliniya.armadax.base.system;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.math.Rectangle;

public class Button implements Disposable {
    
    private NinePatch ninePatch;
    private NinePatchDrawable drawable;
    private boolean ownsNinePatch;
    
    private Runnable action;
    
    // 按钮状态
    private boolean isPressed = false;
    
    // 按钮位置和尺寸
    private float x;
    private float y;
    private float width;
    private float height;
    private Rectangle bounds;
    
    // 可见性
    private boolean visible = true;
    
    // 名称标识
    private String name;
    
    /**
     * 默认构造函数 - 使用默认UI按钮
     */
    public Button(String name) {
        this(name, "ui/button");
    }

    /**
     * 指定名称的构造
     */
    public Button(String name , String regionName) {
        this(name ,regionName ,null);
    }
    
    public Button(String name, Runnable action) {
        this(name, "ui/button", action);
    }
    
    public Button(String name, String regionName, Runnable action) {
        this.bounds = new Rectangle();
        this.name = name;
        if (action != null) {
            this.action = action;
        }
        initializeFromAtlas(regionName);
    }

    /**
     * 从图集初始化NinePatch（自动检测边距）
     */
    private void initializeFromAtlas( String regionName) {
        TextureAtlas.AtlasRegion region = Assets.getRegionStatic(regionName);
        if (region != null) {
            // 检查是否有split信息（NinePatch图片）
            if (hasSplitInfo(region)) {
                this.ninePatch = createNinePatchFromRegion(region);
            } else {
                // 普通纹理，使用默认边距
                this.ninePatch = new NinePatch(region, 10, 10, 10, 10);
            }
            
            this.ownsNinePatch = true;
            this.drawable = new NinePatchDrawable(ninePatch);
            setSize(ninePatch.getTotalWidth(), ninePatch.getTotalHeight());
        } else {
            throw new IllegalArgumentException("NO found" + regionName);
        }
    }

    /**
     * 检查区域是否有split信息（NinePatch）
     */
    private boolean hasSplitInfo(TextureAtlas.AtlasRegion region) {
        // 使用反射检查split字段（如果存在）
        try {
            java.lang.reflect.Field splitsField = TextureAtlas.AtlasRegion.class.getDeclaredField("splits");
            splitsField.setAccessible(true);
            int[] splits = (int[]) splitsField.get(region);
            return splits != null && splits.length >= 4;
        } catch (Exception e) {
            // 如果反射失败，使用其他方法检测
            return region.packedWidth < region.getTexture().getWidth() || 
               region.packedHeight < region.getTexture().getHeight();
               // 简单的启发式检测：如果区域比原始纹理小，可能是NinePatch
        }
    }

    /**
     * 从区域创建NinePatch（使用split信息）
     */
    private NinePatch createNinePatchFromRegion(TextureAtlas.AtlasRegion region) {
        try {
            // 使用反射获取split信息
            java.lang.reflect.Field splitsField = TextureAtlas.AtlasRegion.class.getDeclaredField("splits");
            splitsField.setAccessible(true);
            int[] splits = (int[]) splitsField.get(region);
            
            if (splits != null && splits.length >= 4) {
                return new NinePatch(region, splits[0], splits[1], splits[2], splits[3]);
            }
        } catch (Exception e) {
            // 静默处理异常
        }
        
        // 如果无法获取split信息，使用默认边距
        return new NinePatch(region, 10, 10, 10, 10);
    }

    /**
     * 更新
     当按钮被按下后触发(由ui系统调用)
     */
    public void update(Batch batch) {
        if (!visible || drawable == null) {
            return;
        }
        
        drawable.draw(batch, x, y, width, height);
        
    }

    /**
     * 设置按钮状态
     */
    public void setPressed(boolean pressed) {
        this.isPressed = pressed;
    }
    
    //按钮按下后触发的逻辑
    public void fire(){
        if(action != null) {
        	action.run();
        }
    }

    /**
     * 设置NinePatch
     */
    public void setNinePatch(NinePatch ninePatch, boolean owns) {
        disposeCurrentNinePatch();
        
        this.ninePatch = ninePatch;
        this.ownsNinePatch = owns;
        this.drawable = new NinePatchDrawable(ninePatch);
        setSize(ninePatch.getTotalWidth(), ninePatch.getTotalHeight());
    }

    /**
     * 获取NinePatchDrawable
     */
    public NinePatchDrawable getDrawable() {
        return drawable;
    }

    /**
     * 释放资源
     */
    private void disposeCurrentNinePatch() {
        if (ownsNinePatch && ninePatch != null) {
            // 注意：不应该直接dispose纹理，因为它由资源管理器处理
            ninePatch = null;
        }
    }

    @Override
    public void dispose() {
        disposeCurrentNinePatch();
        drawable = null;
    }
    
    // =================== 位置和尺寸相关方法 ===================
    
    /**
     * 设置位置
     */
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        updateBounds();
    }
    
    /**
     * 设置X坐标
     */
    public void setX(float x) {
        this.x = x;
        updateBounds();
    }
    
    /**
     * 设置Y坐标
     */
    public void setY(float y) {
        this.y = y;
        updateBounds();
    }
    
    /**
     * 设置尺寸
     */
    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
        updateBounds();
    }
    
    /**
     * 设置宽度
     */
    public void setWidth(float width) {
        this.width = width;
        updateBounds();
    }
    
    /**
     * 设置高度
     */
    public void setHeight(float height) {
        this.height = height;
        updateBounds();
    }
    
    /**
     * 获取X坐标
     */
    public float getX() {
        return x;
    }
    
    /**
     * 获取Y坐标
     */
    public float getY() {
        return y;
    }
    
    /**
     * 获取宽度
     */
    public float getWidth() {
        return width;
    }
    
    /**
     * 获取高度
     */
    public float getHeight() {
        return height;
    }
    
    /**
     * 更新边界矩形
     */
    private void updateBounds() {
        bounds.set(x, y, width, height);
    }
    
    /**
     * 获取边界矩形
     */
    public Rectangle getBounds() {
        return bounds;
    }
    
    /**
     * 检查点是否在按钮范围内
     */
    public boolean contains(float pointX, float pointY) {
        return bounds.contains(pointX, pointY);
    }
    
    // =================== 其他实用方法 ===================
    
    /**
     * 设置可见性
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    
    /**
     * 获取可见性
     */
    public boolean isVisible() {
        return visible;
    }
    
    /**
     * 设置名称
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * 获取名称
     */
    public String getName() {
        return name;
    }
    
    /**
     * 移动按钮
     */
    public void moveBy(float deltaX, float deltaY) {
        this.x += deltaX;
        this.y += deltaY;
        updateBounds();
    }
    
    /**
     * 设置按钮中心位置
     */
    public void setCenter(float centerX, float centerY) {
        setPosition(centerX - width / 2, centerY - height / 2);
    }
    
    /**
     * 获取按钮中心X坐标
     */
    public float getCenterX() {
        return x + width / 2;
    }
    
    /**
     * 获取按钮中心Y坐标
     */
    public float getCenterY() {
        return y + height / 2;
    }
}