package caliniya.armavoke;

import arc.input.InputMultiplexer;
import arc.input.GestureDetector;
import caliniya.armavoke.base.tool.Ar;
import arc.graphics.g2d.Draw;
import caliniya.armavoke.core.UI;
import caliniya.armavoke.game.data.WorldData;
import caliniya.armavoke.game.type.UnitType;
import caliniya.armavoke.system.BasicSystem;
import caliniya.armavoke.system.render.MapRender;
import caliniya.armavoke.system.input.*;
import caliniya.armavoke.ui.fragment.*;
import arc.ApplicationCore;
import arc.ApplicationListener;
import arc.assets.Loadable;
import arc.graphics.Color;
import arc.graphics.g2d.TextureAtlas;
import arc.util.Log;
import caliniya.armavoke.system.world.*;
import caliniya.armavoke.content.*;
import caliniya.armavoke.ui.*;

import static arc.Core.*;

public class Armavoke extends ApplicationCore {

  public boolean assinited = false;
  public CameraInput camInput;

  public static Ar<BasicSystem> systems = new Ar<BasicSystem>();

  @Override
  public void setup() {
    graphics.clear(Color.black);
    Init.init();
    Floors.load();
    ENVBlocks.load();
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

    // 资源加载完成后的初始化
    if (assets.update() && !assinited) {
      atlas = assets.get("sprites/sprites.aatls", TextureAtlas.class);
      Styles.load();
      UI.Menu();
      scene.resize(graphics.getWidth(), graphics.getHeight());
      UnitControl unitCtrl = new UnitControl().init();
      systems.add(unitCtrl);
      camInput = new CameraInput().init();
      Log.info("loaded");
      InputMultiplexer multiplexer =
          new InputMultiplexer(
              scene,
              new GestureDetector(unitCtrl),
              new GestureDetector(camInput),
              unitCtrl,
              camInput);
      input.addProcessor(multiplexer);
      systems.add(camInput);
      UnitTypes.load();
      assinited = true;
    }

    // 加载界面
    if (!assinited) {
      UI.Loading();
    } else {
      Draw.proj(camera);

      for (int i = 0; i < systems.size; i++) {
        BasicSystem sys = systems.get(i);
        sys.update();
      }

      camera.update();
      Draw.flush();
    }
    scene.act();
    scene.draw();
    if (WorldData.units.size >= 1) {
      WorldData.units.get(1).update();
      WorldData.units.get(0).update();
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

  @Override
  public void resize(int width, int height) {
    super.resize(width, height);
    scene.resize(width, height);
    camera.resize(width, height);
  }
}
