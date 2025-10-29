package caliniya.armadax;

import caliniya.armadax.srceen.MainGameSrceen;
import caliniya.armadax.base.text.Text;
import caliniya.armadax.system.Assets;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;

import caliniya.armadax.base.language.LanguageManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

public class Armadax extends Game {
    private MainGameSrceen MainGameSrceen;
    private int aaa;
    private Assets assets;
    @Override
    public void create() {
        aaa = 1;
        assets = Assets.getInstance();
        assets.loadAssets();
    }

    @Override
    public void render() {
        super.render();
        assets.update();
        if(assets.update() && aaa == 1) {
        	MainGameSrceen = new MainGameSrceen();
            setScreen(MainGameSrceen);
            aaa = 3;
        } 
    }
    
    @Override
    public void dispose() {
        super.dispose();
    }
}
