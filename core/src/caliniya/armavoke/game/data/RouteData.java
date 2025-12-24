package caliniya.armavoke.game.data;

import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.math.geom.Vec2; // 引入 Vec2 进行向量计算
import caliniya.armavoke.base.tool.Ar;
import caliniya.armavoke.world.ENVBlock;
import caliniya.armavoke.world.World;
import java.util.PriorityQueue;

public class RouteData {

  private static boolean[] solidMap;
  // 【新增】间隙图：记录每个格子距离最近的障碍物有多少格
  // 0 = 是墙, 1 = 挨着墙, 2 = 离墙2格远...
  private static int[] clearanceMap;

  // 静态跳点标记 (针对标准 1x1 单位的优化)
  private static boolean[] interestMap;

  public static int W, H;

  private RouteData() {}

  public static void init() {
    World world = WorldData.world;
    W = world.W;
    H = world.H;

    solidMap = new boolean[W * H];
    interestMap = new boolean[W * H];
    clearanceMap = new int[W * H];

    // 1. 加载碰撞体
    Ar<ENVBlock> blocks = world.envblocks;
    for (int i = 0; i < blocks.size; i++) {
      ENVBlock b = blocks.get(i);
      solidMap[i] = (b != null);
    }

    // 2. 计算距离场 (Clearance Map)
    calcClearance();

    // 3. 预计算 1x1 单位的跳点
    loadJPoint();
  }

  /** 计算距离场 (Brushfire Algorithm 的简化版) 计算每个格子距离最近的障碍物的曼哈顿距离 */
  private static void calcClearance() {
    // 初始化：墙为 0，空地为最大值
    for (int i = 0; i < W * H; i++) {
      clearanceMap[i] = solidMap[i] ? 0 : 9999;
    }

    // 第一遍扫描：从左上到右下
    for (int y = 0; y < H; y++) {
      for (int x = 0; x < W; x++) {
        if (solidMap[coordToIndex(x, y)]) continue;

        int val = clearanceMap[coordToIndex(x, y)];
        // 检查左边和上边
        if (isValid(x - 1, y)) val = Math.min(val, clearanceMap[coordToIndex(x - 1, y)] + 1);
        if (isValid(x, y - 1)) val = Math.min(val, clearanceMap[coordToIndex(x, y - 1)] + 1);

        clearanceMap[coordToIndex(x, y)] = val;
      }
    }

    // 第二遍扫描：从右下到左上
    for (int y = H - 1; y >= 0; y--) {
      for (int x = W - 1; x >= 0; x--) {
        if (solidMap[coordToIndex(x, y)]) continue;

        int val = clearanceMap[coordToIndex(x, y)];
        // 检查右边和下边
        if (isValid(x + 1, y)) val = Math.min(val, clearanceMap[coordToIndex(x + 1, y)] + 1);
        if (isValid(x, y + 1)) val = Math.min(val, clearanceMap[coordToIndex(x, y + 1)] + 1);

        clearanceMap[coordToIndex(x, y)] = val;
      }
    }
  }

  private static void loadJPoint() {
    for (int y = 0; y < H; y++) {
      for (int x = 0; x < W; x++) {
        if (isSolid(x, y)) continue;
        if (hasSolidNeighbor(x, y)) {
          interestMap[coordToIndex(x, y)] = true;
        }
      }
    }
  }

  /**
   * 获取路径 (带体积支持)
   *
   * @param unitSize 单位占用格子的半径 (0=1x1单位, 1=3x3单位, 2=5x5单位) 如果你的单位是 32像素宽(1格)，传 0。 如果你的单位是
   *     64像素宽(2格)，传 1。
   */
  public static Ar<Point2> findPath(int sx, int sy, int tx, int ty, int unitSize) {
    // 1. 如果终点对于该体积单位来说太窄，直接返回空
    if (!isPassable(tx, ty, unitSize)) return new Ar<>();

    // 2. JPS 搜索 (加入体积判断)
    Ar<Point2> rawPath = jpsSearch(sx, sy, tx, ty, unitSize);

    if (rawPath == null || rawPath.isEmpty()) return new Ar<>();

    // 3. 路径平滑 (加入体积宽度的射线检测)
    return smoothPath(rawPath, unitSize);
  }

  // 重载旧方法以兼容
  public static Ar<Point2> findPath(int sx, int sy, int tx, int ty) {
    return findPath(sx, sy, tx, ty, 0);
  }

