package caliniya.armavoke.game.type;

import arc.Core;
import arc.graphics.g2d.TextureRegion;
import caliniya.armavoke.game.Unit;

public class UnitType {
    public String name;
    
    // 基础属性
    public float health = 100f;
    public float speed = 1f;
    public float hitSize = 10f; // 碰撞体积
    
    // 渲染资源
    public TextureRegion region;
    
    public UnitType(String name) {
        this.name = name;
    }
    
    // 加载资源 (在 Assets 加载完成后调用)
    public void load() {
        // 假设图集里单位名字叫 "unit-名字"
        region = Core.atlas.find(name, "white"); 
    }
    
    // 工厂方法：创建一个该类型的单位
    public Unit create() {
        return Unit.create(this);
    }
}