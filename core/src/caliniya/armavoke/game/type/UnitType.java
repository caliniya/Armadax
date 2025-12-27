package caliniya.armavoke.game.type;

import arc.Core;
import arc.graphics.g2d.TextureRegion;
import caliniya.armavoke.base.game.ContentType;
import caliniya.armavoke.base.type.CType;
import caliniya.armavoke.game.Unit;

public class UnitType extends ContentType {

  public float speed = 5f, health = 100f;
  public float w = 100f, h = 180f;

  // 渲染资源
  public TextureRegion region , cell;

  public UnitType(String name) {
    super(name, CType.Unit);
  }

  // 加载资源 (在 Assets 加载完成后调用)
  public void load() {
    region = Core.atlas.find(name, "white");
    cell = Core.atlas.find(name + "-cell" , "white");
  }

  // 工厂方法：创建一个该类型的单位
  public Unit create() {
    return Unit.create(this);
  }
  
  //带坐标
  public Unit create(float x , float y) {
    return Unit.create(this , x, y);
  }
}
