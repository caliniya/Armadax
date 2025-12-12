package caliniya.armavoke;

import arc.scene.ui.Dialog;
import arc.scene.ui.layout.Table;
import caliniya.armavoke.core.UI;
import caliniya.armavoke.ui.fragment.*;
import arc.ApplicationCore;
import arc.ApplicationListener;
import arc.assets.Loadable;
import arc.graphics.Color;
import arc.graphics.g2d.TextureAtlas;
import arc.util.Log;
import caliniya.armavoke.content.*;
import caliniya.armavoke.ui.*;

import static arc.Core.*;

public class Armavoke extends ApplicationCore {

  public boolean assinited = false;
  
  @Override
  public void setup() {
    Init.init();
  }

  @Override
  public void init() {
    super.init();
  }

  @Override
  public void update() {
    super.update();
    graphics.clear(Color.black);
    assets.update();
    if (assets.update() && !assinited) {
      //在这里进行二次引用
      atlas = assets.get("sprites/sprites.aatls", TextureAtlas.class);
      Styles.load();
      UI.Menu();
      assinited = true;
    }
    //绘制ui
    scene.act();
    scene.draw();
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

  @Override
  public void resize(int width, int height) {
    super.resize(width, height);
    scene.resize(width, height);
  }
}
