package caliniya.armadax.base.system;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Disposable;
import caliniya.armadax.base.system.*;

public class Button extends Actor implements Disposable {
    private NinePatch ninePatch;
    private NinePatchDrawable drawable;
    private boolean ownsNinePatch;
    private Assets assets;
    
    // 按钮状态
    private boolean isPressed = false;
    private boolean isHovered = false;

    /**
     * 默认构造函数 - 使用默认UI按钮
     */
    public Button() {
        this("sprites", "ui/button");
    }

    /**
     * 指定图集和区域名称的构造
     */
    public Button(String atlasName, String regionName) {
        this.assets = Assets.getInstance();
        initializeFromAtlas(atlasName, regionName);
    }

    /**
     * 指定边距的构造函数
     */
    public Button(String atlasName, String regionName, int left, int right, int top, int bottom) {
        this.assets = Assets.getInstance();
        initializeWithCustomPadding(atlasName, regionName, left, right, top, bottom);
    }

    /**
     * 使用现有NinePatch的构造函数
     */
    public Button(NinePatch ninePatch) {
        this(ninePatch, false);
    }

    /**
     * 使用NinePatch并指定所有权
     */
    public Button(NinePatch ninePatch, boolean ownsNinePatch) {
        this.ninePatch = ninePatch;
        this.ownsNinePatch = ownsNinePatch;
        this.drawable = new NinePatchDrawable(ninePatch);
        setSize(ninePatch.getTotalWidth(), ninePatch.getTotalHeight());
    }

    /**
     * 从图集初始化NinePatch（自动检测边距）
     */
    private void initializeFromAtlas(String atlasName, String regionName) {
        TextureAtlas.AtlasRegion region = assets.getRegion(atlasName, regionName);
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
            throw new IllegalArgumentException("Region '" + regionName + "' not found in atlas: " + atlasName);
        }
    }

    /**
     * 使用自定义边距初始化
     */
    private void initializeWithCustomPadding(String atlasName, String regionName, 
                                           int left, int right, int top, int bottom) {
        TextureAtlas.AtlasRegion region = assets.getRegion(atlasName, regionName);
        if (region != null) {
            this.ninePatch = new NinePatch(region, left, right, top, bottom);
            this.ownsNinePatch = true;
            this.drawable = new NinePatchDrawable(ninePatch);
            setSize(ninePatch.getTotalWidth(), ninePatch.getTotalHeight());
        } else {
            throw new IllegalArgumentException("Region '" + regionName + "' not found in atlas: " + atlasName);
        }
    }

    /**
     * 检查区域是否有split信息（NinePatch）
     */
    private boolean hasSplitInfo(TextureAtlas.AtlasRegion region) {
        // 方法1：检查文件名是否以.9.png结尾
        if (region.name != null && region.name.endsWith(".9")) {
            return true;
        }
        
        // 方法2：使用反射检查split字段（如果存在）
        try {
            java.lang.reflect.Field splitsField = TextureAtlas.AtlasRegion.class.getDeclaredField("splits");
            splitsField.setAccessible(true);
            int[] splits = (int[]) splitsField.get(region);
            return splits != null && splits.length >= 4;
        } catch (Exception e) {
            // 如果反射失败，使用其他方法检测
            return detectSplitByTextureBounds(region);
        }
    }

    /**
     * 通过纹理边界检测NinePatch（备选方法）
     */
    private boolean detectSplitByTextureBounds(TextureAtlas.AtlasRegion region) {
        // 简单的启发式检测：如果区域比原始纹理小，可能是NinePatch
        return region.packedWidth < region.getTexture().getWidth() || 
               region.packedHeight < region.getTexture().getHeight();
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
            System.err.println("Failed to get split info from region: " + e.getMessage());
        }
        
        // 如果无法获取split信息，使用默认边距
        return new NinePatch(region, 10, 10, 10, 10);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (drawable != null) {
            // 保存当前颜色
            float r = batch.getColor().r;
            float g = batch.getColor().g;
            float b = batch.getColor().b;
            float a = batch.getColor().a;
            
            // 根据按钮状态调整颜色
            if (isPressed) {
                batch.setColor(r * 0.8f, g * 0.8f, b * 0.8f, a * parentAlpha);
            } else if (isHovered) {
                batch.setColor(r * 0.9f, g * 0.9f, b * 0.9f, a * parentAlpha);
            } else {
                batch.setColor(r, g, b, a * parentAlpha);
            }
            
            // 绘制NinePatch
            drawable.draw(batch, getX(), getY(), getWidth(), getHeight());
            
            // 恢复颜色
            batch.setColor(r, g, b, a);
        }
    }

    /**
     * 设置按钮状态 - 按下
     */
    public void setPressed(boolean pressed) {
        this.isPressed = pressed;
    }

    /**
     * 设置按钮状态 - 悬停
     */
    public void setHovered(boolean hovered) {
        this.isHovered = hovered;
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
            // 注意：通常不应该直接dispose纹理，因为它可能被共享
            ninePatch = null;
        }
    }

    @Override
    public void dispose() {
        disposeCurrentNinePatch();
        drawable = null;
    }

    @Override
    public boolean remove() {
        dispose();
        return super.remove();
    }
}