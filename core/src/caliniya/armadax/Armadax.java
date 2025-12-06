package caliniya.armadax;

import arc.ApplicationCore;
import arc.ApplicationListener;
import arc.Core;
import arc.assets.AssetManager;
import arc.assets.Loadable;
import arc.graphics.g2d.TextureAtlas;
import arc.util.Log;
import caliniya.armadax.base.type.*;
import caliniya.armadax.content.*;
//import caliniya.armadax.base.system.*;

public class Armadax extends ApplicationCore {
    
    @Override
    public void setup() {
        Core.assets = new AssetManager();
        Core.assets.load("sprites/sprites.aatls" , TextureAtlas.class);
    }
    
    
    @Override
    public void init() {
        super.init();
        Blocks.load();
        Floors.load();
    }

    @Override
    public void update() {
        super.update();
        if(!Core.assets.update()){
            Core.assets.update();
            Log.info("A1");
        }
    }
    
    @Override
    public void dispose() {
        super.dispose();
        Core.assets.dispose();
    }
}
