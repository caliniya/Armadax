package caliniya.armadax.base.game;

import caliniya.armadax.base.type.*;

public class ContentType {
	
    public String name;
    public String type;
    
    public ContentType(String name , CType.type type){
        this.name = name;
        this.type = type.name();
    }
    
    public String getLName(){
        return (type + name);
    }
    
}