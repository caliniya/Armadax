package caliniya.armavoke.game.data;

import caliniya.armavoke.base.tool.Ar;
import caliniya.armavoke.game.Unit;
import caliniya.armavoke.world.Floor;
import caliniya.armavoke.world.World;

public class WorldData {
  // 游戏地图的世界数据

  public static World world; // 静态地图内容
  public static Ar<Unit> units;

  private WorldData() {}

  public static void initWorld() {
    world = new World();
    world.test = true;
    world.init();
  }
}
