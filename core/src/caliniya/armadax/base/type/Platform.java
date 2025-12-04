package caliniya.armadax.base.type;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;

public class Platform {
    
    public enum platform {
        Desktop,
        Mobile
    }
    
    private static platform plat;
    
    // 私有构造函数，防止实例化
    private Platform() {
        // 工具类，不提供实例化
    }
    
    private static void initPlat() {
        ApplicationType appType = Gdx.app.getType();
        if (appType == ApplicationType.Android || 
            appType == ApplicationType.iOS) {
            plat = platform.Mobile;
        } else {
            plat = platform.Desktop;
        }
    }
    
    /**
     * 获取平台类型
     */
    public static platform getPlat() {
        if (plat == null) {
            initPlat();
        }
        return plat;
    }
    
    /**
     * 判断是否为移动平台
     */
    public static boolean isMobile() {
        return getPlat() == platform.Mobile;
    }
    
    /**
     * 判断是否为桌面平台
     */
    public static boolean isDesktop() {
        return getPlat() == platform.Desktop;
    }
    
    /**
     * 重置平台检测（用于测试）
     */
    public static void reset() {
        plat = null;
    }
}