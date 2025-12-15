package caliniya.armavoke.system.world;

import arc.Core;
import arc.input.GestureDetector;
import arc.input.GestureDetector.GestureListener;
import arc.input.KeyCode;
import arc.input.InputProcessor;
import arc.math.geom.Vec2;
import caliniya.armavoke.base.tool.Ar;
import caliniya.armavoke.game.Unit;
import caliniya.armavoke.game.data.WorldData;
import caliniya.armavoke.system.BasicSystem;

// 负责单位选中以及下达目标
public class UnitControl extends BasicSystem<UnitControl>
    implements InputProcessor, GestureListener {
  //当前选中的单位
  public Ar<Unit> units = new Ar<Unit>(100);

  @Override
  public UnitControl init() {
    this.priority = 5;
    return super.init();
  }

  @Override
  public void update() {}

  @Override
  public boolean touchDown(int screenX, int screenY, int pointer, KeyCode button) {
    // 如果是移动端，忽略 touchDown，交给 tap 处理 (防止误触和阻碍拖动地图)
    if (Core.app.isMobile()) return false;
    return false;
  }

  @Override
  public boolean tap(float x, float y, int count, KeyCode button) {
    // 只有移动端才使用这套逻辑
    if (!Core.app.isMobile()) return false;

    // 注意：GestureListener 的 x, y 已经是屏幕坐标了
    Vec2 worldPos = Core.camera.unproject(x, y);
    float wx = worldPos.x;
    float wy = worldPos.y;
    return false;
  }
  // GestureListener 其他空方法
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
  } // 注意这是 GestureListener 的 touchDown

  // InputProcessor 其他空方法
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