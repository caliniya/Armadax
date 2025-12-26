package caliniya.armavoke.base.type;

import caliniya.armavoke.game.data.TeamData;
import caliniya.armavoke.core.*;

public enum TeamTypes {
    Sharded,  // 玩家默认团队 (碎片/盟军)
    Crux,     // 敌人默认团队 (核心/十字军)
    Derelict, // 废弃/中立 (通常不攻击)
    Green,    // 其他势力...
    Blue;

    /** 快捷获取该团队的运行时数据 */
    public TeamData data() {
        return Teams.get(this);
    }
}