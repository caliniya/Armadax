package caliniya.armavoke.base.game;

import caliniya.armavoke.base.type.*;

public class ContentType {
	
    public String name;
    public String type;
    
    public ContentType(String name , CType type){
        this.name = name;
        this.type = type.name();
    }
    
    public String getLName(){
        return (type + "." +name);
    }
    
}