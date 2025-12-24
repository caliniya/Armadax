package caliniya.armavoke.system.render;

import arc.Core;
import arc.graphics.Camera;
import arc.math.Mathf;
import caliniya.armavoke.game.data.WorldData;
import caliniya.armavoke.system.BasicSystem;
import caliniya.armavoke.world.World;
import caliniya.armavoke.base.game.*;

public class MapRender extends BasicSystem<MapRender> {
  public static final float TILE_SIZE = 32f;
  public static World world;
  public Camera camera = Core.camera;

  // 存储所有区块的二维数组
  private MapChunk[][] chunks;
  private int chunksW, chunksH;

  @Override
  public MapRender init() {
    WorldData.initWorld();
    world = WorldData.world;
    priority = 10;
    
    initChunks();
    
    return super.init();
  }

  private void initChunks() {
    // 计算横向和纵向有多少个区块
    chunksW = Mathf.ceil((float)world.W / MapChunk.SIZE);
    chunksH = Mathf.ceil((float)world.H / MapChunk.SIZE);
    
    chunks = new MapChunk[chunksW][chunksH];
    
    for(int x = 0; x < chunksW; x++){
        for(int y = 0; y < chunksH; y++){
            chunks[x][y] = new MapChunk(x, y);
        }
    }
  }

  @Override
  public void update() {
    if (!inited || chunks == null) return;
    
    // 计算摄像机视野范围内的 区块索引
    float viewLeft = camera.position.x - camera.width / 2f;
    float viewBottom = camera.position.y - camera.height / 2f;
    float viewRight = camera.position.x + camera.width / 2f;
    float viewTop = camera.position.y + camera.height / 2f;

    // 将像素坐标转换为区块索引
    int startX = (int) (viewLeft / MapChunk.PIXEL_SIZE);
    int startY = (int) (viewBottom / MapChunk.PIXEL_SIZE);
    int endX = (int) (viewRight / MapChunk.PIXEL_SIZE);
    int endY = (int) (viewTop / MapChunk.PIXEL_SIZE);

    // 限制在数组范围内
    startX = Mathf.clamp(startX, 0, chunksW - 1);
    startY = Mathf.clamp(startY, 0, chunksH - 1);
    endX = Mathf.clamp(endX, 0, chunksW - 1);
    endY = Mathf.clamp(endY, 0, chunksH - 1);

    // 只渲染视野内的区块
    for (int y = startY; y <= endY; y++) {
      for (int x = startX; x <= endX; x++) {
          // 每个区块内部会检查 dirty，如果脏了会自动重绘 FBO
          // 如果没脏，直接画一张大图
          chunks[x][y].render();
      }
    }
  }
  
  /** 
   * 当地图某个位置发生改变时调用此方法 
   * 例如：玩家建造了墙，或者地形被破坏
   */
  public void flagUpdate(int worldGridX, int worldGridY) {
      int cx = worldGridX / MapChunk.SIZE;
      int cy = worldGridY / MapChunk.SIZE;
      
      if (cx >= 0 && cx < chunksW && cy >= 0 && cy < chunksH) {
          chunks[cx][cy].dirty = true;
          
          // 边界处理：如果修改的块在区块边缘，可能由于纹理溢出(比如有些墙比较大)需要更新相邻区块
          // 这里暂时只更新本格
      }
  }

  @Override
  public void dispose() {
      // 释放显存资源
      if(chunks != null){
          for(int x = 0; x < chunksW; x++){
              for(int y = 0; y < chunksH; y++){
                  chunks[x][y].dispose();
              }
          }
      }
      super.dispose();
  }
}