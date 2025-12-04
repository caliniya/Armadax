package caliniya.armadax;

import caliniya.armadax.content.*;
import caliniya.armadax.screen.*;
import caliniya.armadax.base.system.*;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

public class Armadax extends Game {
    
    public boolean inited;//所以内容已经初始化
    private Assets assets;
    
    @Override
    public void create() {
        assets = Assets.getInstance();
        assets.loadAssets();
        Blocks.load();
        Floors.load();
    }

    @Override
    public void render() {
        super.render();
        if(assets.update()){
        	inited = true;
        }else{
            assets.update();
        }
    }
    
    @Override
    public void dispose() {
        super.dispose();
        assets.dispose();
    }
}
