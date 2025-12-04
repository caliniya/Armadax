package caliniya.armadax.content;
import caliniya.armadax.base.system.*;
import caliniya.armadax.world.Floor;

public class Floors {
    
    public static Floor
    TestFloor , space
    ;
	
    public static void load(){
        TestFloor = new Floor("TestFloor");
        space = new Floor("space");
    }
    
}