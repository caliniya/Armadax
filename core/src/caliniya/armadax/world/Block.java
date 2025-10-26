package caliniya.armadax.world;

import caliniya.armadax.world.*;

public class Block {
    public String name;
    public float W;
    public float H;
    public boolean solid = false;
    public boolean buildable = true;
    public float health = 100;
    //public TextureRegion region;
    
    public void Block() {
    	
    }
    
    public boolean isMultiblock() {
        return true;
    }
    
    public Block createBuilding() {
        return new Block();
    }
}