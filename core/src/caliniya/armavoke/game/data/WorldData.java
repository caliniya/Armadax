package caliniya.armavoke.game.data;

import caliniya.armavoke.base.tool.Ar;
import caliniya.armavoke.game.Unit;
import caliniya.armavoke.world.Floor;
import caliniya.armavoke.world.World;

public class WorldData {
  // 游戏地图的世界数据

  public static World world; // 静态地图内容
  public static Ar<Unit> units = new Ar<Unit>(1000);

  private WorldData() {}

  public static void initWorld() {
    world = new World();
    world.test = true;
    world.init();
  }
  
  public static void clearunits() {
        for(int i = 0; i < units.size; i++) {
            units.get(i).remove();
        }
        units.clear();
    }
  
}
