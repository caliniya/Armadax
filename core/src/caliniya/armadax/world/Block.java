package caliniya.armadax.world;

import caliniya.armadax.base.game.ContentType;
import caliniya.armadax.base.type.CType;
import caliniya.armadax.world.*;

public class Block extends ContentType {
    
    public float size = 2;
    public boolean buildable = true;
    public float health = 10;
    
    public Block(){
        this("test-building" , CType.type.Block);
    }
    
    public Block(String BlockName , CType.type type){
    	super(BlockName , type);
    }
    
    public boolean isMultiblock() {
        return size == 1;
    }
    
    public Block CreatBlock() {
        return new Block();
    }
}