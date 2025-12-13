package caliniya.armavoke.core;

import arc.Core;
import arc.Events;
import caliniya.armavoke.base.type.EventType;
import caliniya.armavoke.game.data.*;

public class InitGame {

  public static InitGame i;

  static {
    Events.on(EventType.GameInit.class, evevt -> testinit());
  }

  public static void testinit() {
    WorldData.initWorld();
  }

  public static void load() {
    if (i == null) {
      i = new InitGame();
    }
  }
}
