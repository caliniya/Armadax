package caliniya.armavoke.system.render;

import caliniya.armavoke.system.BasicSystem;
import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.graphics.Camera;
import arc.math.Mathf;
import caliniya.armavoke.game.data.WorldData;
import caliniya.armavoke.world.World;
import caliniya.armavoke.world.Floor;
import caliniya.armavoke.world.ENVBlock; // 【1. 新增】导入 ENVBlock 类

public class MapRender extends BasicSystem<MapRender> {
  public static final float TILE_SIZE = 32f;
  public static World world;
  public Camera camera = Core.camera;

  @Override
  public void update() {
    if (!inited) return;
    
    // ... (计算 startX, startY, endX, endY 的代码保持不变) ...
    float viewLeft = camera.position.x - camera.width / 2f;
    float viewBottom = camera.position.y - camera.height / 2f;
    float viewRight = camera.position.x + camera.width / 2f;
    float viewTop = camera.position.y + camera.height / 2f;

    int startX = (int) Math.floor(viewLeft / TILE_SIZE) - 1;
    int startY = (int) Math.floor(viewBottom / TILE_SIZE) - 1;
    int endX = (int) Math.ceil(viewRight / TILE_SIZE) + 1;
    int endY = (int) Math.ceil(viewTop / TILE_SIZE) + 1;

    startX = Mathf.clamp(startX, 0, world.W - 1);
    startY = Mathf.clamp(startY, 0, world.H - 1);
    endX = Mathf.clamp(endX, 0, world.W - 1);
    endY = Mathf.clamp(endY, 0, world.H - 1);

    for (int y = startY; y <= endY; y++) {
      for (int x = startX; x <= endX; x++) {
        int index = world.coordToIndex(x, y);

        // --- 绘制地板 (保持不变) ---
        if (index >= 0 && index < world.floors.size) {
            Floor floor = world.floors.get(index);
            if (floor != null) {
                drawFloor(floor, x, y);
            }
        }
        
        // --- 【2. 新增】绘制环境块 ---
        if (index >= 0 && index < world.envblocks.size) {
            ENVBlock block = world.envblocks.get(index);
            // 如果这个位置有环境块，就绘制它
            if (block != null) {
                drawBlock(block, x, y);
            }
        }
      }
    }
  }

  private void drawFloor(Floor floor, int x, int y) {
    TextureRegion region = Core.atlas.find(floor.name);
    if (!Core.atlas.isFound(region)) return;
    
    float drawX = x * TILE_SIZE + TILE_SIZE / 2f;
    float drawY = y * TILE_SIZE + TILE_SIZE / 2f;
    Draw.rect(region, drawX, drawY, TILE_SIZE, TILE_SIZE);
  }

  /**
   * 【3. 新增】绘制环境块的方法
   * @param block 要绘制的方块
   * @param x 网格 X 坐标
   * @param y 网格 Y 坐标
   */
  private void drawBlock(ENVBlock block, int x, int y) {
    // 假设 ENVBlock 也有一个 'name' 字段来获取纹理
    TextureRegion region = Core.atlas.find(block.name); 
    
    if (!Core.atlas.isFound(region)) return;

    // 绘制位置和地板一样
    float drawX = x * TILE_SIZE + TILE_SIZE / 2f;
    float drawY = y * TILE_SIZE + TILE_SIZE / 2f;
    Draw.rect(region, drawX, drawY, TILE_SIZE, TILE_SIZE);
  }

  @Override
  public MapRender init() {
    WorldData.initWorld();
    world = WorldData.world;
    priority = 10;
    return super.init();
  }
}