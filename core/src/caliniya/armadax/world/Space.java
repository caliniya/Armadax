package caliniya.armadax.world;

import caliniya.armadax.content.Floors;

public class Space {
    
    public int W;
    public int H;
    public Tile[] tiles;
    public int index;
    
    public Space(int W, int H){
        this.W = W;
        this.H = H;
        this.index = W * H;
        this.tiles = new Tile[index];
        
        for(int i = 0; i < index; ++i) {
            tiles[i] = new Tile(i % W, i / W){{
                floor = Floors.space;
            }};
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
    
    //根据坐标获取Tile
    public Tile getTile(int x, int y) {
        if (x >= 0 && x < W && y >= 0 && y < H) {
            return tiles[coordToIndex(x, y)];
        }
        return null;
    }
    
    //检查坐标是否有效
    public boolean isValidCoord(int x, int y) {
        return x >= 0 && x < W && y >= 0 && y < H;
    }
}