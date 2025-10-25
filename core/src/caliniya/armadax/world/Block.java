package caliniya.armadax.world;

import caliniya.armadax.world.*;

public class Block {
    public String name;
    public int[] size;
    public boolean solid = false;
    public boolean buildable = true;
    public float health = 100;
    //public TextureRegion region;
    
    public void Block() {
    	
    }
    
    public boolean isMultiblock() {
        return
    }
    
    public Block createBuilding() {
        return new Block();
    }
}