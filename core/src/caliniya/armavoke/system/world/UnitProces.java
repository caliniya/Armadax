package caliniya.armavoke.system.world;

import arc.math.Angles;
import arc.math.Mathf;
import arc.util.Log;
import caliniya.armavoke.base.tool.Ar;
import caliniya.armavoke.game.Unit;
import caliniya.armavoke.game.data.WorldData;
import caliniya.armavoke.system.BasicSystem;

// 单位移动控制和单位物理处理
// 在后台运行 (super.init(true))
public class UnitProces extends BasicSystem<UnitProces> {

  @Override
  public UnitProces init() {
    // 尝试以60的帧率更新
    return super.init(true);
  }

  @Override
  public void update() {
    Ar<Unit> list = WorldData.units;

    for (int i = 0; i < list.size; i++) {
      Unit u = list.get(i);

      if (u == null || u.health <= 0) continue;

      float oldX = u.x;
      float oldY = u.y;

      // Log.info(u.targetX + " "+ u.targetY +"    ");

      // --- 1. 终点吸附逻辑 ---
      if (!(u.path == null) && !u.path.isEmpty()) {
        float distToTarget = Mathf.dst(u.x, u.y, u.targetX, u.targetY);
        if (distToTarget <= u.speed) {
          // 强制吸附到目标点
          u.x = u.targetX;
          u.y = u.targetY;
          u.speedX = 0;
          u.speedY = 0;

          // 到达终点后，不再需要计算旋转，且防止后续误判
          u.velocityDirty = false;
        } else {
          // 正常移动
          u.x += u.speedX;
          u.y += u.speedY;
        }
      }

      // --- 2. 旋转逻辑 (核心修改) ---
      // 只有当 UnitMath 标记了方向改变(dirty)，且单位确实在运动时，才重新计算旋转
      if (u.velocityDirty && Mathf.len(u.speedX, u.speedY) > 0.01f) {

        // 计算移动方向 (耗时操作，现在仅在拐点执行一次)
        float moveAngle = Angles.angle(0, 0, u.speedX, u.speedY);

        // 更新朝向
        u.rotation = moveAngle - 90;

        // 由物理系统消费这个标记
        // 告诉 UnitMath 和 UnitProces：方向已经处理完毕，后续帧保持当前状态即可
        u.velocityDirty = false;
      }

      // --- 3. 网格更新 ---
      if (u.x != oldX || u.y != oldY) {
        updateChunkPosition(u);
      }
    }
  }

  private void updateChunkPosition(Unit u) {
    if (WorldData.unitGrid == null) return;

    int newIndex = WorldData.getChunkIndex(u.x, u.y);

    if (newIndex < 0 || newIndex >= WorldData.unitGrid.length) return;

    if (newIndex != u.currentChunkIndex) {
      if (u.currentChunkIndex != -1 && u.currentChunkIndex < WorldData.unitGrid.length) {
        // 由于 UnitProces 是唯一修改位置和网格归属的系统，
        // 且 WorldData.unitGrid 通常只被用于读取(点击检测)，
        // 这里不加锁通常是可行的。但如果出现并发修改异常，请在这里加 synchronized
        WorldData.unitGrid[u.currentChunkIndex].remove(u);
      }

      WorldData.unitGrid[newIndex].add(u);
      u.currentChunkIndex = newIndex;
    }
  }
}
