package caliniya.armadax;

import arc.ApplicationCore;
import arc.ApplicationListener;
import arc.Core;
import arc.assets.Loadable;
import caliniya.armadax.base.type.*;
import caliniya.armadax.content.*;
import caliniya.armadax.base.system.*;

public class Armadax extends ApplicationCore {
    
    private Assets assets;
    
    @Override
    public void add(ApplicationListener module){
        super.add(module);

        //autoload modules when necessary
        if(module instanceof Loadable l){
            assets.load(l);
        }
    }
    
    @Override
    public void setup() {
        // TODO
    }
    
    
    @Override
    public void init() {
        super.init();
        assets = Assets.getInstance();
        assets.loadAssets();
        Blocks.load();
        Floors.load();
    }

    @Override
    public void update() {
        super.update();
        if(!assets.update()){
            assets.update();
        }
    }
    
    @Override
    public void dispose() {
        super.dispose();
        assets.dispose();
    }
}
