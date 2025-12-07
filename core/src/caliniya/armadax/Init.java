package caliniya.armadax;

import arc.Core;
import arc.Files;
import arc.util.Log;
import arc.util.OS;

import static arc.Core.*;

public class Init {

  public void load() {
    String dataDir = System.getProperty("armadax.data.dir" , OS.env("ARMADAX.DATA.DIR"));
    if(dataDir != null){
      Core.settings.setDataDirectory(files.absolute(dataDir));
    }
  }
}
