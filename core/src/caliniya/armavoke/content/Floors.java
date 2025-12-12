package caliniya.armavoke.content;

import caliniya.armavoke.world.Floor;

public class Floors {

  public static Floor TestFloor, space;

  public static void load() {
    TestFloor = new Floor("TestFloor");
    space = new Floor("space");
  }
}
