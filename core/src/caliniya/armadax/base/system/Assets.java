package caliniya.armadax.system;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;

public class Assets extends AssetManager implements Disposable {
    
    private static Assets instance;
    private final ObjectMap<String, TextureAtlas> loadedAtlases;
    
    // 图集目录常量
    public static final String ATLASES_DIR = "sprites/";
    
    private Assets() {
        super(new InternalFileHandleResolver());
        this.loadedAtlases = new ObjectMap<>();
    }
    
    public static Assets getInstance() {
        if (instance == null) {
            instance = new Assets();
        }
        return instance;
    }
    
    /**
     * 加载
     */
    public void loadAssets() {
        // 加载主要的游戏纹理图集
        // TODO: 等等素材完善
        loadAtlas("sprites");
        
    }
    
    /**
     * 加载单个纹理图集
     */
    public void loadAtlas(String atlasName) {
        String fileName = ATLASES_DIR + atlasName + ".atlas";
        load(fileName, TextureAtlas.class);
    }
    
    /**
     * 完成加载并缓存图集引用
     */
    public void finishLoading() {
        super.finishLoading();
    }
    
    /**
     * 获取纹理图集
     */
    public TextureAtlas getAtlas(String atlasName) {
        // 首先尝试从缓存获取
        TextureAtlas atlas = loadedAtlases.get(atlasName);
        if (atlas != null) {
            return atlas;
        }
        
        // 如果缓存中没有，尝试加载
        String fileName = ATLASES_DIR + atlasName + ".atlas";
        if (contains(fileName, TextureAtlas.class)) {
            atlas = get(fileName, TextureAtlas.class);
            loadedAtlases.put(atlasName, atlas);
            return atlas;
        }
        return null;
    }
    
    /**
     * 从图集获取纹理区域
     */
    public TextureAtlas.AtlasRegion getRegion(String atlasName, String regionName) {
        TextureAtlas atlas = getAtlas(atlasName);
        if (atlas != null) {
            TextureAtlas.AtlasRegion region = atlas.findRegion(regionName);
            if (region != null) {
            return region;
                }
        }
        return null;
    }
    
    @Override
    public void dispose() {
        Array<String> atlasFiles = new Array<>();
        for (String fileName : getAssetNames()) {
            if (fileName.endsWith(".atlas")) {
                atlasFiles.add(fileName);
            }
        }
        for (String fileName : atlasFiles) {
            unload(fileName);
        }
        loadedAtlases.clear();
        super.dispose();
        instance = null;
    }
    
    /**
     * 静态便捷方法
     */
    public static TextureAtlas getAtlasStatic(String atlasName) {
        return getInstance().getAtlas(atlasName);
    }
    
    public static TextureAtlas.AtlasRegion getRegionStatic(String atlasName, String regionName) {
        return getInstance().getRegion(atlasName, regionName);
    }
    
    public static void loadAllStatic() {
        getInstance().loadAssets();
        getInstance().finishLoading();
    }
}