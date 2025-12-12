package caliniya.armavoke;

import arc.backend.sdl.SdlApplication;
import arc.backend.sdl.SdlConfig;
import caliniya.armavoke.Armavoke;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {
  public static void main(String[] arg) {
    new SdlApplication(new Armavoke(), new SdlConfig() {
      {
        title = "armavoke";
        maximized = true;
        width = 900;
        height = 700;
      }
    });
  }
}
