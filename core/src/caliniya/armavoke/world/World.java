package caliniya.armavoke.world;

import arc.util.Log;
import caliniya.armavoke.base.tool.Ar;
import caliniya.armavoke.content.ENVBlocks;
import caliniya.armavoke.content.Floors;
import caliniya.armavoke.game.Unit;

// 这个类表示一个世界
// 数据不在这里，数据在WorrldData里
// 在初始化世界数据时同步创建一个此世界对象
// 这个类表示的是世界静态内容
public class World {

  public boolean space; // 表示这是太空还是地表(当然离实现太空还早)
  public int W;
  public int H;
  public int index;

  public boolean test = true; // 还早着

  public Ar<Floor> floors = new Ar<Floor>(10000);
  public Ar<ENVBlock> envblocks = new Ar<ENVBlock>(10000);

  public World() {
    test = true;
    W = 100;
    H = 100;
  }

  public World(int W, int H, boolean space) {
    this.W = W;
    this.H = H;
    this.space = space;
    this.index = W * H;
    test = false;
  }

  public void init() {
    if (test) {
      // 初始化地板和环境块数组
      for (int i = 0; i < W * H; i++) {
        floors.add(Floors.TestFloor);
        envblocks.add((ENVBlock)null); // 默认所有地方都没有环境块
      }

      // 放置一堵墙作为障碍物来测试寻路
      // 在 x=5 的地方画一条垂直的墙
      for (int y = 0; y < 50; y++) {
        int index = coordToIndex(5, y);
        envblocks.set(index, ENVBlocks.a);
      }
    }
  }

  public int indexToX(int ind) {
    return ind % W;
  }

  public int indexToY(int ind) {
    return ind / W;
  }

  // 根据坐标获取索引
  public int coordToIndex(int x, int y) {
    return y * W + x;
  }

  // 检查坐标是否有效
  public boolean isValidCoord(int x, int y) {
    return x >= 0 && x < W && y >= 0 && y < H;
  }

  /**
   * 【核心方法】检查一个网格坐标是否是障碍物 (无法通行)
   *
   * @param x 网格 x 坐标
   * @param y 网格 y 坐标
   * @return 如果是障碍物则返回 true
   */
  public boolean isSolid(int x, int y) {
    // 1. 检查坐标是否在地图范围内
    if (!isValidCoord(x, y)) {
      return true; // 地图外的区域视为障碍物
    }

    // 2. 检查该位置是否有环境块
    int index = coordToIndex(x, y);

    // 安全检查，防止数组未完全初始化
    if (index >= envblocks.size) {
      return false; // 如果没有环境块数据，则默认可以通过
    }
    ENVBlock block = envblocks.get(index);
    // 如果这个位置有方块 (不是 null)，并且这个方块是无法通行的
    // （未来可以给 ENVBlock 加一个 isSolid 属性）
    // 目前我们假设只要有环境块就不能通过
    return block != null;
  }
}
