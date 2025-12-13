package caliniya.armavoke.system.input;

import arc.Core;
import arc.input.GestureDetector;
import arc.input.GestureDetector.GestureListener;
import arc.input.InputProcessor;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import caliniya.armavoke.system.BasicSystem;
import caliniya.armavoke.system.render.MapRender;

@SuppressWarnings("unused")
public class CameraInput extends BasicSystem<CameraInput> implements GestureListener, InputProcessor {

  // 键盘控制状态
  private boolean up, down, left, right;
  private float keySpeed = 10f;

  public float currentZoom = 1.0f;

  // 缩放限制
  private float minZoom = 0.25f; // 最大放大倍数
  private float maxZoom = 4.0f; // 最大缩小倍数

  // 用于记录手势缩放开始时的基准值
  private float lastZoom = 1f;

  /** 每帧调用，应用移动和缩放 */
  @Override
  public void update() {
    if (!inited) return;

    float targetWidth = Core.graphics.getWidth() * currentZoom;
    float targetHeight = Core.graphics.getHeight() * currentZoom;

    Core.camera.width = targetWidth;
    Core.camera.height = targetHeight;

    float speed = keySpeed * currentZoom * (Core.input.keyDown(KeyCode.shiftLeft) ? 2f : 1f);

    if (up) Core.camera.position.y += speed;
    if (down) Core.camera.position.y -= speed;
    if (left) Core.camera.position.x -= speed;
    if (right) Core.camera.position.x += speed;

    clampCamera();
  }

  @Override
  public boolean pan(float x, float y, float deltaX, float deltaY) {

    Core.camera.position.x -= deltaX * currentZoom;
    Core.camera.position.y -= deltaY * currentZoom;
    return false;
  }

  @Override
  public boolean zoom(float initialDistance, float distance) {
    // 双指缩放
    // distance / initialDistance 得到的是“两指距离变大了多少倍”
    // 如果手指张开，ratio > 1，我们希望放大（currentZoom 变小）
    // 如果手指捏合，ratio < 1，我们希望缩小（currentZoom 变大）

    if (initialDistance == 0) return false;

    float ratio = initialDistance / distance;

    // 计算新的缩放值 (基于手势开始时的 lastZoom)
    float newZoom = lastZoom * ratio;
    currentZoom = Mathf.clamp(newZoom, minZoom, maxZoom);

    return true;
  }

  @Override
  public boolean pinch(Vec2 initialPointer1, Vec2 initialPointer2, Vec2 pointer1, Vec2 pointer2) {
    // 这是一个小技巧：当捏合手势开始时，GestureDetector 会先调用 touchDown
    // 但我们很难判断何时开始捏合。
    // 为了让 zoom 平滑，我们通常在 zoom 开始前如果不记录 lastZoom 会抖动。
    // 简单的做法是：不用复杂的 lastZoom，而是用灵敏度控制
    return false;
  }

  // 为了解决 zoom 的 lastZoom 问题，我们需要重写 touchDown 来重置状态
  @Override
  public boolean touchDown(float x, float y, int pointer, KeyCode button) {
    // 当手指按下时，记录当前的 zoom 作为基准
    lastZoom = currentZoom;
    return false;
  }

  @Override
  public boolean scrolled(float amountX, float amountY) {
    // 鼠标滚轮缩放
    // amountY 通常是 1 或 -1
    float zoomSpeed = 0.1f * currentZoom; // 动态缩放速度，越小缩越细
    currentZoom += amountY * zoomSpeed;

    currentZoom = Mathf.clamp(currentZoom, minZoom, maxZoom);
    return true;
  }

  @Override
  public boolean keyDown(KeyCode key) {
    if (key == KeyCode.w || key == KeyCode.up) up = true;
    if (key == KeyCode.s || key == KeyCode.down) down = true;
    if (key == KeyCode.a || key == KeyCode.left) left = true;
    if (key == KeyCode.d || key == KeyCode.right) right = true;
    return false;
  }

  @Override
  public boolean keyUp(KeyCode key) {
    if (key == KeyCode.w || key == KeyCode.up) up = false;
    if (key == KeyCode.s || key == KeyCode.down) down = false;
    if (key == KeyCode.a || key == KeyCode.left) left = false;
    if (key == KeyCode.d || key == KeyCode.right) right = false;
    return false;
  }

  @Override
  public boolean tap(float x, float y, int count, KeyCode button) {
    return false;
  }

  @Override
  public boolean longPress(float x, float y) {
    return false;
  }

  @Override
  public boolean fling(float velocityX, float velocityY, KeyCode button) {
    return false;
  }

  @Override
  public boolean panStop(float x, float y, int pointer, KeyCode button) {
    return false;
  }

  @Override
  public boolean keyTyped(char character) {
    return false;
  }

  @Override
  public boolean touchDown(int screenX, int screenY, int pointer, KeyCode button) {
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

  private void clampCamera() {
    // 安全检查
    if (MapRender.world == null) return;

    // 计算地图的世界坐标总宽高
    float mapW = MapRender.world.W * MapRender.TILE_SIZE;
    float mapH = MapRender.world.H * MapRender.TILE_SIZE;
    Core.camera.position.x = Mathf.clamp(Core.camera.position.x, 0, mapW);
    Core.camera.position.y = Mathf.clamp(Core.camera.position.y, 0, mapH);
  }

  @Override
  public caliniya.armavoke.system.input.CameraInput init() {
    priority = 1;
    return super.init();
  }
}
