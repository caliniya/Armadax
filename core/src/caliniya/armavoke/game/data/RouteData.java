package caliniya.armavoke.game.data;

import arc.math.Mathf;
import arc.math.geom.Point2;
import caliniya.armavoke.base.tool.Ar;
import caliniya.armavoke.world.ENVBlock;
import caliniya.armavoke.world.World;
import java.util.PriorityQueue;

public class RouteData {

  // 配置常量
  private static final int MAX_PRECALC_RADIUS = 4; // 预计算跳点的最大半径 (0-4)
  private static final int MAX_CAPABILITY = 2;     // 最大跨越等级 (0, 1, 2)

  public static int W, H;

  // --- 数据结构类 ---
  /** 导航层：代表一种特定的跨越能力 (例如普通、跨越1层墙、跨越2层墙) */
  private static class NavLayer {
    boolean[] solidMap;        // 当前能力的障碍物图
    int[] clearanceMap;        // 当前能力的距离场
    boolean[][] interestMaps;  // [半径][索引] 针对不同体积单位的跳点缓存

    public NavLayer() {
      solidMap = new boolean[W * H];
      clearanceMap = new int[W * H];
      interestMaps = new boolean[MAX_PRECALC_RADIUS + 1][W * H];
    }
  }

  // 存储所有导航层：layers[capability]
  private static NavLayer[] layers;

  private RouteData() {}

  public static void init() {
    World world = WorldData.world;
    W = world.W;
    H = world.H;

    layers = new NavLayer[MAX_CAPABILITY + 1];

    // 1. 初始化 Layer 0 (基础层，无跨越能力)
    layers[0] = new NavLayer();
    Ar<ENVBlock> blocks = world.envblocks;
    for (int i = 0; i < blocks.size; i++) {
      ENVBlock b = blocks.get(i);
      layers[0].solidMap[i] = (b != null); // 基础障碍物
    }

    // 2. 初始化 Layer 1 ~ N (腐蚀层，为机甲提供能力)
    for (int cap = 1; cap <= MAX_CAPABILITY; cap++) {
      layers[cap] = new NavLayer();
      // 基于上一层进行"腐蚀"操作 (墙壁变薄)
      erodeMap(layers[cap - 1].solidMap, layers[cap].solidMap);
    }

    // 3. 为每一层计算距离场和跳点
    for (int cap = 0; cap <= MAX_CAPABILITY; cap++) {
      NavLayer layer = layers[cap];
      
      // A. 计算距离场 (Clearance)
      calcClearance(layer);

      // B. 为不同半径预计算跳点
      for (int r = 0; r <= MAX_PRECALC_RADIUS; r++) {
        loadJPointsForRadius(layer, r);
      }
    }
  }

  /** 地图腐蚀算法：只有当一个墙壁周围全是墙壁时，它在新地图中才保留为墙壁 */
  private static void erodeMap(boolean[] source, boolean[] target) {
    for (int y = 0; y < H; y++) {
      for (int x = 0; x < W; x++) {
        int index = coordToIndex(x, y);
        if (!source[index]) {
          target[index] = false; // 本来就是空地
        } else {
          // 本来是墙，检查是否是边缘墙
          // 如果上下左右有一个是空地，说明它是边缘，腐蚀掉(变为空地)
          boolean isEdge = false;
          if (isValid(x + 1, y) && !source[coordToIndex(x + 1, y)]) isEdge = true;
          else if (isValid(x - 1, y) && !source[coordToIndex(x - 1, y)]) isEdge = true;
          else if (isValid(x, y + 1) && !source[coordToIndex(x, y + 1)]) isEdge = true;
          else if (isValid(x, y - 1) && !source[coordToIndex(x, y - 1)]) isEdge = true;
          
          target[index] = !isEdge; // 如果是边缘则移除，否则保留
        }
      }
    }
  }

