package caliniya.armavoke.game;

import arc.math.Rand;
import arc.util.pooling.Pool.Poolable;
import arc.util.pooling.Pools;
import caliniya.armavoke.base.tool.Ar;
import caliniya.armavoke.game.data.WorldData;
import caliniya.armavoke.game.type.UnitType;
import arc.math.geom.Point2;
import arc.math.Mathf;
import arc.math.Angles;
import caliniya.armavoke.game.data.RouteData;

public class Unit implements Poolable {

  public UnitType type;
  public int id;
  public float x, y, floorX, floorY;
  public float rotation;
  public float health;
  public float targetX, targetY;
  private Ar<Point2> path; // 假设你的Ar是Arc的Array
  public int pathIndex = 1;

  public boolean isSelected = false;

  // --- 新增字段：记录当前所在的区块索引 ---
  // -1 表示未加入任何区块
  private int currentChunkIndex = -1;

  protected Unit() {}

  public static Unit create(UnitType type) {
    Unit u = Pools.obtain(Unit.class, Unit::new);
    u.init(type);
    return u;
  }

  public void init(UnitType type) {
    this.type = type;
    this.rotation = 0f;
    this.id = (new Rand().random(10000));

    // 初始化时加入全局列表
    WorldData.units.add(this);

    // 必须立即更新一次区块位置，否则刚出生时无法被点中
    updateChunkPosition();
  }

  @Override
  public void reset() {
    this.type = null;
    this.x = 0;
    this.y = 0;
    this.currentChunkIndex = -1; // 重置索引
    this.isSelected = false;
    // ...其他重置
  }

  public void update() {
    // ... 你的移动逻辑 ...
        // 1. 之前写的：更新所在区块逻辑
    updateChunkPosition();

    // 2. 【新增】测试寻路逻辑
    debugPathfinding();
  }

  /** 核心逻辑：更新单位在网格中的位置 */
  private void updateChunkPosition() {
    // 计算当前坐标应该在哪个区块
    int newIndex = WorldData.getChunkIndex(x, y);

    // 如果区块变了 (或者刚出生 currentChunkIndex 为 -1)
    if (newIndex != currentChunkIndex) {
      // 1. 如果之前在某个区块里，先移出
      if (currentChunkIndex != -1) {
        WorldData.unitGrid[currentChunkIndex].remove(this);
      }

      // 2. 加入新区块
      WorldData.unitGrid[newIndex].add(this);

      // 3. 更新索引记录
      currentChunkIndex = newIndex;
    }
  }

  public void remove() {
    // 1. 从全局列表移除
    WorldData.units.remove(this);

    // 2. 从空间网格中移除
    if (currentChunkIndex != -1) {
      WorldData.unitGrid[currentChunkIndex].remove(this);
    }

    isSelected = false;
    currentChunkIndex = -1;

    Pools.free(this);
  }

  // --- 测试用的临时变量 ---
  // 用于防止寻路失败时每一帧都请求路径导致掉帧
  private float pathFindCooldown = 0f;


  /** 测试用的临时寻路移动逻辑 测试完毕后可直接删除此方法 */
  public void debugPathfinding() {
    // 如果没有设定目标（比如目标是 0,0），或者目标就在脚下，就不动
    if ((targetX == 0 && targetY == 0) || Mathf.dst(x, y, targetX, targetY) < 1f) {
      return;
    }

    // 1. 请求路径
    // 如果当前没有路径，或者路径走完了，尝试请求新路径
    if ((path == null || path.isEmpty()) && pathFindCooldown <= 0) {
      // 将像素坐标转换为网格坐标
      int sx = (int) (x / WorldData.TILE_SIZE);
      int sy = (int) (y / WorldData.TILE_SIZE);
      int tx = (int) (targetX / WorldData.TILE_SIZE);
      int ty = (int) (targetY / WorldData.TILE_SIZE);

      // 如果起点和终点不在同一个格子里，才寻路
      if (sx != tx || sy != ty) {
        // 调用我们刚才写的 RouteData
        this.path = RouteData.findPath(sx, sy, tx, ty);
        this.pathIndex = 0;

        // 如果没找到路径（path为空），设置一个冷却时间（比如60帧），避免每帧都疯狂计算
        if (this.path == null || this.path.isEmpty()) {
          pathFindCooldown = 60f;
          // System.out.println("寻路失败: 无法到达目标");
        } else {
          // System.out.println("寻路成功: 节点数 " + path.size);
        }
      }
    }

    if (pathFindCooldown > 0) pathFindCooldown--;

    // 2. 沿着路径移动
    if (path != null && !path.isEmpty()) {
      // 检查索引是否有效
      if (pathIndex < path.size) {
        // 获取当前要去的路径节点
        Point2 node = path.get(pathIndex);

        // 【重要】将网格坐标转回像素坐标中心
        // 假设格子是 32x32，中心就是 +16
        float nextX = node.x * WorldData.TILE_SIZE + WorldData.TILE_SIZE / 2f;
        float nextY = node.y * WorldData.TILE_SIZE + WorldData.TILE_SIZE / 2f;

        // 计算距离
        float dist = Mathf.dst(x, y, nextX, nextY);

        // 获取移动速度，假设 UnitType 里有 speed 字段，如果没有请手动写一个数字比如 3f
        float moveSpeed = (type != null) ? type.speed : 3f;

        if (dist <= moveSpeed) {
          // 如果距离非常近，直接“吸附”过去，并把目标切换到下一个点
          x = nextX;
          y = nextY;
          pathIndex++;
        } else {
          // 否则，向目标方向移动
          float angle = Angles.angle(x, y, nextX, nextY);
          this.rotation = angle - 90
          ; // 设置单位朝向

          x += Mathf.cosDeg(angle) * moveSpeed;
          y += Mathf.sinDeg(angle) * moveSpeed;
        }
      } else {
        // 3. 路径走完了
        path.clear();
        // 稍微修正最后的位置到精确的 targetX/Y (可选)
        // x = targetX;
        // y = targetY;
      }
    }
  }
}
