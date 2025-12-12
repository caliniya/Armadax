package caliniya.armavoke.world;

import caliniya.armavoke.base.game.ContentType;
import caliniya.armavoke.base.type.CType;
import caliniya.armavoke.world.*;

public class Block extends ContentType {
    
    public float size = 2;
    public boolean buildable = true;
    public float health = 10;
    
    public Block(){
        this("test-building" , CType.Block);
    }
    
    public Block(String BlockName , CType type){
    	super(BlockName , type);
    }
    
    public boolean isMultiblock() {
        return size == 1;
    }
    
    public Block CreatBlock() {
        return new Block();
    }
}