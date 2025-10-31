package caliniya.armadax;

import caliniya.armadax.base.language.LanguageManager;
import caliniya.armadax.base.text.Text;
import caliniya.armadax.content.*;
import caliniya.armadax.screen.*;
import caliniya.armadax.system.Assets;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

public class Armadax extends Game {
    private Screen mapTest;
    private int aaa;
    private Assets assets;
    @Override
    public void create() {
        aaa = 1;
        assets = Assets.getInstance();
        assets.loadAssets();
        Blocks.load();
        Floors.load();
    }

    @Override
    public void render() {
        super.render();
        assets.update();
        if(assets.update() && aaa == 1) {
        	mapTest = new MapTest();
            setScreen(mapTest);
            aaa = 3;
        } 
    }
    
    @Override
    public void dispose() {
        super.dispose();
    }
}
