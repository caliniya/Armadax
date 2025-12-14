package caliniya.armavoke.world;

import caliniya.armavoke.base.game.ContentType;
import caliniya.armavoke.base.type.CType;
import caliniya.armavoke.world.*;

public class Floor extends ContentType {
    
    public int X;
    public int Y;
    
    public Floor(String Name){
        super(Name , CType.Floor);
    }
	
}