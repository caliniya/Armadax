package caliniya.armavoke.system.world;

import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.util.Log;
import arc.util.Strings;
import caliniya.armavoke.base.tool.Ar;
import caliniya.armavoke.game.Unit;
import caliniya.armavoke.game.data.RouteData;
import caliniya.armavoke.game.data.WorldData;
import caliniya.armavoke.system.BasicSystem;

public class UnitMath extends BasicSystem<UnitMath> {

  private Ar<Unit> processList = new Ar<>();

  @Override
  public UnitMath init() {
    return super.init(true);
  }

  @Override
  public void update() {
    // 1. 获取数据快照
    processList.clear();
    synchronized (WorldData.moveunits) {
      processList.addAll(WorldData.moveunits);
    }

    // 2. 遍历处理每个单位
    for (int i = 0; i < processList.size; ++i) {
      Unit u = processList.get(i);
      
      // 基础检查
      if (u == null || u.health <= 0) continue;

      // -------------------------------------------------
      // 第一阶段：路径规划 (Heavy)
      // 如果还没路径，或者请求重新寻路，则计算整条路径
      // -------------------------------------------------
      if (!u.pathed) {
          calculatePath(u);
          u.pathed = true; // 标记已规划
      }

      // -------------------------------------------------
      // 第二阶段：路径跟随与速度计算 (Light)
      // 每一帧都计算，确保单位能沿着路径走
      // -------------------------------------------------
      calculateVelocity(u);
    }
    
  }

  /** 计算整条路径 (JPS) */
  private void calculatePath(Unit u) {
      int sx = (int) (u.x / WorldData.TILE_SIZE);
      int sy = (int) (u.y / WorldData.TILE_SIZE);
      int tx = (int) (u.targetX / WorldData.TILE_SIZE);
      int ty = (int) (u.targetY / WorldData.TILE_SIZE);

      // 起点终点不同才寻路
      if (sx != tx || sy != ty) {
          u.path = RouteData.findPath(sx, sy, tx, ty);
          u.pathIndex = 0; // 重置进度

          if (u.path == null || u.path.isEmpty()) {
              u.pathFindCooldown = 60f;
              // 寻路失败，让单位停下
              u.speedX = 0;
              u.speedY = 0;
          }
      } else {
          // 在同一个格子里，直接清空路径，交给 calculateVelocity 处理最后的微调
          if (u.path != null) u.path.clear();
      }
  }

  /** 计算当前帧的速度分量 (Path Following) */
  private void calculateVelocity(Unit u) {
      // 默认重置速度，如果后面逻辑没覆盖到，单位就会停下
      u.speedX = 0;
      u.speedY = 0;

      // 如果有路径且还没走完
      if (u.path != null && !u.path.isEmpty()) {
          if (u.pathIndex < u.path.size) {
              // --- 1. 获取当前目标节点 ---
              Point2 node = u.path.get(u.pathIndex);
              float nextX = node.x * WorldData.TILE_SIZE + WorldData.TILE_SIZE / 2f;
              float nextY = node.y * WorldData.TILE_SIZE + WorldData.TILE_SIZE / 2f;
              
              float dist = Mathf.dst(u.x, u.y, nextX, nextY);

              // --- 2. 判断是否到达节点 ---
              // 注意：这里不要直接修改 u.x = nextX (吸附)，
              // 因为这属于物理位置更新，最好留给主线程或者物理线程做。
              // 这里我们只负责算“这一帧该往哪飞”和“是不是该换目标了”。
              
              // 稍微放宽一点判定范围，或者如果速度很快，这里判定要大于速度
              if (dist <= u.speed) {
                  // 到达了！切换到下一个节点
                  u.pathIndex++; 
                  // 递归调用一次，立马计算去下一个节点的速度，
                  // 防止这一帧停顿 (如果不递归，这一帧速度就是0了)
                  calculateVelocity(u); 
                  return;
              }

              // --- 3. 计算去往节点的速度向量 ---
              float angle = Angles.angle(u.x, u.y, nextX, nextY);
              u.speedX = Mathf.cosDeg(angle) * u.speed;
              u.speedY = Mathf.sinDeg(angle) * u.speed;
              
              // 顺便算个角度给渲染用 (可选，也可以主线程算)
              // u.rotation = angle - 90; 

          } else {
              // --- 4. 路径走完了，去往最终 targetX/Y ---
              float distToFinal = Mathf.dst(u.x, u.y, u.targetX, u.targetY);
              
              if (distToFinal > u.speed) {
                  float angle = Angles.angle(u.x, u.y, u.targetX, u.targetY);
                  u.speedX = Mathf.cosDeg(angle) * u.speed;
                  u.speedY = Mathf.sinDeg(angle) * u.speed;
              } else {
                  // 彻底到达，速度保持 0
                  // 这里也可以把 u.path = null 清理掉
              }
          }
      }
  }
}