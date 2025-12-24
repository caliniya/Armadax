package caliniya.armavoke.system.world;

import arc.math.Angles;
import arc.math.Mathf;
import arc.util.Log;
import caliniya.armavoke.base.tool.Ar;
import caliniya.armavoke.game.Unit;
import caliniya.armavoke.game.data.WorldData;
import caliniya.armavoke.system.BasicSystem;

// 单位移动控制和单位物理处理
// 后台线程运行
public class UnitProces extends BasicSystem<UnitProces> {

  // 本地快照列表，防止多线程遍历时 WorldData.units 被主线程修改导致崩溃
  private Ar<Unit> processList = new Ar<>();

  @Override
  public UnitProces init() {
    // 开启后台线程，
    return super.init(true);
  }

  @Override
  public void update() {
    // 1. 线程安全：获取数据快照
    // 必须锁住源列表，快速拷贝引用，然后释放锁
    processList.clear();
    synchronized (WorldData.units) {
      processList.addAll(WorldData.units);
    }

    // 2. 遍历快照进行物理计算
    for (int i = 0; i < processList.size; i++) {
      Unit u = processList.get(i);

      // 基础检查：防止处理已死亡单位
      if (u == null || u.health <= 0) continue;

      // 记录旧位置，用于判断是否需要更新网格
      float oldX = u.x;
      float oldY = u.y;

      // 标记：这一帧是否发生了实际位移
      boolean hasMoved = false;

      // --- 物理位置更新 ---

      float distToTarget = Mathf.dst(u.x, u.y, u.targetX, u.targetY);

      // 判定：是否进入终点吸附范围
      if (distToTarget <= u.speed) {
        // 【吸附逻辑】
        // 直接瞬移到目标，并将速度归零
        u.x = u.targetX;
        u.y = u.targetY;
        u.speedX = 0;
        u.speedY = 0;

        // 既然已经强行停止，这就视为"没有发生需要改变朝向的位移"
        // 这样可以保证单位停下后，朝向死死锁住在最后那一帧的方向，绝不抖动
        hasMoved = false;
      } else {
        // 【正常移动】
        u.x += u.speedX;
        u.y += u.speedY;
        hasMoved = true;
      }

      // --- 旋转更新 ---

      // 只有在"正常移动"且"速度分量足够大"时才更新朝向
      // 使用 len2() (长度平方) > 0.01 (即长度>0.1) 来避免 sqrt 开方运算，提升性能
      if (hasMoved && Mathf.len2(u.speedX, u.speedY) > 0.01f) {
        // 计算移动方向
        float moveAngle = Angles.angle(0, 0, u.speedX, u.speedY);

        // 设置朝向 (纹理向上默认为90度，所以减90)
        // 由于 UnitMath 保证了 speedX/Y 在节点间是常数，这里的 moveAngle 也是绝对常数
        u.rotation = moveAngle - 90;
        Log.info(u.rotation + "AAA" + u.speedX + "AAA" + u.speedY);
      }

      // --- 空间网格更新 ---

      // 只有位置发生变化时才更新网格
      if (u.x != oldX || u.y != oldY) {
        updateChunkPosition(u);
      }
    }
  }

  /** 更新空间划分网格 */
  private void updateChunkPosition(Unit u) {
    if (WorldData.unitGrid == null) return;

    int newIndex = WorldData.getChunkIndex(u.x, u.y);

    // 索引越界检查
    if (newIndex < 0 || newIndex >= WorldData.unitGrid.length) return;

    if (newIndex != u.currentChunkIndex) {
      // 移除旧引用
      if (u.currentChunkIndex != -1 && u.currentChunkIndex < WorldData.unitGrid.length) {
        // 注意：unitGrid 的各个 Ar<Unit> 也不是线程安全的
        // 如果有其他系统（如渲染、点击检测）同时在遍历这个 grid 列表，这里需要加锁
        synchronized (WorldData.unitGrid[u.currentChunkIndex]) {
          WorldData.unitGrid[u.currentChunkIndex].remove(u);
        }
      }

      // 添加新引用
      synchronized (WorldData.unitGrid[newIndex]) {
        WorldData.unitGrid[newIndex].add(u);
      }

      // 更新记录
      u.currentChunkIndex = newIndex;
    }
  }
}
