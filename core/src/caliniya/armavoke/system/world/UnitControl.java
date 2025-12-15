package caliniya.armavoke.system.world;

import arc.Core;
import arc.Events; // 引入事件系统
import arc.input.GestureDetector.GestureListener;
import arc.input.KeyCode;
import arc.input.InputProcessor;
import arc.math.geom.Vec2;
import caliniya.armavoke.base.tool.Ar;
import caliniya.armavoke.base.type.EventType; // 引入事件类型
import caliniya.armavoke.game.Unit;
import caliniya.armavoke.game.data.WorldData;
import caliniya.armavoke.system.BasicSystem;

public class UnitControl extends BasicSystem<UnitControl> implements InputProcessor, GestureListener {
    
    // 选中的单位列表
    public Ar<Unit> selectedUnits = new Ar<>(100);
    
    // 状态标志：是否处于指挥模式
    private boolean isCommandMode = false;

    // 之前定义的常量
    private static final float MAX_UNIT_HALF_SIZE = 64f; 

    @Override
    public UnitControl init() {
        this.priority = 5;

        // 【注册监听器】监听指挥模式切换事件
        Events.on(EventType.CommandChange.class, event -> {
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
        // 1. 基本检查：移动端且必须处于指挥模式
        if (!Core.app.isMobile()) return false;
        
        // 【关键】如果不处于指挥模式，不进行任何操作，返回 false
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
        for (int i = 0; i < selectedUnits.size; i++) {
            Unit u = selectedUnits.get(i);
            // 设定目标坐标
            // 注意：具体的寻路逻辑由你的导航系统在 Unit.update() 或其他地方处理
            u.targetX = tx;
            u.targetY = ty;
        }
        // 可选：在这里播放一个"收到指令"的音效或特效
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
        float halfW = unit.type.w / 2f;
        float halfH = unit.type.h / 2f;
        return Math.abs(unit.x - px) <= halfW && Math.abs(unit.y - py) <= halfH;
    }

    private Unit findUnitAt(float wx, float wy) {
        int cx = (int)(wx / WorldData.CHUNK_PIXEL_SIZE);
        int cy = (int)(wy / WorldData.CHUNK_PIXEL_SIZE);

        Unit found = searchInChunk(cx, cy, wx, wy);
        if (found != null) return found;

        float localX = wx % WorldData.CHUNK_PIXEL_SIZE;
        float localY = wy % WorldData.CHUNK_PIXEL_SIZE;
        if(localX < 0) localX += WorldData.CHUNK_PIXEL_SIZE;
        if(localY < 0) localY += WorldData.CHUNK_PIXEL_SIZE;

        if (localX < MAX_UNIT_HALF_SIZE) found = searchInChunk(cx - 1, cy, wx, wy);
        else if (localX > WorldData.CHUNK_PIXEL_SIZE - MAX_UNIT_HALF_SIZE) found = searchInChunk(cx + 1, cy, wx, wy);
        if (found != null) return found;

        if (localY < MAX_UNIT_HALF_SIZE) found = searchInChunk(cx, cy - 1, wx, wy);
        else if (localY > WorldData.CHUNK_PIXEL_SIZE - MAX_UNIT_HALF_SIZE) found = searchInChunk(cx, cy + 1, wx, wy);
        
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

    // InputProcessor 其他方法...
    @Override public void update() {}
    @Override public boolean touchDown(int x, int y, int p, KeyCode b) { return false; }
    @Override public boolean pinch(Vec2 i1, Vec2 i2, Vec2 p1, Vec2 p2) { return false; }
    @Override public boolean longPress(float x, float y) { return false; }
    @Override public boolean fling(float vx, float vy, KeyCode button) { return false; }
    @Override public boolean pan(float x, float y, float dx, float dy) { return false; }
    @Override public boolean panStop(float x, float y, int pointer, KeyCode button) { return false; }
    @Override public boolean zoom(float initialDistance, float distance) { return false; }
    @Override public boolean touchDown(float x, float y, int pointer, KeyCode button) { return false; }
    @Override public boolean keyDown(KeyCode key) { return false; }
    @Override public boolean keyUp(KeyCode key) { return false; }
    @Override public boolean keyTyped(char character) { return false; }
    @Override public boolean touchUp(int screenX, int screenY, int pointer, KeyCode button) { return false; }
    @Override public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
    @Override public boolean mouseMoved(int screenX, int screenY) { return false; }
    @Override public boolean scrolled(float amountX, float amountY) { return false; }
}