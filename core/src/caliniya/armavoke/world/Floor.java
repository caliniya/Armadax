package caliniya.armavoke.world;

import arc.Core;
import arc.graphics.g2d.TextureRegion;
import caliniya.armavoke.base.game.ContentType;
import caliniya.armavoke.base.type.CType;

public class Floor extends ContentType {
    
    // 【新增】存储纹理区域，避免每次渲染都调用 Core.atlas.find
    public TextureRegion region;

    public Floor(String name){
        super(name, CType.Floor);
    }
    
    // 【新增】资源加载方法
    // 请在游戏启动的 LoadContent 阶段调用此方法
    public void load() {
        // 查找与 name 同名的纹理
        region = Core.atlas.find(name);
        
        // 可选：如果没找到，使用默认的 "error" 纹理，防止空指针
        if (!Core.atlas.isFound(region)) {
            // region = Core.atlas.find("error"); 
        }
    }
}