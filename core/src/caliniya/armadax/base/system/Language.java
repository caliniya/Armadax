package caliniya.armadax.base.language;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.I18NBundle;

import java.util.HashMap;
import java.util.Locale;

public class Language {
    // 默认语言设置为中文
    private static final String DEFAULT_LANGUAGE = "zh_CN";
    // 单例实例
    private static Language instance;
    // 国际化资源包
    private I18NBundle bundle;
    // 缓存已加载的字符串，提高性能
    private HashMap<String, String> cache;
    // 当前语言环境
    private Locale currentLocale;

    // 静态初始化块，用于创建单例实例
    static {
        instance = new Language();
    }

    // 私有构造函数，防止外部直接创建实例
    private Language() {
        cache = new HashMap<>();
        detectAndSetDeviceLanguage();
    }

    // 设置语言和国家的公共方法
    public void setLanguage(String language) {
        try {
            currentLocale = new Locale(language);//指定语言
            bundle = I18NBundle.createBundle(Gdx.files.internal("properties/messages"), currentLocale);//读取指定语言的语言文件
            cache.clear(); // 语言改变后清除缓存
        } catch (Exception e) {
            // 语言加载失败，回退到默认语言p
            Gdx.app.error("LanguageManager", "无法加载语言 " + language  + "，已启用默认语言 " + DEFAULT_LANGUAGE);
            currentLocale = new Locale(DEFAULT_LANGUAGE);
            bundle = I18NBundle.createBundle(Gdx.files.internal("properties/messages"), currentLocale);//回退至简体中文
        }
    }

    // 自动检测并设置设备语言
    private void detectAndSetDeviceLanguage() {
        try{
        String language = Locale.getDefault().getLanguage();//例如zh
        String country = Locale.getDefault().getCountry();//例如CN或者TW
        String ard = language + "_" +  country;//例如zh_CN，即简体中文
        setLanguage(ard);
        }catch(Exception e){
            Gdx.app.error("多语言框架","读取设备语言失败，已启用默认语言");
        }
        
    }

    
    public String get(String key){
        try{
            String cachedValue = cache.get(key);
            if (cachedValue != null) {
                return cachedValue;
            }
            String value = bundle.get(key);
            cache.put(key , value);
            return value;
        } catch(Exception e){
            Gdx.app.error("LanguageManager", "不存在此字段: " + key);
            return "!?" + key + "?!缺失";
            
        }
    }
    
    
    
    // 支持占位符的字符串获取方法
    public String get(String key, Object... args) {
        try {
            // 先从缓存中获取
            String cachedValue = cache.get(key);
            if (cachedValue != null) {
                return cachedValue;
            }
            // 使用资源包的格式化方法来替换占位符
            String value = bundle.format(key, args);
            cache.put(key, value); // 缓存结果
            return value;
        } catch (Exception e) {
            // 处理键不存在的情况
            Gdx.app.error("LanguageManager", "不存在此字段: " + key);
            return "ERROR: " + key + " 此字段丢失";
        }
    }

    // 获取单例实例
    public static Language getInstance() {
        return instance;
    }
}