package caliniya.armadax.base.system;

import arc.assets.AssetManager;
import arc.assets.loaders.I18NBundleLoader;
import arc.graphics.g2d.TextureAtlas;
import arc.util.I18NBundle;
import caliniya.armadax.base.tool.Ar;
import java.util.Locale;

public class Assets extends AssetManager {
    
    private static Assets instance;
    public TextureAtlas atlas; // 唯一的图集
    // 图集文件路径
    public static final String ATLAS_PATH = "sprites/sprites.atlas";
    public static final String ATLAS_NAME = "sprites";
    
    // 语言文件相关常量
    public static final String I18N_BASE_PATH = "properties/messages";
    public static final String[] SUPPORTED_LANGUAGES = {
        "zh_CN",
        "en_US",
        "ja_JP",
        "ko_KR"
    };
    
    private Assets() {
        super();
    }
    
    public static Assets getInstance() {
        if (instance == null) {
            instance = new Assets();
        }
        return instance;
    }
    
    /**
     * 加载所有资源（图集和语言文件）
     */
    public void loadAssets() {
        // 加载图集
        load(ATLAS_PATH, TextureAtlas.class);
        
        // 加载所有支持的语言文件
        loadI18nFiles();
        
        // 等待加载完成
        finishLoading();
        
        // 获取图集引用
        atlas = get(ATLAS_PATH, TextureAtlas.class);
    }
    
    /**
     * 加载国际化文件
     */
    private void loadI18nFiles() {
        // 加载默认语言文件
        load(I18N_BASE_PATH + ".properties", I18NBundle.class);
        
        // 加载所有支持的语言变体
        for (String language : SUPPORTED_LANGUAGES) {
            String i18nPath = I18N_BASE_PATH + "_" + language + ".properties";
            load(i18nPath, I18NBundle.class);
        }
    }
    
    /**
     * 加载指定语言文件
     */
    public void loadLanguageFile(String languageCode) {
        String i18nPath = I18N_BASE_PATH + "_" + languageCode + ".properties";
        load(i18nPath, I18NBundle.class);
    }
    
    /**
     * 从图集获取纹理区域 - 使用父类的get
     */
    public TextureAtlas.AtlasRegion getRegion(String regionName) {
        // 首先尝试获取已缓存的图集
        if (atlas == null) {
            // 使用父类的contains和get方法检查并获取图集
            if (contains(ATLAS_PATH, TextureAtlas.class)) {
                atlas = get(ATLAS_PATH, TextureAtlas.class);
            } else {
                // 图集未加载，抛出异常或记录错误
                throw new IllegalStateException("Texture atlas not loaded. Please call loadAssets() first.");
            }
        }
        // 查找区域
        TextureAtlas.AtlasRegion region = atlas.find(regionName);
        return region;
    }
    
    /**
     * 获取I18NBundle资源
     */
    public I18NBundle getI18nBundle(String languageCode) {
        String i18nPath = I18N_BASE_PATH + "_" + languageCode + ".properties";
        if (contains(i18nPath, I18NBundle.class)) {
            return get(i18nPath, I18NBundle.class);
        }
        return null;
    }
    
    /**
     * 获取默认的I18NBundle资源
     */
    public I18NBundle getDefaultI18nBundle() {
        String defaultPath = I18N_BASE_PATH + ".properties";
        if (contains(defaultPath, I18NBundle.class)) {
            return get(defaultPath, I18NBundle.class);
        }
        return null;
    }
    
    /**
     * 预加载并缓存图集
     */
    public void ensureAtlasLoaded() {
        if (atlas == null && !contains(ATLAS_PATH, TextureAtlas.class)) {
            loadAssets();
        } else if (atlas == null) {
            atlas = get(ATLAS_PATH, TextureAtlas.class);
        }
    }
    
    @Override
    public void dispose() {
        // 卸载图集文件
        if (contains(ATLAS_PATH, TextureAtlas.class)) {
            unload(ATLAS_PATH);
        }
        
        // 卸载所有语言文件
        for (String language : SUPPORTED_LANGUAGES) {
            String i18nPath = I18N_BASE_PATH + "_" + language + ".properties";
            if (contains(i18nPath, I18NBundle.class)) {
                unload(i18nPath);
            }
        }
        
        // 卸载默认语言文件
        String defaultPath = I18N_BASE_PATH + ".properties";
        if (contains(defaultPath, I18NBundle.class)) {
            unload(defaultPath);
        }
        
        // 清除引用
        atlas = null;
        
        // 调用父类清理
        super.dispose();
        instance = null;
    }
    
    /**
     * 静态方法 - 改进版
     */
    public static TextureAtlas.AtlasRegion getRegionStatic(String regionName) {
        return getInstance().getRegion(regionName);
    }
    
    /**
     * 检查是否已加载图集 - 改进版
     */
    public boolean isLoaded() {
        // 检查是否在加载队列中或已加载
        if (contains(ATLAS_PATH, TextureAtlas.class)) {
            if (atlas == null) {
                atlas = get(ATLAS_PATH, TextureAtlas.class);
            }
            return true;
        }
        return false;
    }
    
    /**
     * 获取所有可用区域名称
     */
    public Ar<String> getAvailableRegions() {
        ensureAtlasLoaded();
        Ar<String> regionNames = new Ar<>();
        for (TextureAtlas.AtlasRegion region : atlas.getRegions()) {
            regionNames.add(region.name);
        }
        return regionNames;
    }
}