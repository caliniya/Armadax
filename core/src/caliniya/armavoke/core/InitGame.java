package caliniya.armavoke.core;

import arc.Core;
import arc.Events;
import caliniya.armavoke.Armavoke;
import caliniya.armavoke.base.type.EventType;
import caliniya.armavoke.game.data.*;
import caliniya.armavoke.system.render.*;

public class InitGame {

  static {
    Events.on(EventType.GameInit.class, evevt -> testinit());
  }

  public static void testinit() {
    WorldData.initWorld();
    Armavoke.systems.add(new MapRender().init());
    Armavoke.systems.add(new UnitRender().init());
  }
}
