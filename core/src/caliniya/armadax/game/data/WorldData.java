package caliniya.armadax.game;
import caliniya.armadax.world.Floor;
import caliniya.armadax.world.World;

public class WorldData {
    public static WorldData Instance;
    public World world;
    public Floor[] floors;
    public Unit[] units;
    
    static{
        Instance = new WorldData();
    }
    
    public WorldData getInstance(){
        return Instance;
    }
    
    private WorldData(){
    }
    
    public void initWorld(int worldW , int worldH, boolean space){
        world = new World(worldW , worldH ,space);
    }
	
}