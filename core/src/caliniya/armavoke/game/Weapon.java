package caliniya.armavoke.game;

import arc.math.Angles;
import arc.math.Mathf;
import arc.util.Time;
import caliniya.armavoke.core.Units;
import caliniya.armavoke.game.type.WeaponType;

public class Weapon {
  public final WeaponType type;
  public final Unit owner; // 武器归属的单位

  // 动态状态
  public float rotation; // 当前朝向 (或者相对于单位)
  public float reloadTimer = 0f; // 冷却计时器
  public Unit target; // 当前锁定的目标

  public Weapon(WeaponType type, Unit owner) {
    this.type = type;
    this.owner = owner;
    this.rotation = 0f; // 初始朝正前
  }

  /** 每帧更新逻辑 */
  public void update() {
    // 冷却
    if (reloadTimer > 0) reloadTimer -= Time.delta;

    // 等待距离检测实现
    // 简单的验证逻辑：如果目标死了，或者跑远了，就置空
    if (target != null && (target.health <= 0)) {
      target = null;
    }

    // 如果没有目标，尝试寻找
    if (target == null) {
      target = Units.closestEnemy(owner.team, owner.x, owner.y, type.range);
    }

    // 瞄准
    if (target != null) {
      // 计算目标相对于单位中心的角度
      // 注意：武器有安装偏移 type.x/y，这里计算需要加上这个偏移
      // 为了简单，我们先假设武器在单位中心，或者计算稍微复杂点：

      // 武器的世界坐标
      // float wx = owner.x + Angles.trnsx(owner.rotation, type.x, type.y);
      // float wy = owner.y + Angles.trnsy(owner.rotation, type.x, type.y);

      // 直接计算单位到目标的角度，然后转动武器
      float targetAngle = Angles.angle(owner.x, owner.y, target.x, target.y);

      // 转换为相对于单位朝向的局部角度

      float targetMountAngle = targetAngle - owner.rotation - 90;

      // 平滑旋转
      this.rotation =
          Angles.moveToward(this.rotation, targetMountAngle, type.rotateSpeed * Time.delta);
    } else {
      // 没有目标时，慢慢回正 (回到 0 度，即正前方)
      this.rotation = Angles.moveToward(this.rotation, 0f, type.rotateSpeed * Time.delta);
    }
  }
}
