package caliniya.armavoke.base.game;

import caliniya.armavoke.base.type.*;
import caliniya.armavoke.core.*;

public class ContentType {
	
    public String name;
    public String type;
    
    public ContentType(String name , CType type){
        this.name = name;
        this.type = type.name();
        ContentVar.add(this);
    }
    
    public String getLName(){
        return (type + "." +name);
    }
    
}