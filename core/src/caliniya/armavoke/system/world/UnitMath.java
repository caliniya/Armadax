package caliniya.armavoke.system.world;

import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Point2;
import caliniya.armavoke.base.tool.Ar;
import caliniya.armavoke.game.Unit;
import caliniya.armavoke.game.data.RouteData;
import caliniya.armavoke.game.data.WorldData;
import caliniya.armavoke.system.BasicSystem;

public class UnitMath extends BasicSystem<UnitMath> {

  private Ar<Unit> processList = new Ar<>();

  @Override
  public UnitMath init() {
    return super.init(true);
  }

  @Override
  public void update() {
    processList.clear();
    synchronized (WorldData.moveunits) {
      processList.addAll(WorldData.moveunits);
    }

    for (int i = 0; i < processList.size; ++i) {
      Unit u = processList.get(i);

      // 1. 如果单位死亡或失效，从源列表中移除，防止僵尸单位占用计算
      if (u == null || u.health <= 0) {
        synchronized (WorldData.moveunits) {
          WorldData.moveunits.remove(u);
        }
        continue;
      }

      if (!u.pathed) {
        calculatePath(u);
        u.pathed = true;
        u.velocityDirty = true;
      }

      calculateVelocity(u);
    }
  }

  private void calculatePath(Unit u) {
    int sx = (int) (u.x / WorldData.TILE_SIZE);
    int sy = (int) (u.y / WorldData.TILE_SIZE);
    int tx = (int) (u.targetX / WorldData.TILE_SIZE);
    int ty = (int) (u.targetY / WorldData.TILE_SIZE);

    if (sx != tx || sy != ty) {
      u.path = RouteData.findPath(sx, sy, tx, ty , 2 , 1);
      u.pathIndex = 0;

      if (u.path == null || u.path.isEmpty()) {
        u.pathFindCooldown = 60f;
        u.speedX = 0;
        u.speedY = 0;
        // 寻路失败也应该视为"到达"(或者放弃)，移除出列表，避免单位卡在原地一直尝试计算
        stopAndRemove(u);
      }
    } else {
      if (u.path != null) u.path.clear();
      u.velocityDirty = true;
    }
  }

  private void calculateVelocity(Unit u) {
    if (u.path == null || u.path.isEmpty()) {
      handleFinalApproach(u);
      return;
    }

    // --- 路径跟随逻辑 ---
    // 如果还没走完
    if (u.pathIndex < u.path.size) {

      // 计算当前要去的那个点的"世界坐标"
      float nextX, nextY;

      // 如果是路径列表的最后一个点 -> 使用精确点击坐标 (targetX, targetY)
      if (u.pathIndex == u.path.size - 1) {
        nextX = u.targetX;
        nextY = u.targetY;
      }
      // 否则 -> 使用网格中心坐标
      else {
        Point2 node = u.path.get(u.pathIndex);
        nextX = node.x * WorldData.TILE_SIZE + WorldData.TILE_SIZE / 2f;
        nextY = node.y * WorldData.TILE_SIZE + WorldData.TILE_SIZE / 2f;
      }

      float dist = Mathf.dst(u.x, u.y, nextX, nextY);

      if (dist <= u.speed) {
        u.pathIndex++;
        u.velocityDirty = true;
        calculateVelocity(u); // 递归去下一个点
        return;
      }

      if (u.velocityDirty) {
        u.angle = Angles.angle(u.x, u.y, nextX, nextY);
        u.speedX = Mathf.cosDeg(u.angle) * u.speed;
        u.speedY = Mathf.sinDeg(u.angle) * u.speed;
        u.velocityDirty = false;
      }
    } else {
      // 如果 pathIndex 超出了 list 大小，说明已经走完所有节点（包括最后一个）
      // 这里的逻辑通常不会触发，因为上面最后一个点就是 targetX/Y，到达后就应该直接 stopAndRemove 了
      // 但为了保险起见，或者应对 pathIndex 异常增加，这里再 check 一次
      handleFinalApproach(u);
    }
  }

  /** 处理最后一段路 */
  private void handleFinalApproach(Unit u) {
    float distToFinal = Mathf.dst(u.x, u.y, u.targetX, u.targetY);

    if (distToFinal > u.speed) {
      if (u.velocityDirty && distToFinal > 0.1f) {
        u.angle = Angles.angle(u.x, u.y, u.targetX, u.targetY);
        u.speedX = Mathf.cosDeg(u.angle) * u.speed;
        u.speedY = Mathf.sinDeg(u.angle) * u.speed;
        u.velocityDirty = false;
      }
    } else {
      // 彻底到达终点
      stopAndRemove(u);
    }
  }

  /** 辅助方法：停止单位并从导航列表移除 */
  private void stopAndRemove(Unit u) {
    u.speedX = 0;
    u.speedY = 0;
    u.velocityDirty = false; // 重置标记

    // 从源列表移除，彻底停止计算
    // 这样下一帧 update() 就不会再遍历到这个单位
    synchronized (WorldData.moveunits) {
      WorldData.moveunits.remove(u);
    }
  }
}