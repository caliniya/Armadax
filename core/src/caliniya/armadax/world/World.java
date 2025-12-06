package caliniya.armadax.world;

import caliniya.armadax.base.tool.Ar;
import caliniya.armadax.content.Floors;
import caliniya.armadax.game.Unit;

//这个类表示一个世界
//数据不在这里，数据在WorrldData里
//在初始化世界数据时同步创建一个此世界对象
//这个类表示的是世界静态内容
public class World {
    public boolean space;
    public int W;
    public int H;
    public int index;
    
    public boolean test = true;
    
    public Ar<Floor> floors = new Ar<Floor>(10000);
    
    public World(){
    }
    
    public World(int W, int H,boolean space){
        this.W = W;
        this.H = H;
        this.space = space;
        this.index = W * H;
        test = false;
        
    }
    
    public void init(){
        if(test) {
        	for(int i = 0; i < 10000; ++i) {
        		floors.add(Floors.TestFloor);
        	}
        }
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