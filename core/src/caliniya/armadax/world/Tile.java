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
        //this.floor = Floors.stone;
        //this.block = Blocks.air;
    }
    /**
    public boolean solid() {
        return block.solid || (building != null && building.solid());
    }
    
    public boolean canBuildOn() {
        return floor.buildable && block == Blocks.air;
    }*/
}