package caliniya.armavoke.game;

import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.pooling.Pool.Poolable;
import arc.util.pooling.Pools;
import caliniya.armavoke.game.data.WorldData;
import caliniya.armavoke.game.type.UnitType;

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

  // 选中状态 (用于绘制选中圈)
  public boolean isSelected = false;

  // 构造函数设为 protected，强迫使用 create 方法
  protected Unit() {}

  // 从池中获取实例的静态方法
  public static Unit create(UnitType type) {
    // 从 Arc 的全局池中获取一个空闲的 Unit 对象
    Unit u = Pools.obtain(Unit.class, Unit::new);
    u.init(type);
    return u;
  }

  // 初始化数据
  public void init(UnitType type) {
    this.type = type;
    this.health = type.health;
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

  /** 下达移动指令 */
  public void commandMoveTo(float tx, float ty) {
    this.targetX = tx;
    this.targetY = ty;
    this.isMoving = true;
  }

  /** 每帧更新单位物理逻辑 */
  public void updatePhysics() {
    if (!isMoving) return;

    // 计算到目标的距离
    float dist = Mathf.dst(x, y, targetX, targetY);

    // 如果距离非常近，就认为到达了，停止移动
    if (dist < 1f) {
      isMoving = false;
      return;
    }
    // 计算移动向量
    // 1. 计算角度
    float angle = Angles.angle(x, y, targetX, targetY);
    // 也可以直接更新单位朝向
    this.rotation = angle;

    // 2. 根据速度移动
    // speed 需要乘以 Time.delta (时间增量) 保证帧率无关性
    // 但 Arc 的 update 默认是定时的，简单起见先直接加
    float moveSpeed = type.speed; // * Time.delta;

    // 防止最后一步冲过头 (Overshoot)
    float moveDist = Math.min(dist, moveSpeed);
    // 3. 应用位移
    // 使用三角函数分解向量
    x += Mathf.cosDeg(angle) * moveDist;
    y += Mathf.sinDeg(angle) * moveDist;

    // 或者使用 Arc 的 Vec2 辅助 (虽然多一点计算但代码简洁)
    // Tmp.v1.set(targetX, targetY).sub(x, y).setLength(moveDist);
    // x += Tmp.v1.x;
    // y += Tmp.v1.y;
  }
  
  // 移除/销毁单位
  public void remove() {
    // 1. 从全局列表中移除 (见下文 Groups)
    WorldData.units.remove(this);
    isMoving = false;
    isSelected = false;
    // 2. 归还给池子
    Pools.free(this);
  }
}
