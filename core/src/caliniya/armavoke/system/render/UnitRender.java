package caliniya.armavoke.system.render;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Point2;
import caliniya.armavoke.game.Unit;
import caliniya.armavoke.game.data.WorldData;
import caliniya.armavoke.system.BasicSystem;

public class UnitRender extends BasicSystem<UnitRender> {

  // 调试开关，可以通过控制台或UI切换它
  public static boolean debug = true;

  @Override
  public UnitRender init() {
    this.priority = 15;
    return super.init();
  }

  @Override
  public void update() {
    for (int i = 0; i < WorldData.units.size; i++) {
      Unit u = WorldData.units.get(i);
      if (shouldDraw(u)) {
        drawUnit(u);
        
        // 如果开启调试，绘制额外信息
        if (debug) {
            drawDebug(u);
        }
      }
    }
  }

  private boolean shouldDraw(Unit u) {
    float viewX = Core.camera.position.x;
    float viewY = Core.camera.position.y;
    // 如果开启调试，扩大一点视锥剔除范围，防止连线突然消失
    float buffer = debug ? 500f : (u.type.w * 2);
    float w = Core.camera.width / 2f + buffer;
    float h = Core.camera.height / 2f + buffer;

    return u.x > viewX - w && u.x < viewX + w && u.y > viewY - h && u.y < viewY + h;
  }

  private void drawUnit(Unit u) {
    // 选中状态圈
    if (u.isSelected) {
      Draw.color(Color.green);
      Lines.stroke(2f);
      // 绘制虚线圈更有质感 (可选)
      Lines.circle(u.x, u.y, (u.w + u.h)/2 + 4);
      Draw.color();
    }
    
    TextureRegion reg = u.region;
    if(reg != null) {
        Draw.rect(reg, u.x, u.y, u.rotation);
    }
  }

  /** 绘制调试信息 */
  private void drawDebug(Unit u) {
      // 1. 绘制碰撞体积 (矩形)
      Draw.color(Color.yellow);
      Lines.stroke(3f);
      // Lines.rect 默认可能是左下角，这里我们需要绘制以 u.x, u.y 为中心的矩形
      // 假设 u.w 和 u.h 是全宽全高
      Lines.rect(u.x - u.w / 2f, u.y - u.h / 2f, u.w, u.h);

      // 2. 绘制目标点连接线 (从单位中心到目标点)
      // 仅当目标点不在原点或单位附近时绘制
      if (u.targetX != 0 || u.targetY != 0) {
          Draw.color(Color.orange);
          Lines.line(u.x, u.y, u.targetX, u.targetY);
          // 绘制目标点的一个小叉叉
          float s = 8f;
          Lines.line(u.targetX - s, u.targetY - s, u.targetX + s, u.targetY + s);
          Lines.line(u.targetX - s, u.targetY + s, u.targetX + s, u.targetY - s);
      }

      // 3. 绘制寻路路径 (Path)
      if (u.path != null && !u.path.isEmpty()) {
          Draw.color(Color.cyan);
          
          float lastX = u.x;
          float lastY = u.y;
          
          // 从当前路径索引开始绘制
          for (int i = u.pathIndex; i < u.path.size; i++) {
              Point2 p = u.path.get(i);
              // 转换网格坐标到世界中心坐标
              float wx = p.x * WorldData.TILE_SIZE + WorldData.TILE_SIZE / 2f;
              float wy = p.y * WorldData.TILE_SIZE + WorldData.TILE_SIZE / 2f;
              
              // 绘制连线
              Lines.line(lastX, lastY, wx, wy);
              
              // 绘制节点小方块
              Fill.square(wx, wy, 3f);
              lastX = wx;
              lastY = wy;
          }
          
      }

      Draw.color(); // 重置颜色
  }
}