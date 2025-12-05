package caliniya.atmadax;

import arc.backend.sdl.SdlApplication;
import arc.backend.sdl.SdlConfig;
import caliniya.armadax.Armadax;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher  {
	public static void main (String[] arg) {
		new SdlApplication(new Armadax(), new SdlConfig(){{
            title = "Armadax";
            maximized = true;
            width = 900;
            height = 700;
            }}
        );
	}
}