  /** 计算距离场 */
  private static void calcClearance(NavLayer layer) {
    for (int i = 0; i < W * H; i++) {
      layer.clearanceMap[i] = layer.solidMap[i] ? 0 : 9999;
    }
    // 左上 -> 右下
    for (int y = 0; y < H; y++) {
      for (int x = 0; x < W; x++) {
        if (layer.solidMap[coordToIndex(x, y)]) continue;
        int val = layer.clearanceMap[coordToIndex(x, y)];
        if (isValid(x - 1, y)) val = Math.min(val, layer.clearanceMap[coordToIndex(x - 1, y)] + 1);
        if (isValid(x, y - 1)) val = Math.min(val, layer.clearanceMap[coordToIndex(x, y - 1)] + 1);
        layer.clearanceMap[coordToIndex(x, y)] = val;
      }
    }
    // 右下 -> 左上
    for (int y = H - 1; y >= 0; y--) {
      for (int x = W - 1; x >= 0; x--) {
        if (layer.solidMap[coordToIndex(x, y)]) continue;
        int val = layer.clearanceMap[coordToIndex(x, y)];
        if (isValid(x + 1, y)) val = Math.min(val, layer.clearanceMap[coordToIndex(x + 1, y)] + 1);
        if (isValid(x, y + 1)) val = Math.min(val, layer.clearanceMap[coordToIndex(x, y + 1)] + 1);
        layer.clearanceMap[coordToIndex(x, y)] = val;
      }
    }
  }

  /** 为特定半径预计算跳点 */
  private static void loadJPointsForRadius(NavLayer layer, int radius) {
    for (int y = 0; y < H; y++) {
      for (int x = 0; x < W; x++) {
        // 核心：基于 radius 判断是否可通过
        if (!isPassable(layer, x, y, radius)) continue;
        
        // 核心：基于 radius 判断是否产生强制邻居
        // 这里的逻辑是：如果这是一个"拐角"点，对于体积为 radius 的单位来说，它就是一个跳点
        if (hasForcedNeighbor(layer, x, y, radius)) {
          layer.interestMaps[radius][coordToIndex(x, y)] = true;
        }
      }
    }
  }

  /** 
   * 获取路径
   * @param rawSize 浮点数大小 (例如 1.5)
   * @param capability 跨越能力 (0=普通, 1=机甲...)
   */
  public static Ar<Point2> findPath(int sx, int sy, int tx, int ty, float rawSize, int capability) {
    // 1. 处理参数
    int radius = Mathf.ceil(rawSize); // 向上取整作为半径
    capability = Mathf.clamp(capability, 0, MAX_CAPABILITY);
    
    NavLayer layer = layers[capability];

    // 2. 检查终点可行性
    if (!isPassable(layer, tx, ty, radius)) return new Ar<>();

    // 3. 选择寻路策略
    Ar<Point2> rawPath;
    
    if (radius <= MAX_PRECALC_RADIUS) {
        // [策略 A] 预计算 JPS
        rawPath = jpsSearchPrecalc(layer, sx, sy, tx, ty, radius);
    } else {
        // [策略 B] 标准 A* (大单位回退)
        // 对于巨型单位，跳点稀疏优势不明显，且预计算内存消耗过大，直接用 A* 配合 Clearance 即可
        rawPath = aStarSearch(layer, sx, sy, tx, ty, radius);
    }

    if (rawPath == null || rawPath.isEmpty()) return new Ar<>();

    // 4. 路径平滑
    return smoothPath(rawPath, layer, radius);
  }

  // --- 核心算法实现 ---

  private static Ar<Point2> jpsSearchPrecalc(NavLayer layer, int sx, int sy, int tx, int ty, int radius) {
    // 逻辑与之前的 JPS 类似，但使用 layer.interestMaps[radius]
    PriorityQueue<Node> openList = new PriorityQueue<>();
    boolean[] closedMap = new boolean[W * H];
    Node[] nodeIndex = new Node[W * H];

    Node startNode = new Node(sx, sy, null, 0, dist(sx, sy, tx, ty));
    openList.add(startNode);
    nodeIndex[coordToIndex(sx, sy)] = startNode;

    while (!openList.isEmpty()) {
      Node current = openList.poll();
      if (current.x == tx && current.y == ty) return reconstructPath(current);
      closedMap[coordToIndex(current.x, current.y)] = true;

      identifySuccessorsJPS(layer, current, tx, ty, openList, closedMap, nodeIndex, radius);
    }
    return null;
  }

