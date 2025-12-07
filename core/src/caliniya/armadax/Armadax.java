package caliniya.armadax;

import arc.ApplicationCore;
import arc.ApplicationListener;
import arc.Core;
import arc.assets.AssetManager;
import arc.assets.Loadable;
import arc.graphics.g2d.TextureAtlas;
import arc.util.Log;
import caliniya.armadax.content.*;

public class Armadax extends ApplicationCore {

  public boolean inited;
    public Init init;

  @Override
  public void setup() {
    Core.assets = new AssetManager();
    Core.assets.load("sprites/sprites.aatls", TextureAtlas.class);
    init = new Init();
    init.load();
  }

  @Override
  public void init() {
    super.init();
  }

  @Override
  public void update() {
    super.update();
    if (!Core.assets.update()) {
      Core.assets.update();
    } else {
        Log.info("AAAAA");
    }
  }

  @Override
  public void add(ApplicationListener module) {
    super.add(module);

    // autoload modules when necessary
    if (module instanceof Loadable l) {
      Core.assets.load(l);
    }
  }

  @Override
  public void dispose() {
    super.dispose();
    Core.assets.dispose();
  }
}
