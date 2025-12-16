package caliniya.armavoke.game.data;

import arc.math.Mathf;
import arc.math.geom.Point2; // Arc 引擎的点类
import caliniya.armavoke.base.tool.Ar;
import caliniya.armavoke.world.ENVBlock;
import caliniya.armavoke.world.World;
import java.util.PriorityQueue;

public class RouteData {

    // 碰撞缓存：true 表示有障碍物
    private static boolean[] solidMap;
    // 静态跳点标记：true 表示这个点周围有墙，是一个潜在的拐点
    private static boolean[] interestMap; 
    
    public static int W, H;

    private RouteData() {}

    public static void init() {
        World world = WorldData.world;
        W = world.W;
        H = world.H;
        
        solidMap = new boolean[W * H];
        interestMap = new boolean[W * H];

        // 1. 第一步：加载当前世界数据 (缓存碰撞体)
        Ar<ENVBlock> blocks = world.envblocks;
        for (int i = 0; i < blocks.size; i++) {
            ENVBlock b = blocks.get(i);
            // 假设 ENVBlock 不为 null 且没有特殊的 isPassable 属性即为墙
            // 如果你有特定的墙体判断逻辑，请在这里修改
            solidMap[i] = (b != null); 
        }

        // 2. 第二步：预计算静态跳点
        loadJPoint();
    }

