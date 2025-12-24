package caliniya.armavoke.system.world;

import arc.Core;
import arc.Events;
import arc.input.GestureDetector.GestureListener;
import arc.input.KeyCode;
import arc.input.InputProcessor;
import arc.math.geom.Vec2;
import caliniya.armavoke.base.tool.Ar;
import caliniya.armavoke.base.type.EventType;
import caliniya.armavoke.game.Unit;
import caliniya.armavoke.game.data.WorldData;
import caliniya.armavoke.system.BasicSystem;

public class UnitControl extends BasicSystem<UnitControl>
    implements InputProcessor, GestureListener {

  // 选中的单位列表
  public Ar<Unit> selectedUnits = new Ar<>(100);

  // 状态标志：是否处于指挥模式
  private boolean isCommandMode = false;

  // 之前定义的常量
  private static final float MAX_UNIT_HALF_SIZE = 64f;

  @Override
  public UnitControl init() {
    this.priority = 5;

    Events.on(
        EventType.CommandChange.class,
        event -> {
          this.isCommandMode = event.enabled;

          if (!this.isCommandMode) {
            // 如果关闭指挥模式，清空选中列表
            clearSelection();
          }
        });

    return super.init();
  }

  @Override
  public boolean tap(float x, float y, int count, KeyCode button) {
    // 基本检查：移动端且必须处于指挥模式
    if (!Core.app.isMobile()) return false;

    // 如果不处于指挥模式，不进行任何操作，返回 false
    if (!isCommandMode) return false;

    Vec2 worldPos = Core.camera.unproject(x, y);
    float wx = worldPos.x;
    float wy = worldPos.y;

    // 2. 尝试查找单位 (沿用之前的空间网格逻辑)
    Unit target = findUnitAt(wx, wy);

    if (target != null) {
      // --- 逻辑分支 A: 点击了单位 ---
      // 切换选中状态 (加入或移除)
      toggleUnitSelection(target);
      return true;
    } else {
      // --- 逻辑分支 B: 点击了空地 ---
      // 如果当前有选中的单位，给它们下达移动指令
      if (!selectedUnits.isEmpty()) {
        issueMoveCommand(wx, wy);
        return true; // 消耗事件，比如不让它触发地图拖动或放置建筑
      }
    }

    return false;
  }

  /** 切换单个单位的选中状态 */
  private void toggleUnitSelection(Unit u) {
    if (selectedUnits.contains(u)) {
      // 如果已经在列表中，移除它
      u.isSelected = false;
      selectedUnits.remove(u);
    } else {
      // 如果不在列表中，加入它
      u.isSelected = true;
      selectedUnits.add(u);
    }
  }

  /** 下达移动指令 */
  private void issueMoveCommand(float tx, float ty) {
    // 1. 获取地图的物理边界 (单位: 像素)
    // 假设地图从 (0,0) 开始，到 (W*32, H*32) 结束
    float mapMinX = 0;
    float mapMinY = 0;
    float mapMaxX = WorldData.world.W * WorldData.TILE_SIZE - 1; // 减1防止刚好压在边界导致数组越界
    float mapMaxY = WorldData.world.H * WorldData.TILE_SIZE - 1;

    // 2. 限制目标坐标在地图范围内 (Clamping)
    // 使用 arc.math.Mathf.clamp
    float clampedX = arc.math.Mathf.clamp(tx, mapMinX, mapMaxX);
    float clampedY = arc.math.Mathf.clamp(ty, mapMinY, mapMaxY);

    // 3. (可选优化) 检查目标点是否是墙壁
    // 如果点击了墙壁，单位通常应该走到离墙最近的空地，或者直接不走。
    // 这里简单的处理是：如果是墙，就不下达指令（或者你自己写一个搜索最近空地的方法）
    if (isSolidAtWorldPos(clampedX, clampedY)) {
      // 如果点到了墙里，暂时不做处理，或者让单位停下？
      // return;
    }

    for (int i = 0; i < selectedUnits.size; i++) {
      Unit u = selectedUnits.get(i);

      // 设定修正后的目标坐标
      u.targetX = clampedX;
      u.targetY = clampedY;

      synchronized (WorldData.moveunits) {
        // 防止重复添加：如果单位已经在列表里了，就别加了
        if (!WorldData.moveunits.contains(u)) {
          WorldData.moveunits.add(u);
        }
      }
      u.pathed = false; // 标记为尚未请求导航数据
    }
    // TODO: 做个音效
  }

  // 辅助方法：判断世界像素坐标是否是障碍物
  private boolean isSolidAtWorldPos(float wx, float wy) {
    int gx = (int) (wx / WorldData.TILE_SIZE);
    int gy = (int) (wy / WorldData.TILE_SIZE);
    return WorldData.world.isSolid(gx, gy);
  }

  /** 清空所有选中状态 */
  private void clearSelection() {
    for (int i = 0; i < selectedUnits.size; i++) {
      selectedUnits.get(i).isSelected = false;
    }
    selectedUnits.clear();
  }

  // --- 下面是之前的检测逻辑 (保持不变) ---

  private boolean isPointInUnit(Unit unit, float px, float py) {
    if (unit == null || unit.type == null) return false;
    float halfW = unit.w / 2f;
    float halfH = unit.h / 2f;
    return Math.abs(unit.x - px) <= halfW && Math.abs(unit.y - py) <= halfH;
  }

  private Unit findUnitAt(float wx, float wy) {
    int cx = (int) (wx / WorldData.CHUNK_PIXEL_SIZE);
    int cy = (int) (wy / WorldData.CHUNK_PIXEL_SIZE);

    Unit found = searchInChunk(cx, cy, wx, wy);
    if (found != null) return found;

    float localX = wx % WorldData.CHUNK_PIXEL_SIZE;
    float localY = wy % WorldData.CHUNK_PIXEL_SIZE;
    if (localX < 0) localX += WorldData.CHUNK_PIXEL_SIZE;
    if (localY < 0) localY += WorldData.CHUNK_PIXEL_SIZE;

    if (localX < MAX_UNIT_HALF_SIZE) found = searchInChunk(cx - 1, cy, wx, wy);
    else if (localX > WorldData.CHUNK_PIXEL_SIZE - MAX_UNIT_HALF_SIZE)
      found = searchInChunk(cx + 1, cy, wx, wy);
    if (found != null) return found;

    if (localY < MAX_UNIT_HALF_SIZE) found = searchInChunk(cx, cy - 1, wx, wy);
    else if (localY > WorldData.CHUNK_PIXEL_SIZE - MAX_UNIT_HALF_SIZE)
      found = searchInChunk(cx, cy + 1, wx, wy);

    return found;
  }

  private Unit searchInChunk(int cx, int cy, float wx, float wy) {
    if (cx < 0 || cx >= WorldData.gridW || cy < 0 || cy >= WorldData.gridH) return null;
    if (WorldData.unitGrid == null) return null;
    int index = cy * WorldData.gridW + cx;
    Ar<Unit> list = WorldData.unitGrid[index];
    if (list == null || list.isEmpty()) return null;

    for (int i = list.size - 1; i >= 0; i--) {
      Unit u = list.get(i);
      if (isPointInUnit(u, wx, wy)) return u;
    }
    return null;
  }

  @Override
  public void update() {}

  @Override
  public boolean touchDown(int x, int y, int p, KeyCode b) {
    return false;
  }

  @Override
  public boolean pinch(Vec2 i1, Vec2 i2, Vec2 p1, Vec2 p2) {
    return false;
  }

  @Override
  public boolean longPress(float x, float y) {
    return false;
  }

  @Override
  public boolean fling(float vx, float vy, KeyCode button) {
    return false;
  }

  @Override
  public boolean pan(float x, float y, float dx, float dy) {
    return false;
  }

  @Override
  public boolean panStop(float x, float y, int pointer, KeyCode button) {
    return false;
  }

  @Override
  public boolean zoom(float initialDistance, float distance) {
    return false;
  }

  @Override
  public boolean touchDown(float x, float y, int pointer, KeyCode button) {
    return false;
  }

  @Override
  public boolean keyDown(KeyCode key) {
    return false;
  }

  @Override
  public boolean keyUp(KeyCode key) {
    return false;
  }

  @Override
  public boolean keyTyped(char character) {
    return false;
  }

  @Override
  public boolean touchUp(int screenX, int screenY, int pointer, KeyCode button) {
    return false;
  }

  @Override
  public boolean touchDragged(int screenX, int screenY, int pointer) {
    return false;
  }

  @Override
  public boolean mouseMoved(int screenX, int screenY) {
    return false;
  }

  @Override
  public boolean scrolled(float amountX, float amountY) {
    return false;
  }
}
