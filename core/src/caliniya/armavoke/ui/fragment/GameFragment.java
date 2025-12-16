package caliniya.armavoke.ui.fragment;

import arc.Core;
import arc.Events;
import arc.scene.Group;
import arc.scene.ui.layout.Table;
import caliniya.armavoke.base.type.EventType;
import caliniya.armavoke.ui.Button;

public class GameFragment {

  private boolean isCommandEnabled = false;
  private Button commandBtn;

  public void build() {

    Table table = new Table();
    table.setFillParent(true); // 填满屏幕
    table.bottom().left();
    
    commandBtn =
        new Button(
            "@指挥",
            () -> {
              isCommandEnabled = !isCommandEnabled;
              Events.fire(new EventType.CommandChange(isCommandEnabled));
            });

    table.add(commandBtn).size(120f, 50f).margin(10f);

    Core.scene.root.addChild(table);
  }
}
