package caliniya.armadax.base.language;

import arc.Core;
import arc.util.I18NBundle;
import arc.util.Log;
import caliniya.armadax.base.system.Assets;
import java.text.MessageFormat;
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

    // 私有构造函数，防止外部直接创建实例
    private Language() {
        cache = new HashMap<>();
        // 不在这里检测语言，等待init()方法调用
    }

    /**
     * 初始化语言管理器（应在Assets加载后调用）
     */
    public void init() {
        detectAndSetDeviceLanguage();
    }

    // 设置语言和国家的公共方法
    public void setLanguage(String languageCode) {
        try {
            // 解析语言代码
            String[] parts = languageCode.split("[_-]");
            if (parts.length >= 2) {
                currentLocale = new Locale(parts[0], parts[1]);
            } else if (parts.length == 1) {
                currentLocale = new Locale(parts[0]);
            } else {
                currentLocale = new Locale(DEFAULT_LANGUAGE);
            }
            
            // 从Assets资源管理器中获取对应的I18NBundle
            bundle = Assets.getInstance().getI18nBundle(languageCode);
            
            // 如果指定语言不存在，尝试查找语言变体
            if (bundle == null && parts.length >= 1) {
                bundle = Assets.getInstance().getI18nBundle(parts[0]);
            }
            
            // 如果还找不到，使用默认语言
            if (bundle == null) {
                bundle = Assets.getInstance().getDefaultI18nBundle();
            }
            
            // 如果默认语言也不存在，从文件直接加载（兼容模式）
            if (bundle == null) {
                Log.warn("LanguageManager", "语言文件未通过Assets加载，使用兼容模式直接加载");
                currentLocale = new Locale(languageCode);
                bundle = I18NBundle.createBundle(Core.files.internal("properties/messages"), currentLocale);
            }
            
            cache.clear(); // 语言改变后清除缓存
            
            // 触发语言更改事件
            //Core.app.post(() -> Core.events.fire(new LanguageChangedEvent(languageCode)));
            
            Log.info("LanguageManager", "语言已设置为: " + languageCode);
            
        } catch (Exception e) {
            // 语言加载失败，回退到默认语言
            Log.warn("LanguageManager", "无法加载语言 " + languageCode + "，已启用默认语言 " + DEFAULT_LANGUAGE, e);
            fallbackToDefault();
        }
    }

    // 回退到默认语言
    private void fallbackToDefault() {
        try {
            currentLocale = new Locale(DEFAULT_LANGUAGE);
            bundle = Assets.getInstance().getI18nBundle(DEFAULT_LANGUAGE);
            
            if (bundle == null) {
                bundle = Assets.getInstance().getDefaultI18nBundle();
            }
            
            if (bundle == null) {
                bundle = I18NBundle.createBundle(Core.files.internal("properties/messages"), currentLocale);
            }
            
            cache.clear();
        } catch (Exception e) {
            Log.err("LanguageManager", "无法加载默认语言", e);
            // 创建空的bundle防止NPE
            bundle = new I18NBundle();
        }
    }

    // 自动检测并设置设备语言
    private void detectAndSetDeviceLanguage() {
        try {
            Locale deviceLocale = Locale.getDefault();
            String language = deviceLocale.getLanguage();
            String country = deviceLocale.getCountry();
            
            // 尝试完全匹配（如zh_CN）
            String fullCode = language + "_" + country;
            
            // 检查Assets是否加载了此语言文件
            if (Assets.getInstance().getI18nBundle(fullCode) != null) {
                setLanguage(fullCode);
                return;
            }
            
            // 尝试只匹配语言（如zh）
            if (Assets.getInstance().getI18nBundle(language) != null) {
                setLanguage(language);
                return;
            }
            
            // 如果都不匹配，使用默认语言
            setLanguage(DEFAULT_LANGUAGE);
            
        } catch(Exception e) {
            Log.warn("多语言框架", "读取设备语言失败，已启用默认语言", e);
            setLanguage(DEFAULT_LANGUAGE);
        }
    }
    
    public String get(String key) {
        if (key == null || key.isEmpty()) {
            return "";
        }
        
        try {
            String cachedValue = cache.get(key);
            if (cachedValue != null) {
                return cachedValue;
            }
            String value = bundle.get(key);
            cache.put(key, value);
            return value;
        } catch(Exception e) {
            Log.warn("LanguageManager", "键不存在: " + key);
            return "!?" + key + "?!缺失";
        }
    }
    
    // 支持占位符的字符串获取方法
    public String get(String key, Object... args) {
        try {
            // 先从缓存中获取基本字符串
            String cachedValue = cache.get(key);
            if (cachedValue != null && args.length == 0) {
                return cachedValue;
            }
            
            // 获取基础文本
            String text;
            if (cachedValue == null) {
                text = bundle.get(key);
                if (args.length == 0) {
                    cache.put(key, text); // 只有无参数时才缓存
                }
            } else {
                text = cachedValue;
            }
            
            // 如果有参数，则格式化字符串
            if (args.length > 0) {
                try {
                    return MessageFormat.format(text, args);
                } catch (Exception e) {
                    // 如果MessageFormat失败，尝试简单的替换
                    return formatString(text, args);
                }
            }
            
            return text;
        } catch (Exception e) {
            // 处理键不存在的情况
            Log.warn("LanguageManager", "不存在此字段: " + key);
            return "ERROR: " + key + " 此字段丢失";
        }
    }
    
    // 简单的字符串格式化（备用）
    private String formatString(String format, Object... args) {
        try {
            String result = format;
            for (int i = 0; i < args.length; i++) {
                String placeholder = "{" + i + "}";
                result = result.replace(placeholder, args[i] != null ? args[i].toString() : "null");
            }
            return result;
        } catch (Exception e) {
            Log.err("LanguageManager", "格式化字符串失败: " + format);
            return format;
        }
    }

    // 获取当前语言代码
    public String getCurrentLanguage() {
        return currentLocale != null ? currentLocale.toString() : DEFAULT_LANGUAGE;
    }

    // 清除缓存（内存紧张时使用）
    public void clearCache() {
        cache.clear();
    }

    // 重新加载当前语言
    public void reload() {
        if (currentLocale != null) {
            setLanguage(getCurrentLanguage());
        } else {
            setLanguage(DEFAULT_LANGUAGE);
        }
    }

    // 获取单例实例
    public static Language getInstance() {
        if (instance == null) {
            instance = new Language();
        }
        return instance;
    }
    
    // 语言更改事件
    public static class LanguageChangedEvent {
        public final String languageCode;
        
        public LanguageChangedEvent(String languageCode) {
            this.languageCode = languageCode;
        }
    }
}