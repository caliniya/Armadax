package caliniya.armavoke.system.world;

import arc.Core;
import arc.input.GestureDetector; // 需要导入
import arc.input.GestureDetector.GestureListener; // 需要导入
import arc.input.KeyCode;
import arc.input.InputProcessor;
import arc.math.geom.Vec2;
import caliniya.armavoke.game.Unit;
import caliniya.armavoke.game.data.WorldData;
import caliniya.armavoke.system.BasicSystem;

// 1. 实现 GestureListener 接口
public class UnitControl extends BasicSystem<UnitControl> implements InputProcessor, GestureListener {

    private Unit selectedUnit = null;

    @Override
    public UnitControl init() {
        this.priority = 5;
        return super.init();
    }

    @Override
    public void update() {
        for (int i = 0; i < WorldData.units.size; i++) {
            Unit u = WorldData.units.get(i);
            u.updatePhysics();
        }
    }

    // =========================================================
    // PC 端逻辑 (鼠标操作) - 使用 InputProcessor
    // =========================================================
    
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, KeyCode button) {
        // 如果是移动端，忽略 touchDown，交给 tap 处理 (防止误触和阻碍拖动地图)
        if (Core.app.isMobile()) return false;

        Vec2 worldPos = Core.camera.unproject(screenX, screenY);
        float wx = worldPos.x;
        float wy = worldPos.y;

        // PC 左键：仅选择
        if (button == KeyCode.mouseLeft) {
            return selectUnitAt(wx, wy);
        }
        
        // PC 右键：仅移动
        if (button == KeyCode.mouseRight) {
            if (selectedUnit != null) {
                selectedUnit.commandMoveTo(wx, wy);
                return true;
            }
        }
        
        return false;
    }

    // =========================================================
    // 移动端逻辑 (触摸操作) - 使用 GestureListener
    // =========================================================

    @Override
    public boolean tap(float x, float y, int count, KeyCode button) {
        // 只有移动端才使用这套逻辑 (或者你想让 PC 也支持点击空地移动也可以去掉这个判断)
        if (!Core.app.isMobile()) return false;

        // 注意：GestureListener 的 x, y 已经是屏幕坐标了
        Vec2 worldPos = Core.camera.unproject(x, y);
        float wx = worldPos.x;
        float wy = worldPos.y;

        // 1. 尝试选中点击位置的单位
        boolean hitUnit = checkUnitHit(wx, wy);

        if (hitUnit) {
            // 如果点到了单位，直接选中它 (selectUnitAt 会处理选中逻辑)
            selectUnitAt(wx, wy);
            return true;
        } else {
            // 2. 如果点到了空地 (没点到单位)
            if (selectedUnit != null) {
                // 如果当前有选中的单位 -> 移动到该位置
                selectedUnit.commandMoveTo(wx, wy);
                return true;
            } else {
                // 如果没选中单位且点了空地 -> 什么都不做，或者取消选择
                return false;
            }
        }
    }

    // 辅助方法：仅检查是否点中，不执行操作
    private boolean checkUnitHit(float x, float y) {
        for (int i = WorldData.units.size - 1; i >= 0; i--) {
            Unit u = WorldData.units.get(i);
            float hitSize = u.type.hitSize;
            if (x >= u.x - hitSize && x <= u.x + hitSize &&
                y >= u.y - hitSize && y <= u.y + hitSize) {
                return true;
            }
        }
        return false;
    }

    // 辅助方法：执行选中逻辑
    private boolean selectUnitAt(float x, float y) {
        // 如果点击的地方本来就是当前选中的单位，不做处理
        // (可选优化，防止重复触发选中音效)

        for (int i = WorldData.units.size - 1; i >= 0; i--) {
            Unit u = WorldData.units.get(i);
            float hitSize = u.type.hitSize;
            
            if (x >= u.x - hitSize && x <= u.x + hitSize &&
                y >= u.y - hitSize && y <= u.y + hitSize) {
                
                // 切换选中状态
                if (selectedUnit != null) selectedUnit.isSelected = false;
                selectedUnit = u;
                u.isSelected = true;
                return true; 
            }
        }
        
        // 如果没点中任何单位，取消当前选择 (PC逻辑，移动端在 tap 里单独处理了)
        if (!Core.app.isMobile() && selectedUnit != null) {
            selectedUnit.isSelected = false;
            selectedUnit = null;
        }
        
        return false;
    }

    // GestureListener 其他空方法
    @Override public boolean pinch(Vec2 i1, Vec2 i2, Vec2 p1, Vec2 p2) { return false; }
    @Override public boolean longPress(float x, float y) { return false; }
    @Override public boolean fling(float vx, float vy, KeyCode button) { return false; }
    @Override public boolean pan(float x, float y, float dx, float dy) { return false; }
    @Override public boolean panStop(float x, float y, int pointer, KeyCode button) { return false; }
    @Override public boolean zoom(float initialDistance, float distance) { return false; }
    @Override public boolean touchDown(float x, float y, int pointer, KeyCode button) { return false; } // 注意这是 GestureListener 的 touchDown

    // InputProcessor 其他空方法
    @Override public boolean keyDown(KeyCode key) { return false; }
    @Override public boolean keyUp(KeyCode key) { return false; }
    @Override public boolean keyTyped(char character) { return false; }
    @Override public boolean touchUp(int screenX, int screenY, int pointer, KeyCode button) { return false; }
    @Override public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
    @Override public boolean mouseMoved(int screenX, int screenY) { return false; }
    @Override public boolean scrolled(float amountX, float amountY) { return false; }
}