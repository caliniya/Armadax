package caliniya.armavoke.system.render;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import caliniya.armavoke.game.Unit;
import caliniya.armavoke.game.data.*;
import caliniya.armavoke.system.BasicSystem;

public class UnitRender extends BasicSystem<UnitRender> {

  @Override
  public UnitRender init() {
    // 确保渲染顺序：地图(10) -> 单位(15) -> UI(100)
    this.priority = 15;
    return super.init();
  }

  @Override
  public void update() {
    // 此时相机的矩阵已经由 Armavoke 主循环设置好了
    // 如果没有设置，可以在这里调用 Draw.proj(Core.camera);

    // 遍历所有单位
    for (int i = 0; i < WorldData.units.size; i++) {
      Unit u = WorldData.units.get(i);

      // 视锥剔除 (Culling):
      // 如果单位不在屏幕内，就不画它。
      // 我们用一个简单的矩形判断。AABB
      if (shouldDraw(u)) {
        drawUnit(u);
      }
    }
  }

  private boolean shouldDraw(Unit u) {
    // 获取相机可视范围 (增加一点 buffer 防止边缘消失)
    float viewX = Core.camera.position.x;
    float viewY = Core.camera.position.y;
    float w = Core.camera.width / 2f + u.type.w * 2;
    float h = Core.camera.height / 2f + u.type.h * 2;

    // 简单的 AABB 碰撞检测
    return u.x > viewX - w && u.x < viewX + w && u.y > viewY - h && u.y < viewY + h;
  }

  private void drawUnit(Unit u) {
    if (u.isSelected) {
      Draw.color(Color.green);
      Lines.circle(u.x, u.y, (u.w + u.h)/2 + 2);
      Draw.color();
    }
    TextureRegion reg = u.region;

    Draw.rect(reg, u.x, u.y, u.rotation);
  }
}
