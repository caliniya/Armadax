package caliniya.armadax.world;

import caliniya.armadax.content.Floors;
import caliniya.armadax.game.Unit;

//这个类表示一个世界
//数据不在这里，数据在WorrldData里
//在初始化世界数据时同步创建一个此世界对象
//此类用于实现世界数据查询工具
public class World {
    public boolean Space;
    public int W;
    public int H;
    public int index;
    
    public World(int W, int H,boolean space){
        this.W = W;
        this.H = H;
        this.Space = space;
        this.index = W * H;
    }
    
    public int indexToX(int ind){
        return ind % W;
    }
    
    public int indexToY(int ind){
        return ind / W;
    }
    
    //根据坐标获取索引
    public int coordToIndex(int x, int y) {
        return y * W + x;
    }
    
    //检查坐标是否有效
    public boolean isValidCoord(int x, int y) {
        return x >= 0 && x < W && y >= 0 && y < H;
    }
}