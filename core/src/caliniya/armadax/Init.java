package caliniya.armadax;

import arc.*;
import arc.util.*;

import static arc.Core.*;

public class Init {

  public static boolean android = app.isAndroid();
  public static boolean desktop = app.isDesktop();
  public static boolean init;

  public static void load() {
    settings.setAppName("Armadax");
    init = false;
    if (desktop) {
      // TODO: 在这里实现桌面端的日志处理器，但是桌面端长什么样?
      Log.info("desktop");
    }
    // 基本平台信息
    Log.info("Graphics init");
    Log.infoTag("Init-Info", "[GL] Version:" + graphics.getGLVersion());
    Log.info("[Init-Info] [GL] Using " + (gl30 != null ? "OpenGL 3" : "OpenGL 2"));
    if (gl30 == null)
      Log.warn(
          "[Init-Info] [Waning] device or video drivers do not support OpenGL 3. This will cause performance issues.");
    if(assets == null) {
    	Log.info("load assets(unexpected)");
    }
  }
  
  public static void inited(){
    init = true;
    Log.info("inited");
  }
  
  
}
