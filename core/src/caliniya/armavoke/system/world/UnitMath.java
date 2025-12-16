package caliniya.armavoke.system.world;

import arc.util.Log;
import caliniya.armavoke.base.tool.Ar; // 假设这是你的数组类
import caliniya.armavoke.game.Unit;
import caliniya.armavoke.game.data.RouteData;
import caliniya.armavoke.game.data.WorldData;
import caliniya.armavoke.system.BasicSystem;

public class UnitMath extends BasicSystem<UnitMath> {

  // 本地缓存列表，用于快照
  private Ar<Unit> processList = new Ar<>();

  @Override
  public UnitMath init() {
    return super.init(true); // 开启后台线程
  }

  @Override
  public void update() {
    // 主线程可能正在往 moveunits 里添加或移除单位。
    // 如果后台线程直接遍历 WorldData.moveunits，会发生并发修改异常或数组越界崩溃。
    // 我们必须给 moveunits 加锁，快速拷贝一份到本地 processList，然后立刻释放锁。
    // if (processList.hashCode() != WorldData.moveunits.hashCode()) {
    processList.clear();
    synchronized (WorldData.moveunits) {
      processList.addAll(WorldData.moveunits);
      // }
    }

    // 注意：主线程在修改 WorldData.moveunits 时也必须 synchronized (WorldData.moveunits)
    for (int i = 0; i < processList.size; ++i) {
      Unit u = processList.get(i);

      // 检查单位是否有效 (防止单位在主线程刚刚被销毁)
      if (u == null || u.health <= 0) continue;

      if (u.pathed) continue;

      int sx = (int) (u.x / WorldData.TILE_SIZE);
      int sy = (int) (u.y / WorldData.TILE_SIZE);
      int tx = (int) (u.targetX / WorldData.TILE_SIZE);
      int ty = (int) (u.targetY / WorldData.TILE_SIZE);

      // 只有起点和终点不在同一个格子才寻路
      if (sx != tx || sy != ty) {
        u.path = RouteData.findPath(sx, sy, tx, ty);
        u.pathIndex = 0;

        if (u.path == null || u.path.isEmpty()) {
          u.pathFindCooldown = 60f;
        }
      }
      
      
      // 标记已处理
      u.pathed = true;
    }
  }
}
