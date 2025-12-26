package caliniya.armavoke.game.data;

import caliniya.armavoke.base.tool.Ar;
import caliniya.armavoke.base.type.TeamTypes;
import caliniya.armavoke.game.Unit;

public class TeamData {
    public final TeamTypes team; // 反向引用
    
    // 该团队下的所有单位
    // 主要用于索敌循环：enemies.get(i).team.data().units
    public Ar<Unit> units = new Ar<>();
    
    // 未来可以添加：
    // public Ar<Building> buildings = new Ar<>();
    // public boolean isEnemy(Team other) { ... } // 敌对关系表

    public TeamData(TeamTypes team) {
        this.team = team;
    }
}