  private static Ar<Point2> aStarSearch(NavLayer layer, int sx, int sy, int tx, int ty, int radius) {
    // 标准 A* 实现
    PriorityQueue<Node> openList = new PriorityQueue<>();
    boolean[] closedMap = new boolean[W * H];
    Node[] nodeIndex = new Node[W * H];

    openList.add(new Node(sx, sy, null, 0, dist(sx, sy, tx, ty)));
    
    int[] dirsX = {0, 0, -1, 1}; // 简化为4向，或者8向均可
    int[] dirsY = {1, -1, 0, 0};

    while (!openList.isEmpty()) {
      Node current = openList.poll();
      if (current.x == tx && current.y == ty) return reconstructPath(current);
      
      if (closedMap[coordToIndex(current.x, current.y)]) continue;
      closedMap[coordToIndex(current.x, current.y)] = true;

      for(int i=0; i<4; i++) {
          int nx = current.x + dirsX[i];
          int ny = current.y + dirsY[i];
          if(isPassable(layer, nx, ny, radius)) {
              float newG = current.g + 1;
              int index = coordToIndex(nx, ny);
              if (closedMap[index]) continue;
              
              Node neighbor = nodeIndex[index];
              if (neighbor == null || newG < neighbor.g) {
                  neighbor = new Node(nx, ny, current, newG, dist(nx, ny, tx, ty));
                  nodeIndex[index] = neighbor;
                  openList.add(neighbor);
              }
          }
      }
    }
    return null;
  }

  // --- JPS 辅助 ---

  private static void identifySuccessorsJPS(NavLayer layer, Node current, int tx, int ty, 
      PriorityQueue<Node> openList, boolean[] closedMap, Node[] nodeIndex, int radius) {
      
      int[] dirsX = {0, 0, -1, 1, -1, -1, 1, 1};
      int[] dirsY = {1, -1, 0, 0, 1, -1, 1, -1};

      for (int i = 0; i < 8; i++) {
          Point2 jumpPoint = jump(layer, current.x, current.y, dirsX[i], dirsY[i], tx, ty, radius);
          if (jumpPoint != null) {
              int jx = (int)jumpPoint.x;
              int jy = (int)jumpPoint.y;
              int index = coordToIndex(jx, jy);
              if (closedMap[index]) continue;

              float gScore = current.g + dist(current.x, current.y, jx, jy);
              Node neighbor = nodeIndex[index];
              if (neighbor == null) {
                  neighbor = new Node(jx, jy, current, gScore, dist(jx, jy, tx, ty));
                  nodeIndex[index] = neighbor;
                  openList.add(neighbor);
              } else if (gScore < neighbor.g) {
                  neighbor.g = gScore;
                  neighbor.parent = current;
                  openList.remove(neighbor);
                  openList.add(neighbor);
              }
          }
      }
  }

  private static Point2 jump(NavLayer layer, int cx, int cy, int dx, int dy, int tx, int ty, int radius) {
      int nextX = cx + dx;
      int nextY = cy + dy;

      if (!isPassable(layer, nextX, nextY, radius)) return null;
      if (nextX == tx && nextY == ty) return new Point2(nextX, nextY);

      // 如果当前点是预计算好的跳点，直接返回
      if (layer.interestMaps[radius][coordToIndex(nextX, nextY)]) {
          return new Point2(nextX, nextY);
      }

      // 对角线递归
      if (dx != 0 && dy != 0) {
          if (jump(layer, nextX, nextY, dx, 0, tx, ty, radius) != null || 
              jump(layer, nextX, nextY, 0, dy, tx, ty, radius) != null) {
              return new Point2(nextX, nextY);
          }
      }

      return jump(layer, nextX, nextY, dx, dy, tx, ty, radius);
  }

