package caliniya.armavoke.game.data;

import arc.math.Mathf;
import caliniya.armavoke.base.tool.Ar;
import caliniya.armavoke.base.type.TeamTypes;
import caliniya.armavoke.game.Unit;

public class TeamData {
  public final TeamTypes team;

  // 该团队下的所有单位列表 (全局)
  public Ar<Unit> units = new Ar<>();

  //空间划分网格 (Per-Team Spatial Grid)
  // 每个格子存储该区域内属于本团队的单位
  public Ar<Unit>[] unitGrid;

  @SuppressWarnings("unchecked")
  public TeamData(TeamTypes team) {
    this.team = team;
    initGrid();
  }

  /** 初始化/重置网格 (需在地图加载后调用) */
  @SuppressWarnings("unchecked")
  public void initGrid() {
    int w = WorldData.gridW;
    int h = WorldData.gridH;
    int total = w * h;

    this.unitGrid = new Ar[total];
    for (int i = 0; i < total; i++) {
      // 预设容量小一点，因为单个团队在单个格子的单位数通常比全局少
      this.unitGrid[i] = new Ar<>(8);
    }
  }

  /**
   * 获取指定矩形区域内的本团队单位
   *
   * @param minX 左下角 X (世界像素坐标)
   * @param minY 左下角 Y
   * @param maxX 右上角 X
   * @param maxY 右上角 Y
   * @param output 结果将存入此列表 (为了减少GC，传入一个可重用的列表)
   */
  public void get(float minX, float minY, float maxX, float maxY, Ar<Unit> output) {
    if (unitGrid == null) return;

    // 1. 将像素坐标转换为区块索引范围
    int startX = (int) (minX / WorldData.CHUNK_PIXEL_SIZE);
    int startY = (int) (minY / WorldData.CHUNK_PIXEL_SIZE);
    int endX = (int) (maxX / WorldData.CHUNK_PIXEL_SIZE);
    int endY = (int) (maxY / WorldData.CHUNK_PIXEL_SIZE);

    // 2. 边界限制
    startX = Mathf.clamp(startX, 0, WorldData.gridW - 1);
    startY = Mathf.clamp(startY, 0, WorldData.gridH - 1);
    endX = Mathf.clamp(endX, 0, WorldData.gridW - 1);
    endY = Mathf.clamp(endY, 0, WorldData.gridH - 1);

    // 3. 遍历覆盖的区块
    for (int y = startY; y <= endY; y++) {
      for (int x = startX; x <= endX; x++) {
        int index = y * WorldData.gridW + x;
        Ar<Unit> chunkUnits = unitGrid[index];

        // 4. 精确检测 (可选)
        // 如果只想要粗略结果(区块内所有单位)，直接 addAll
        // 如果需要精确矩形裁剪，则遍历 chunkUnits 检查 u.x/u.y

        // 这里我们做简单矩形判断
        for (int i = 0; i < chunkUnits.size; i++) {
          Unit u = chunkUnits.get(i);
          // 考虑单位半径，这里简单用点判断，或者用 AABB 判断
          // u.x > minX - u.radius ...
          if (u.x >= minX && u.x <= maxX && u.y >= minY && u.y <= maxY) {
            output.add(u);
          }
        }
      }
    }
  }

  // --- 网格更新方法 ---
  // 这些方法应该由 UnitProces.updateChunkPosition 调用
  // 逻辑和 WorldData.unitGrid 一模一样

  /**
   * 更新单位在团队空间网格中的位置
   * @param u 单位对象
   * @param oldIndex 旧的区块索引 (如果刚生成则为 -1)
   * @param newIndex 新的区块索引
   */
  public void updateChunk(Unit u, int oldIndex, int newIndex) {
    if (unitGrid == null) return;

    // 1. 从旧区块移除
    if (oldIndex != -1 && oldIndex < unitGrid.length) {
      unitGrid[oldIndex].remove(u);
    }

    // 2. 加入新区块
    if (newIndex >= 0 && newIndex < unitGrid.length) {
      unitGrid[newIndex].add(u);
    }
  }
}
