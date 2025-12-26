package caliniya.armavoke.base.type;

import caliniya.armavoke.game.data.TeamData;
import caliniya.armavoke.core.*;

public enum TeamTypes {
  Evoke,
  Veto,
  Abort, //中止(废墟)
  Mutex
  ;

  /** 快捷获取该团队的运行时数据 */
  public TeamData data() {
    return Teams.get(this);
  }
}
