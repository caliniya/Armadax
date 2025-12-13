package caliniya.armavoke.ui.fragment;

import arc.Core;
import arc.Events;
import arc.scene.Group;
import arc.scene.ui.layout.Table;
import arc.util.Log;
import caliniya.armavoke.core.InitGame;
import caliniya.armavoke.ui.*;

import static caliniya.armavoke.base.type.EventType.*;

public class MenuFragment {

  public void build() {
    Table root = new Table();
    root.setFillParent(true);
    root.background(null);
    Core.scene.root.addChild(root);

    float menuWidth = 260f;

    root.bottom().left();

    root.table(
            menu -> {
              menu.defaults().width(menuWidth).height(70f).padBottom(0);

              menu.add(new Button("开始", () -> InitGame.testinit()));
              menu.row();

              menu.add(new Button("A1", () -> Log.info("A1")));
              menu.row();

              menu.add(new Button("A2", () -> Log.info("A2")));
              menu.row();

              menu.add(new Button("A3", () -> Log.info("A3")));
              menu.row();

              menu.add(new Button("退出", () -> Core.app.exit()));
            })
        .width(menuWidth)
        .padLeft(20f)
        .padBottom(60f);
  }
}
