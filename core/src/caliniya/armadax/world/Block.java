package caliniya.armadax.world;

import caliniya.armadax.world.*;

public class Block {
    public String name = "test-building";
    public float size = 2;
    public boolean buildable = true;
    public float health = 10;
    
    public Block() {
    	
    }
    
    public boolean isMultiblock() {
        return size == 1;
    }
    
    public Block Block() {
        return new Block();
    }
}