  // --- 路径平滑 ---
  
  private static Ar<Point2> smoothPath(Ar<Point2> path, NavLayer layer, int radius) {
      if (path.size <= 2) return path;
      Ar<Point2> smoothed = new Ar<>();
      smoothed.add(path.get(0));
      int inputIndex = 0;
      while (inputIndex < path.size - 1) {
          int nextIndex = inputIndex + 1;
          for (int i = path.size - 1; i > inputIndex + 1; i--) {
              Point2 start = path.get(inputIndex);
              Point2 end = path.get(i);
              if (lineCast(layer, (int)start.x, (int)start.y, (int)end.x, (int)end.y, radius)) {
                  nextIndex = i;
                  break;
              }
          }
          smoothed.add(path.get(nextIndex));
          inputIndex = nextIndex;
      }
      return smoothed;
  }

  /** 射线检测：利用 clearanceMap  */
  private static boolean lineCast(NavLayer layer, int x0, int y0, int x1, int y1, int radius) {
      int dx = Math.abs(x1 - x0);
      int dy = Math.abs(y1 - y0);
      int sx = x0 < x1 ? 1 : -1;
      int sy = y0 < y1 ? 1 : -1;
      int err = dx - dy;
      int cx = x0;
      int cy = y0;

      while (true) {
          if (!isPassable(layer, cx, cy, radius)) return false;
          if (cx == x1 && cy == y1) break;
          int e2 = 2 * err;
          if (e2 > -dy) { err -= dy; cx += sx; }
          if (e2 < dx) { err += dx; cy += sy; }
      }
      return true;
  }

  // --- 通用辅助 ---

  private static boolean isPassable(NavLayer layer, int x, int y, int radius) {
      if (!isValid(x, y)) return false;
      // clearance 必须 > radius
      return layer.clearanceMap[coordToIndex(x, y)] > radius;
  }

  // 这里的 hasForcedNeighbor 需要根据 radius 判断虚拟墙壁
  // 简单实现：检查周围8格的 isPassable 状态变化
  private static boolean hasForcedNeighbor(NavLayer layer, int x, int y, int radius) {
      // 简化判断：如果周围有阻挡，且该点是空地，则可能是拐点
      // 严格的 JPS 定义比较复杂，这里可以用一个近似：
      // 只要该点可通过，且周围 8 邻域内有"不可通过"的点，就视为潜在跳点
      // 这会产生比严格 JPS 稍多的跳点，但预计算速度快且逻辑安全
      if (!isPassable(layer, x, y, radius)) return false;
      
      for (int i = -1; i <= 1; i++) {
        for (int j = -1; j <= 1; j++) {
            if (i==0 && j==0) continue;
            if (!isPassable(layer, x+i, y+j, radius)) return true;
        }
      }
      return false;
  }
  
  
  public static int coordToIndex(int x, int y) { return y * W + x; }
  public static boolean isValid(int x, int y) { return x >= 0 && x < W && y >= 0 && y < H; }
  private static float dist(int x1, int y1, int x2, int y2) { return Math.abs(x1 - x2) + Math.abs(y1 - y2); }
  private static Ar<Point2> reconstructPath(Node current) {
      Ar<Point2> p = new Ar<>();
      while(current!=null){ p.add(new Point2(current.x, current.y)); current=current.parent; }
      p.reverse(); return p;
  }
  private static class Node implements Comparable<Node> {
      int x, y; Node parent; float g, h;
      public Node(int x, int y, Node parent, float g, float h) { this.x=x; this.y=y; this.parent=parent; this.g=g; this.h=h; }
      @Override public int compareTo(Node o) { return Float.compare(g+h, o.g+o.h); }
  }
}