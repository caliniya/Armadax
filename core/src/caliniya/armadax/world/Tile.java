package caliniya.armadax.world;

import caliniya.armadax.world.*;
import caliniya.armadax.content.*;

public class Tile {
    public int x, y;
    public Block block;
    public Floor floor;
    
    public Tile(int x, int y) {
        this.x = x;
        this.y = y;
        //this.floor =
        this.block = Blocks.test;
    }
    
    public boolean canBuildOn() {
        return true;
    }
}