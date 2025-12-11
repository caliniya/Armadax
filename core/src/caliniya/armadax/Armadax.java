package caliniya.armadax;

import arc.ApplicationCore;
import arc.ApplicationListener;
import arc.assets.AssetManager;
import arc.assets.Loadable;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.ScreenQuad;
import arc.graphics.g2d.TextureAtlas;
import arc.scene.Scene;
import arc.util.ArcRuntimeException;
import arc.util.Log;
import arc.util.ScreenRecorder;
import arc.util.ScreenUtils;
import arc.util.viewport.ScreenViewport;
import arc.util.viewport.Viewport;
import caliniya.armadax.content.*;
import caliniya.armadax.ui.Fonts;

import static arc.Core.*;

public class Armadax extends ApplicationCore{

  public boolean inited;

  @Override
  public void setup() {
    Init.load();
  }

  @Override
  public void init() {
    super.init();
  }

  @Override
  public void update() {
    super.update();
    graphics.clear(Color.black);
    if (!assets.update()) {
      assets.update();
    }else{
      if(Fonts.def != null) {
      Draw.batch(batch);
      Fonts.def.draw("test" ,500 ,500);
      }
    }
  
  }

  @Override
  public void add(ApplicationListener module) {
    super.add(module);
    if (module instanceof Loadable l) {
      assets.load(l);
    }
  }

  @Override
  public void dispose() {
    super.dispose();
    assets.dispose();
  }
}
