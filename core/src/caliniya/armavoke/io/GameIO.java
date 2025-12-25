package caliniya.armavoke.io;

import arc.files.Fi;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.io.Reads;
import arc.util.io.Writes;
import caliniya.armavoke.base.tool.Ar;
import caliniya.armavoke.core.*;
import caliniya.armavoke.game.Unit;
import caliniya.armavoke.game.data.*;
import caliniya.armavoke.game.type.UnitType;
import caliniya.armavoke.world.ENVBlock;
import caliniya.armavoke.world.Floor;

import caliniya.armavoke.world.World;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class GameIO {
  private static final int SAVE_VERSION = 1;

  public static void save(Fi file) {
    try (DataOutputStream stream = new DataOutputStream(file.write(false))) {
      Writes w = new Writes(stream);

      // 头文件信息
      w.i(SAVE_VERSION);
      w.i(WorldData.world.W);
      w.i(WorldData.world.H);

      // 调色板生成 (Palette Generation)
      // 找出所有用到的 Floor 和 ENVBlock，生成映射表，减小文件体积
      Ar<Floor> usedFloors = new Ar<>();
      Ar<ENVBlock> usedBlocks = new Ar<>();

      // 写入地图数据
      for (int i = 0; i < WorldData.world.W * WorldData.world.H; i++) {
        Floor floor = WorldData.world.floors.get(i);
        ENVBlock block = WorldData.world.envblocks.get(i);

        // 写入全名 (例如 "Floor.xxx")
        // 如果是 null, 写入 "null"
        w.str(floor != null ? floor.getLName() : "null");
        w.str(block != null ? block.getLName() : "null");
      }

      // --- 4. 写入单位 ---
      // 【关键修复】先统计有效单位数量，防止 null 或死单位干扰
      int validUnitCount = 0;
      for (Unit u : WorldData.units) {
        if (u != null && u.health > 0) validUnitCount++;
      }
      w.i(validUnitCount); // 写入真实的有效数量

      for (Unit u : WorldData.units) {
        if (u == null || u.health <= 0) continue; // 跳过无效单位

        w.str(u.type.getLName());
        u.write(w);
      }
      // ...

      Log.info("Saved world to @", file.path());

    } catch (IOException e) {
      Log.err("Save failed", e);
    }
  }

  public static void load(Fi file) {
    try (DataInputStream stream = new DataInputStream(file.read())) {
      Reads r = new Reads(stream);

      // 头信息
      int ver = r.i();
      int width = r.i();
      int height = r.i();

      // 初始化空世界
      WorldData.reBuildAll(width, height);

      // 读取地图数据
      int total = width * height;
      for (int i = 0; i < total; i++) {
        String floorName = r.str();
        String blockName = r.str();

        // 查表
        Floor floor = ContentVar.get(floorName, Floor.class);
        ENVBlock block = ContentVar.get(blockName, ENVBlock.class);

        WorldData.world.floors.add(floor);
        WorldData.world.envblocks.add(block);
      }

      // 读取单位
      int unitCount = r.i();
      for (int i = 0; i < unitCount; i++) {
        String typeName = r.str();
        UnitType type = ContentVar.get(typeName, UnitType.class);

        if (type != null) {
          Unit u = Unit.create(type);
          u.read(r); // 恢复数据
        } else {
          Log.err("Unknown unit type: @", typeName);
          // 实际上这里会导致后续数据读取错位(
        }
      }

      Log.info("Loaded world from @", file.path());

    } catch (IOException e) {
      Log.err("Load failed", e);
      WorldData.initWorld(); // 失败则回退到测试图()
    }
  }
}
