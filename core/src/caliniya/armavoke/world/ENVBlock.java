package caliniya.armavoke.world;

import arc.Core;
import arc.graphics.g2d.TextureRegion;
import caliniya.armavoke.base.game.ContentType;
import caliniya.armavoke.base.type.CType;

public class ENVBlock extends ContentType {
    
    // 存储纹理区域
    public TextureRegion region;

    public ENVBlock(String name){
        super(name, CType.ENVBlock);
    }
    
    // 资源加载方法
    public void load() {
        region = Core.atlas.find(name);
    }
    
    public Block createBlock() {
        return new Block();
    }
}