  private static Ar<Point2> jpsSearch(int sx, int sy, int tx, int ty, int unitSize) {
    PriorityQueue<Node> openList = new PriorityQueue<>();
    boolean[] closedMap = new boolean[W * H];
    Node[] nodeIndex = new Node[W * H];

    Node startNode = new Node(sx, sy, null, 0, dist(sx, sy, tx, ty));
    openList.add(startNode);
    nodeIndex[coordToIndex(sx, sy)] = startNode;

    while (!openList.isEmpty()) {
      Node current = openList.poll();

      if (current.x == tx && current.y == ty) {
        return reconstructPath(current);
      }

      closedMap[coordToIndex(current.x, current.y)] = true;

      // 传入 unitSize
      identifySuccessors(current, tx, ty, openList, closedMap, nodeIndex, unitSize);
    }

    return null;
  }

  private static void identifySuccessors(
      Node current,
      int tx,
      int ty,
      PriorityQueue<Node> openList,
      boolean[] closedMap,
      Node[] nodeIndex,
      int unitSize) {
    int[] dirsX = {0, 0, -1, 1, -1, -1, 1, 1};
    int[] dirsY = {1, -1, 0, 0, 1, -1, 1, -1};

    for (int i = 0; i < 8; i++) {
      // 传入 unitSize
      Point2 jumpPoint = jump(current.x, current.y, dirsX[i], dirsY[i], tx, ty, unitSize);

      if (jumpPoint != null) {
        int jx = (int) jumpPoint.x;
        int jy = (int) jumpPoint.y;
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

  private static Point2 jump(int cx, int cy, int dx, int dy, int tx, int ty, int unitSize) {
    int nextX = cx + dx;
    int nextY = cy + dy;

    // 【修改】检测是否通过：不仅要不越界，还要满足 Clearance >= unitSize
    if (!isPassable(nextX, nextY, unitSize)) return null;

    if (nextX == tx && nextY == ty) return new Point2(nextX, nextY);

    // 对于大体积单位，我们不能简单使用 interestMap (那是给 1x1 优化的)
    // 必须回退到每步检查，或者重新计算针对该体积的 interestMap (太复杂)
    // 这里采用简化逻辑：如果 unitSize > 0，则不做复杂的强制邻居跳跃优化，退化为普通 A* 步进检查
    // 或者，仅当 unitSize == 0 时使用 interestMap，否则每步检查

    if (unitSize == 0) {
      // 标准 1x1 单位优化逻辑
      if (interestMap[coordToIndex(nextX, nextY)]) {
        if (dx != 0 && dy != 0) {
          if ((isSolid(nextX - dx, nextY) && !isSolid(nextX - dx, nextY + dy))
              || (isSolid(nextX, nextY - dy) && !isSolid(nextX + dx, nextY - dy))) {
            return new Point2(nextX, nextY);
          }
        } else {
          if (dx != 0) {
            if ((isSolid(nextX, nextY - 1) && !isSolid(nextX + dx, nextY - 1))
                || (isSolid(nextX, nextY + 1) && !isSolid(nextX + dx, nextY + 1))) {
              return new Point2(nextX, nextY);
            }
          } else {
            if ((isSolid(nextX - 1, nextY) && !isSolid(nextX - 1, nextY + dy))
                || (isSolid(nextX + 1, nextY) && !isSolid(nextX + 1, nextY + dy))) {
              return new Point2(nextX, nextY);
            }
          }
        }
      }
    } else {
      // 【大单位逻辑】：简单地作为节点返回（退化为 A*），或者你可以实现更复杂的宽体强制邻居判断
      // 为了代码稳健性，如果体积大，我们每一步都允许作为跳点（损失性能但保证不穿墙）
      return new Point2(nextX, nextY);
    }

    if (dx != 0 && dy != 0) {
      if (jump(nextX, nextY, dx, 0, tx, ty, unitSize) != null
          || jump(nextX, nextY, 0, dy, tx, ty, unitSize) != null) {
        return new Point2(nextX, nextY);
      }
    }

    return jump(nextX, nextY, dx, dy, tx, ty, unitSize);
  }

  // --- 路径平滑 (带宽度检测) ---

  private static Ar<Point2> smoothPath(Ar<Point2> path, int unitSize) {
    if (path.size <= 2) return path;

    Ar<Point2> smoothed = new Ar<>();
    smoothed.add(path.get(0));

    int inputIndex = 0;
    while (inputIndex < path.size - 1) {
      int nextIndex = inputIndex + 1;
      for (int i = path.size - 1; i > inputIndex + 1; i--) {
        Point2 start = path.get(inputIndex);
        Point2 end = path.get(i);

        // 【修改】检测宽体直线
        if (lineCastWidth((int) start.x, (int) start.y, (int) end.x, (int) end.y, unitSize)) {
          nextIndex = i;
          break;
        }
      }
      smoothed.add(path.get(nextIndex));
      inputIndex = nextIndex;
    }

    return smoothed;
  }

  /** 宽体射线检测 实现了你要求的"计算方向，偏移，检测环境块"的逻辑 但更高效的是直接利用 Clearance Map */
  private static boolean lineCastWidth(int x0, int y0, int x1, int y1, int unitSize) {
    // 如果没有体积，使用普通检测
    if (unitSize == 0) return !lineCastSolid(x0, y0, x1, y1);

    // 使用 Bresenham 算法遍历线上的每个点
    // 对于线上的每个点，检查 clearanceMap[i] >= unitSize
    // 这等价于：以此线为中心，半径为 unitSize 的管道内是否有墙

    int dx = Math.abs(x1 - x0);
    int dy = Math.abs(y1 - y0);
    int sx = x0 < x1 ? 1 : -1;
    int sy = y0 < y1 ? 1 : -1;
    int err = dx - dy;

    int cx = x0;
    int cy = y0;

    while (true) {
      // 【核心检查】如果路径上任意一点的间隙小于单位体积，则无法直线通过
      if (!isPassable(cx, cy, unitSize)) return false;

      if (cx == x1 && cy == y1) break;
      int e2 = 2 * err;
      if (e2 > -dy) {
        err -= dy;
        cx += sx;
      }
      if (e2 < dx) {
        err += dx;
        cy += sy;
      }
    }
    return true;
  }

  private static boolean lineCastSolid(int x0, int y0, int x1, int y1) {
    // ... (保持之前的实现，用于 unitSize=0) ...
    // 为了篇幅这里省略，逻辑同上，只是把 check 改为 isSolid
    int dx = Math.abs(x1 - x0);
    int dy = Math.abs(y1 - y0);
    int sx = x0 < x1 ? 1 : -1;
    int sy = y0 < y1 ? 1 : -1;
    int err = dx - dy;

    while (true) {
      if (isSolid(x0, y0)) return true;
      if (x0 == x1 && y0 == y1) break;
      int e2 = 2 * err;
      if (e2 > -dy) {
        err -= dy;
        x0 += sx;
      }
      if (e2 < dx) {
        err += dx;
        y0 += sy;
      }
    }
    return false;
  }

  // --- 辅助方法 ---

  /** 判断某点是否允许特定体积的单位通过 */
  public static boolean isPassable(int x, int y, int unitSize) {
    if (!isValid(x, y)) return false;
    // 如果 unitSize 为 0，只要不是墙就行
    // 如果 unitSize > 0，必须 clearance >= unitSize
    // 例如：unitSize=1，需要该点本身不是墙(clearance>0)，且离墙至少1格远
    return clearanceMap[coordToIndex(x, y)] > unitSize;
  }

  private static Ar<Point2> reconstructPath(Node current) {
    Ar<Point2> path = new Ar<>();
    while (current != null) {
      path.add(new Point2(current.x, current.y));
      current = current.parent;
    }
    path.reverse();
    return path;
  }

  public static int coordToIndex(int x, int y) {
    return y * W + x;
  }

  public static boolean isValid(int x, int y) {
    return x >= 0 && x < W && y >= 0 && y < H;
  }

  public static boolean isSolid(int x, int y) {
    if (!isValid(x, y)) return true;
    return solidMap[coordToIndex(x, y)];
  }

  private static boolean hasSolidNeighbor(int x, int y) {
    // 检查周围8个点是否有墙
    for (int i = -1; i <= 1; i++) {
      for (int j = -1; j <= 1; j++) {
        if (i == 0 && j == 0) continue;
        if (isSolid(x + i, y + j)) return true;
      }
    }
    return false;
  }

  private static float dist(int x1, int y1, int x2, int y2) {
    // 曼哈顿距离用于启发式通常在4向移动好，欧几里得距离在任意角度好
    // 这里为了速度可以用曼哈顿或者切比雪夫
    return Math.abs(x1 - x2) + Math.abs(y1 - y2);
  }

  // A* 节点内部类
  private static class Node implements Comparable<Node> {
    int x, y;
    Node parent;
    float g; // 离起点距离
    float h; // 离终点估算距离

    public Node(int x, int y, Node parent, float g, float h) {
      this.x = x;
      this.y = y;
      this.parent = parent;
      this.g = g;
      this.h = h;
    }

    @Override
    public int compareTo(Node other) {
      return Float.compare(this.g + this.h, other.g + other.h);
    }
  }
}
