package caliniya.armavoke.system.world;

import arc.math.Angles;
import arc.math.Mathf;
import caliniya.armavoke.base.tool.Ar;
import caliniya.armavoke.game.Unit;
import caliniya.armavoke.game.data.WorldData;
import caliniya.armavoke.system.BasicSystem;

// 单位移动控制和单位物理处理
// 在后台运行
public class UnitProces extends BasicSystem<UnitProces> {

  @Override
  public UnitProces init() {
    //尝试以60的帧率更新(也就是默认频率)
    return super.init(true);
  }

  @Override
  public void update() {
    // 遍历所有单位
    // 通常物理积分是对所有单位生效的
    Ar<Unit> list = WorldData.units;

    for (int i = 0; i < list.size; i++) {
      Unit u = list.get(i);

      if (u == null || u.health <= 0) continue;

      float oldX = u.x;
      float oldY = u.y;

      // 检查是否到达终点附近的吸附范围
      // 这里的逻辑是为了配合 UnitMath 计算出的 0 速度
      // 如果 UnitMath 算出来还没到终点，speedX/Y 会有值

      // 计算离最终目标的距离 (用于最后一步的完美停止)
      float distToTarget = Mathf.dst(u.x, u.y, u.targetX, u.targetY);

      // 如果距离非常近，且速度分量也很小（说明 UnitMath 认为该停了或者到了）
      if (distToTarget <= u.speed ){
        // 强制吸附到目标点，消除最后 0.5 像素的误差
        u.x = u.targetX;
        u.y = u.targetY;
        u.speedX = 0;
        u.speedY = 0;
      } else {
        // 正常移动
        u.x += u.speedX;
        u.y += u.speedY;
      }

      // 只有当单位真的在动时才更新朝向，防止停止时抖动或归零
      if (Mathf.len(u.speedX, u.speedY) > 0.1f) {
        // 计算移动方向
        float moveAngle = Angles.angle(0, 0, u.speedX, u.speedY);
        // 设置朝向 (纹理向上，所以 -90)
        // 这里可以使用 Lerp 实现平滑旋转
        // 暂时先不考虑
        // u.rotation = Angles.moveToward(u.rotation, moveAngle - 90, 10f);
        u.rotation = moveAngle - 90;
      }

      // 只有位置发生变化时才更新网格，节省性能
      if (u.x != oldX || u.y != oldY) {
        updateChunkPosition(u);
      }
    }
  }

  private void updateChunkPosition(Unit u) {
    if (WorldData.unitGrid == null) return;

    int newIndex = WorldData.getChunkIndex(u.x, u.y);

    // 索引越界检查
    if (newIndex < 0 || newIndex >= WorldData.unitGrid.length) return;

    if (newIndex != u.currentChunkIndex) {
      // 从旧格子移除
      if (u.currentChunkIndex != -1 && u.currentChunkIndex < WorldData.unitGrid.length) {
        // 这里不需要考虑线程安全
        // 只有这个系统在修改，安全的。
        WorldData.unitGrid[u.currentChunkIndex].remove(u);
      }

      // 加入新格子
      WorldData.unitGrid[newIndex].add(u);

      // 更新记录
      u.currentChunkIndex = newIndex;
    }
  }
}
