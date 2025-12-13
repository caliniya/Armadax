package caliniya.armavoke.system.render;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.graphics.Camera;
import arc.math.Mathf;
import arc.util.Align;
import arc.util.Log;
import caliniya.armavoke.game.data.WorldData;
import caliniya.armavoke.world.World;
import caliniya.armavoke.world.Floor;

public class MapRender {
  // 定义单个网格的大小（像素）
  public static final float TILE_SIZE = 32f;
  public static World world;
  public static boolean a = false;
  public Camera camera = Core.camera;

  /**
   * 渲染地图
   *
   * @param camera 当前使用的相机，用于计算视野范围
   */
  public void render() {
    if (!a) {
      a = true;
      WorldData.initWorld();
      world = WorldData.world;
    }

    // 获取相机可视区域的左下角和右上角坐标 (世界坐标)
    // 相机位置是中心点，所以要减去/加上 宽高的一半
    float viewLeft = camera.position.x - camera.width / 2f;
    float viewBottom = camera.position.y - camera.height / 2f;
    float viewRight = camera.position.x + camera.width / 2f;
    float viewTop = camera.position.y + camera.height / 2f;

    // 将世界坐标转换为网格坐标 (Grid Coordinates)
    // 使用 Math.floor 向下取整确保覆盖边缘，并扩充 1-2 个格子防止边缘闪烁
    int startX = (int) Math.floor(viewLeft / TILE_SIZE) - 1;
    int startY = (int) Math.floor(viewBottom / TILE_SIZE) - 1;
    int endX = (int) Math.ceil(viewRight / TILE_SIZE) + 1;
    int endY = (int) Math.ceil(viewTop / TILE_SIZE) + 1;

    // 限制范围在地图边界内 (Clamping)
    // 防止索引越界 (比如相机移到了地图外面，不能去读 -1 的索引)
    startX = Mathf.clamp(startX, 0, world.W - 1);
    startY = Mathf.clamp(startY, 0, world.H - 1);
    endX = Mathf.clamp(endX, 0, world.W - 1);
    endY = Mathf.clamp(endY, 0, world.H - 1);

    for (int y = startY; y <= endY; y++) {
      for (int x = startX; x <= endX; x++) {
        // 计算该坐标在数组中的索引
        int index = world.coordToIndex(x, y);

        // 安全检查：防止计算出的索引超出数组范围
        if (index < 0 || index >= world.floors.size) continue;

        // 获取地板对象
        Floor floor = world.floors.get(index);

        // 如果该位置没有地板或者地板为空，跳过
        if (floor == null) continue;

        drawFloor(floor, x, y);
      }
    }
  }

  private void drawFloor(Floor floor, int x, int y) {
    // 从图集中查找纹理
    // 优化什么的，下辈子再做吧
    TextureRegion region = Core.atlas.find(floor.name);

    if (!Core.atlas.isFound(region)) {
      // region = Core.atlas.find("error")
      // (这玩意我还没做出来)
      return;
    }

    // 计算绘制位置
    // Arc 的 Draw.rect 默认是以 (wx, wy) 为中心绘制的
    // 所以网格 (0,0) 的中心点应该是 (16, 16)
    float drawX = x * TILE_SIZE + TILE_SIZE / 2f;
    float drawY = y * TILE_SIZE + TILE_SIZE / 2f;

    // 绘制
    Draw.rect(region, drawX, drawY, TILE_SIZE, TILE_SIZE);
  }
}