    /** 预计算：标记所有在几何上可能成为跳点的“关键位置” */
    private static void loadJPoint() {
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                if (isSolid(x, y)) continue;

                // 判断是否是"拐角"
                // 如果一个点，它的邻居是墙，且该邻居的对角线是空的，那么这个点就是一个"强制邻居"的产生源
                // 简单来说：只要这个格子周围 8 邻域内有墙，它就有可能是关键点
                if (hasSolidNeighbor(x, y)) {
                    interestMap[coordToIndex(x, y)] = true;
                }
            }
        }
    }

    /**
     * 获取路径
     * @param sx 起点 X
     * @param sy 起点 Y
     * @param tx 终点 X
     * @param ty 终点 Y
     * @return 路径点列表 (如果不连通返回空列表)
     */
    public static Ar<Point2> findPath(int sx, int sy, int tx, int ty) {
        Ar<Point2> rawPath = jpsSearch(sx, sy, tx, ty);
        
        // 如果没找到路径，返回空
        if (rawPath == null || rawPath.isEmpty()) return new Ar<>();
        
        // 最后一步：合并路径 (平滑处理)
        return smoothPath(rawPath);
    }

    private static Ar<Point2> jpsSearch(int sx, int sy, int tx, int ty) {
        // 边界检查
        if (isSolid(tx, ty)) return null; // 终点不可达

        PriorityQueue<Node> openList = new PriorityQueue<>();
        boolean[] closedMap = new boolean[W * H];
        Node[] nodeIndex = new Node[W * H]; // 用于快速获取已存在的节点

        Node startNode = new Node(sx, sy, null, 0, dist(sx, sy, tx, ty));
        openList.add(startNode);
        nodeIndex[coordToIndex(sx, sy)] = startNode;

        while (!openList.isEmpty()) {
            Node current = openList.poll();

            // 到达终点
            if (current.x == tx && current.y == ty) {
                return reconstructPath(current);
            }

            closedMap[coordToIndex(current.x, current.y)] = true;

            // 扩展后继节点 (使用 JPS 规则剪枝)
            identifySuccessors(current, tx, ty, openList, closedMap, nodeIndex);
        }

        return null; // 无法到达
    }

    /** JPS: 识别后继节点 */
    private static void identifySuccessors(Node current, int tx, int ty, 
                                         PriorityQueue<Node> openList, boolean[] closedMap, Node[] nodeIndex) {
        // JPS 的精髓：基于当前节点和父节点的方向进行搜索
        // 这里为了简化代码且适应全向移动，我们采用全邻居扫描然后进行跳跃检测
        // 标准 JPS 会根据父节点方向只扫描特定方向，这里简化为 8 向尝试跳跃
        
        int[] dirsX = {0, 0, -1, 1, -1, -1, 1, 1}; // 上下左右 + 4个对角
        int[] dirsY = {1, -1, 0, 0, 1, -1, 1, -1};

        for (int i = 0; i < 8; i++) {
            int dx = dirsX[i];
            int dy = dirsY[i];

            // 尝试向该方向跳跃，寻找跳点
            Point2 jumpPoint = jump(current.x, current.y, dx, dy, tx, ty);

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
                    // 由于 Java PriorityQueue 不支持动态更新权值，这里通常需要 remove 再 add
                    // 或者使用 lazy deletion。为了性能，如果是极小优化可以忽略，或者使用 Arc 的 BinaryHeap
                    openList.remove(neighbor);
                    openList.add(neighbor);
                }
            }
        }
    }

    /** JPS: 跳跃函数 (核心递归逻辑) */
    private static Point2 jump(int cx, int cy, int dx, int dy, int tx, int ty) {
        int nextX = cx + dx;
        int nextY = cy + dy;

        // 1. 越界或撞墙
        if (!isValid(nextX, nextY) || isSolid(nextX, nextY)) return null;

        // 2. 到达终点
        if (nextX == tx && nextY == ty) return new Point2(nextX, nextY);

        // 3. 检查是否有强制邻居 (Forced Neighbor)
        // 只有当该点是我们在 loadJPoint 中标记的 "感兴趣点" 时，才细致检查强制邻居
        // 这是一个巨大的优化，避免了每次都做复杂的墙体判断
        if (interestMap[coordToIndex(nextX, nextY)]) {
             // 检查对角线移动时的强制邻居逻辑
             if (dx != 0 && dy != 0) {
                 // 对角线移动: 检查水平和垂直分量是否被阻挡但下一格是空的
                 if ((isSolid(nextX - dx, nextY) && !isSolid(nextX - dx, nextY + dy)) ||
                     (isSolid(nextX, nextY - dy) && !isSolid(nextX + dx, nextY - dy))) {
                     return new Point2(nextX, nextY);
                 }
             } else {
                 // 直线移动
                 if (dx != 0) { // 水平
                     if ((isSolid(nextX, nextY - 1) && !isSolid(nextX + dx, nextY - 1)) ||
                         (isSolid(nextX, nextY + 1) && !isSolid(nextX + dx, nextY + 1))) {
                         return new Point2(nextX, nextY);
                     }
                 } else { // 垂直
                     if ((isSolid(nextX - 1, nextY) && !isSolid(nextX - 1, nextY + dy)) ||
                         (isSolid(nextX + 1, nextY) && !isSolid(nextX + 1, nextY + dy))) {
                         return new Point2(nextX, nextY);
                     }
                 }
             }
        }

        // 4. 对角线移动时，需要额外检查水平和垂直分量是否能产生跳点
        if (dx != 0 && dy != 0) {
            if (jump(nextX, nextY, dx, 0, tx, ty) != null || 
                jump(nextX, nextY, 0, dy, tx, ty) != null) {
                return new Point2(nextX, nextY);
            }
        }

        // 5. 递归跳跃
        // 如果这里不是跳点，继续沿当前方向跳
        return jump(nextX, nextY, dx, dy, tx, ty);
    }
    
    // --- 路径平滑 (Raycast / Line of Sight) ---
    
    private static Ar<Point2> smoothPath(Ar<Point2> path) {
        if (path.size <= 2) return path;

        Ar<Point2> smoothed = new Ar<>();
        smoothed.add(path.get(0)); // 起点

        int inputIndex = 0;
        while (inputIndex < path.size - 1) {
            // 尝试寻找最远的可视节点
            // 从当前点(inputIndex)开始，向后看，找到最后一个能直线到达的点
            int nextIndex = inputIndex + 1;
            for (int i = path.size - 1; i > inputIndex + 1; i--) {
                Point2 start = path.get(inputIndex);
                Point2 end = path.get(i);
                
                // 如果两点之间没有障碍物
                if (!lineCastSolid((int)start.x, (int)start.y, (int)end.x, (int)end.y)) {
                    nextIndex = i;
                    break;
                }
            }
            
            smoothed.add(path.get(nextIndex));
            inputIndex = nextIndex;
        }

        return smoothed;
    }

    /** 简单的视线检查 (Bresenham 直线算法变种) */
    private static boolean lineCastSolid(int x0, int y0, int x1, int y1) {
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

    private static Ar<Point2> reconstructPath(Node current) {
        Ar<Point2> path = new Ar<>();
        while (current != null) {
            path.add(new Point2(current.x, current.y));
            current = current.parent;
        }
        path.reverse(); // 从起点到终点
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