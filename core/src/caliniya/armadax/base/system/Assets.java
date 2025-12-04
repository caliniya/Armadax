package caliniya.armadax.base.system;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public class Assets extends AssetManager implements Disposable {
    
    private static Assets instance;
    public TextureAtlas atlas; // 唯一的图集
    
    // 图集文件路径
    public static final String ATLAS_PATH = "sprites/sprites.atlas";
    public static final String ATLAS_NAME = "sprites";
    
    private Assets() {
        super(new InternalFileHandleResolver());
    }
    
    public static Assets getInstance() {
        if (instance == null) {
            instance = new Assets();
        }
        return instance;
    }
    
    /**
     * 加载唯一的图集
     */
    public void loadAssets() {
        load(ATLAS_PATH, TextureAtlas.class);
        finishLoading();
        atlas = get(ATLAS_PATH, TextureAtlas.class);
    }
    
    /**
     * 从图集获取纹理区域（简化的单图集版本）
     */
    public TextureAtlas.AtlasRegion getRegion(String regionName) {
        if (atlas == null) {
            // 如果还没有加载，尝试获取
            if (contains(ATLAS_PATH, TextureAtlas.class)) {
                atlas = get(ATLAS_PATH, TextureAtlas.class);
            } else {
                // 图集未加载，尝试加载
                loadAssets();
            }
        }
        return atlas.findRegion(regionName);
    }
    
    /**
     * 从图集获取多个相同前缀的纹理区域（用于动画帧）
     */
    public Array<TextureAtlas.AtlasRegion> getRegions(String regionName) {
        if (atlas == null) {
            loadAssets();
        }
        return atlas.findRegions(regionName);
    }
    
    @Override
    public void dispose() {
        // 卸载图集文件
        if (contains(ATLAS_PATH, TextureAtlas.class)) {
            unload(ATLAS_PATH);
        }
        
        // 清除引用
        atlas = null;
        
        // 调用父类清理
        super.dispose();
        instance = null;
    }
    
    /**
     * 静态方法
     */
    public static TextureAtlas.AtlasRegion getRegionStatic(String regionName) {
        return getInstance().getRegion(regionName);
    }
    
    public static Array<TextureAtlas.AtlasRegion> getRegionsStatic(String regionName) {
        return getInstance().getRegions(regionName);
    }
    
    /**
     * 检查是否已加载图集
     */
    public boolean isLoaded() {
        return instance.update();
    }
}