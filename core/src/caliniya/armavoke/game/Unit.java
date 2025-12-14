package caliniya.armavoke.game;

import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.pooling.Pool.Poolable;
import arc.util.pooling.Pools;
import caliniya.armavoke.base.tool.Ar;
import caliniya.armavoke.game.data.WorldData;
import caliniya.armavoke.game.type.UnitType;
import arc.math.geom.Point2;
import caliniya.armavoke.system.render.MapRender; // 用于获取 TILE_SIZE

// 实现 Poolable 接口，以便复用
public class Unit implements Poolable {

  // 核心数据
  public UnitType type;
  public int id; // 唯一ID

  // 物理属性 (尽量直接用 float 而不是 Vec2 对象，减少内存开销)
  public float x, y;
  public float rotation;
  public float health;

  public float targetX, targetY; // 目标位置
  public boolean isMoving = false; // 是否正在移动

  // 寻路相关字段
  private Ar<Point2> path; // 存储计算出的路径
  private int pathIndex; // 当前路径的目标点索引

  // 选中状态 (用于绘制选中圈)
  public boolean isSelected = false;

  // 构造函数设为 protected，强迫使用 create 方法
  protected Unit() {}

  // 从池中获取实例的静态方法
  public static Unit create(UnitType type) {
    Unit u = Pools.obtain(Unit.class, Unit::new);
    u.init(type);
    return u;
  }

  // 初始化数据
  public void init(UnitType type) {
    this.type = type;
    this.rotation = 90f;
    // 分配一个唯一ID (简单实现)
    this.id = (int) (Math.random() * 100000);
  }

  // 当对象被回收回池子时调用
  @Override
  public void reset() {
    this.type = null;
    this.x = 0;
    this.y = 0;
    this.rotation = 0;
    this.health = 0;
    this.id = -1;
  }

  /** 每帧更新单位物理逻辑 */
  public void updatePhysics() {
    if (!isMoving) return;
  }

  // 移除/销毁单位
  public void remove() {
    // 1. 从全局列表中移除
    WorldData.units.remove(this);
    isMoving = false;
    isSelected = false;
    // 2. 归还给池子
    Pools.free(this);
  }
}
