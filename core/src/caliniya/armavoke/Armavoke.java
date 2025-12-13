package caliniya.armavoke;

import arc.graphics.g2d.Draw;
import caliniya.armavoke.core.UI;
import caliniya.armavoke.system.render.MapRender;
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
  public MapRender m;

  @Override
  public void setup() {
    graphics.clear(Color.black);
    Init.init();
    Floors.load();
    camera.resize(graphics.getWidth(), graphics.getHeight());
  }

  @Override
  public void init() {
    super.init();
  }

  @Override
  public void update() {
    super.update();
    graphics.clear(Color.black);
    if (assets.update() && !assinited) {
      // 在这里进行二次引用
      atlas = assets.get("sprites/sprites.aatls", TextureAtlas.class);
      Styles.load();
      UI.Menu();
      scene.resize(graphics.getWidth(), graphics.getHeight());
      m = new MapRender();
      Log.info("loaded");
      //camera.position.set(200, 200);
      assinited = true;
    }
    if (!assinited) {
      UI.Loading();
    }
    if (assinited) {
      camera.update();
      Draw.proj(camera);
      m.render();
      Draw.flush();
      //Draw.proj().setOrtho(0, 0, graphics.getWidth(), graphics.getHeight());
    }
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
    camera.resize(width, height);
  }
}
