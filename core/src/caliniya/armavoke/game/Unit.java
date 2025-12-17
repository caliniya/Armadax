package caliniya.armavoke.game;

import arc.graphics.g2d.TextureRegion;
import arc.math.Rand;
import arc.util.Log;
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

  // --- 物理属性 ---
  public float x, y;
  public float speedX, speedY; // 速度分量 (每帧移动的像素量)
  public float rotation; // 渲染朝向 (度)

  // --- 导航属性 ---
  public float targetX, targetY;
  public Ar<Point2> path;
  public int pathIndex = 0;
  public boolean pathed; // 真则当前已经请求过一次导航数据

  // --- 状态属性 ---
  public boolean isSelected = false;
  public float health, w, h, speed; // speed 这里指最大标量速度
  public TextureRegion region;
  public int currentChunkIndex = -1;
  public float pathFindCooldown = 0f;

  protected Unit() {}

  public static Unit create(UnitType type) {
    Unit u = Pools.obtain(Unit.class, Unit::new);
    u.init(type);
    return u;
  }

  public void init(UnitType type) {
    this.type = type;
    this.w = type.w;
    this.h = type.h;
    this.speed = type.speed;
    this.region = type.region;
    this.health = type.health;

    this.rotation = 0f;
    this.speedX = 0f;
    this.speedY = 0f;
    this.id = (new Rand().random(10000));

    // 初始化目标为当前位置，防止刚出生就归零
    this.targetX = 50f;
    this.targetY = 50f;

    // 加入世界列表
    WorldData.units.add(this);

    // 立即更新一次网格位置，确保出生就能被点中
    updateChunkPosition();
  }

  @Override
  public void reset() {
    this.type = null;
    this.x = 0;
    this.y = 0;
    this.speedX = 0;
    this.speedY = 0;
    this.targetX = 0;
    this.targetY = 0;
    this.rotation = 0;
    this.health = 0;
    this.id = -1;

    this.currentChunkIndex = -1;
    this.isSelected = false;
    this.pathFindCooldown = 0;
    if (path != null) path.clear();
  }

  /** 更新单位在网格中的位置 */
  private void updateChunkPosition() {
    if (WorldData.unitGrid == null) return;

    int newIndex = WorldData.getChunkIndex(x, y);

    if (newIndex < 0 || newIndex >= WorldData.unitGrid.length) return;

    if (newIndex != currentChunkIndex) {
      if (currentChunkIndex != -1 && currentChunkIndex < WorldData.unitGrid.length) {
        WorldData.unitGrid[currentChunkIndex].remove(this);
      }
      WorldData.unitGrid[newIndex].add(this);
      currentChunkIndex = newIndex;
    }
  }

  public void remove() {
    WorldData.units.remove(this);
    if (currentChunkIndex != -1
        && WorldData.unitGrid != null
        && currentChunkIndex < WorldData.unitGrid.length) {
      WorldData.unitGrid[currentChunkIndex].remove(this);
    }
    isSelected = false;
    currentChunkIndex = -1;
    Pools.free(this);
  }

  public void update() {
  }
}
