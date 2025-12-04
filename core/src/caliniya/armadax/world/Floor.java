package caliniya.armadax.world;

import caliniya.armadax.base.game.ContentType;
import caliniya.armadax.base.type.CType;
import caliniya.armadax.world.*;

public class Floor extends ContentType {
    
    public String name;
    
    public int X;
    public int Y;
    
    public Floor(String Name){
        super(Name , CType.Block);
    }
	
}