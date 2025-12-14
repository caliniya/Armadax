package caliniya.armavoke.world;

import caliniya.armavoke.base.game.ContentType;
import caliniya.armavoke.base.type.CType;
import caliniya.armavoke.world.*;

public class ENVBlock extends ContentType {
    
    public ENVBlock(String Name){
    	super(Name , CType.ENVBlock);
    }
    
    public Block CreatBlock() {
        return new Block();
    }
}