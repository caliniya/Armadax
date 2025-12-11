package caliniya.armadax;

import static arc.Core.*;

import android.os.Bundle;
import arc.*;
import arc.ApplicationListener;
import arc.backend.android.AndroidApplication;
import arc.backend.android.AndroidApplicationConfiguration;
import arc.files.Fi;
import arc.util.Log;
import arc.util.Log.*;
import cat.ereza.customactivityoncrash.config.CaocConfig;
import java.io.Writer;

public class AndroidLauncher extends AndroidApplication {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    CaocConfig.Builder.create().enabled(true).errorActivity(ErrorActivity.class).apply();

    initialize(
        new Armadax(),
        new AndroidApplicationConfiguration() {
          {
            useImmersiveMode = true;
            hideStatusBar = true;
            useGL30 = true;
          }
        });
    Fi data = Core.files.absolute(this.getExternalFilesDir(null).getAbsolutePath());
    // throw new ArcRuntimeException(data.toString());
    Core.settings.setDataDirectory(data);
    try {
      Writer writer = settings.getDataDirectory().child("log.txt").writer(false);
      LogHandler originalLogger = Log.logger;
      // 要过滤的标签列表(它们太多了而且一般没有用)
      String[] filteredTags = {"AndroidGraphics", "GL30", "OtherTag"};
      // 例外列表：这些消息不过滤
      String[] exceptions = {"[pause]", "[resume]"};

      Log.logger =
          (level, text) -> {
            if (level == LogLevel.info && Log.level.ordinal() > LogLevel.debug.ordinal()) {
              // 检查是否是例外
              boolean isException = false;
              for (String exception : exceptions) {
                if (text.contains(exception)) {
                  isException = true;
                  break;
                }
              }

              // 如果不是例外，才进行过滤
              if (!isException) {
                for (String tag : filteredTags) {
                  if (text.matches("\\[" + tag + "\\].*")) {
                    return;
                  }
                }
              }
            }
            originalLogger.log(level, text);
            try {
              writer.write(
                  "["
                      + Character.toUpperCase(level.name().charAt(0))
                      + "] "
                      + Log.removeColors(text)
                      + "\n");
              writer.flush();
            } catch (Exception e) {
              e.printStackTrace();
            }
          };
    } catch (Exception e) {
      // 只能这么做了
      Log.err(e);
    }
    Log.level = Log.LogLevel.info;
    Log.info("start-Android");
  }

  @Override
  public void addListener(ApplicationListener appl) {
    synchronized (this.getListeners()) {
      this.getListeners().add(appl);
    }
  }
}
