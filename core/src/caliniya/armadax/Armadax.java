package caliniya.armadax;

import arc.ApplicationCore;
import arc.ApplicationListener;
import arc.Core;
import arc.assets.AssetManager;
import arc.assets.Loadable;
import arc.graphics.g2d.TextureAtlas;
import arc.util.ArcRuntimeException;
import arc.util.Log;
import caliniya.armadax.content.*;

public class Armadax extends ApplicationCore {

  public boolean inited;

  @Override
  public void setup() {
    Core.assets = new AssetManager();
    Core.assets.load("sprites/sprites.aatls", TextureAtlas.class);
    Init.load();
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
    }
  }

  @Override
  public void add(ApplicationListener module) {
    super.add(module);
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
