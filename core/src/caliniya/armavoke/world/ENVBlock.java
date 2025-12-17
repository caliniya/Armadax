package caliniya.armavoke.world;

import arc.Core;
import arc.graphics.g2d.TextureRegion;
import caliniya.armavoke.base.game.ContentType;
import caliniya.armavoke.base.type.CType;

public class ENVBlock extends ContentType {
    
    // 【新增】存储纹理区域
    public TextureRegion region;

    public ENVBlock(String name){
        super(name, CType.ENVBlock);
    }
    
    // 【新增】资源加载方法
    public void load() {
        region = Core.atlas.find(name);
    }
    
    // 建议将方法名首字母小写以符合 Java 规范: CreatBlock -> createBlock
    public Block createBlock() {
        return new Block();
    }
}