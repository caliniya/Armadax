package caliniya.armadax.game.data;

import caliniya.armadax.game.Unit;
import caliniya.armadax.world.Floor;
import caliniya.armadax.world.World;
import com.badlogic.gdx.Gdx;

public class WorldData {
    //游戏地图的世界数据
    public static WorldData Instance;
    public World world;//静态地图内容
    public Floor[] floors;
    public Unit[] units;
    
    static{
        Instance = new WorldData();
    }
    
    public WorldData getInstance(){
        return Instance;
    }
    
    private World getWorld(){
        return world;
    }
    
    public void initWorld(int worldW , int worldH, boolean space){
        world = new World();
    }
	
}