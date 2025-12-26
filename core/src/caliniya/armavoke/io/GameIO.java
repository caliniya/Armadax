package caliniya.armavoke.io;

import arc.files.Fi;
import arc.struct.ObjectIntMap; // Arc的高效 Map
import arc.struct.Seq;          // Arc的高效 List
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

      // 头信息
      w.i(SAVE_VERSION);
      w.i(WorldData.world.W);
      w.i(WorldData.world.H);

      // 调色板
      // 我们需要统计地图里究竟用到了哪些方块，给它们分配一个短 ID (0, 1, 2...)
      
      // 临时存储唯一的 Floor 和 Block
      Seq<Floor> floorPalette = new Seq<>();
      Seq<ENVBlock> blockPalette = new Seq<>();
      
      // 映射表：对象 -> ID
      ObjectIntMap<Floor> floorMap = new ObjectIntMap<>();
      ObjectIntMap<ENVBlock> blockMap = new ObjectIntMap<>();

      int totalTiles = WorldData.world.W * WorldData.world.H;

      // 第一遍扫描：收集所有出现的非空内容
      for (int i = 0; i < totalTiles; i++) {
        Floor f = WorldData.world.floors.get(i);
        ENVBlock b = WorldData.world.envblocks.get(i);

        if (f != null && !floorMap.containsKey(f)) {
            floorMap.put(f, floorPalette.size);
            floorPalette.add(f);
        }
        
        if (b != null && !blockMap.containsKey(b)) {
            blockMap.put(b, blockPalette.size);
            blockPalette.add(b);
        }
      }
      
      // 写入 Floor 调色板
      w.s(floorPalette.size); // 写入数量 (short)
      for (Floor f : floorPalette) {
          w.str(f.getLName());
      }

      // 写入 Block 调色板
      w.s(blockPalette.size); // 写入数量 (short)
      for (ENVBlock b : blockPalette) {
          w.str(b.getLName());
      }

      // 写入地图索引数据
      // 第二遍扫描：写入 ID
      for (int i = 0; i < totalTiles; i++) {
        Floor f = WorldData.world.floors.get(i);
        ENVBlock b = WorldData.world.envblocks.get(i);

        // 如果是 null 写 -1，否则写 Map 里的 ID
        w.s(f == null ? -1 : floorMap.get(f));
        w.s(b == null ? -1 : blockMap.get(b));
      }

      // 写入单位
      int validUnitCount = 0;
      for (Unit u : WorldData.units) {
        if (u != null && u.health > 0) validUnitCount++;
      }
      w.i(validUnitCount);

      for (Unit u : WorldData.units) {
        if (u == null || u.health <= 0) continue;
        w.str(u.type.getLName());
        u.write(w);
      }

      Log.info("Saved world to @ (Palette Mode)", file.path());

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

      // 清理并初始化
      WorldData.reBuildAll(width, height);

      // 读取调色板
      
      // 读取 Floor 映射表
      int floorCount = r.s();
      Floor[] floorLookup = new Floor[floorCount];
      for(int i=0; i<floorCount; i++){
          String name = r.str();
          floorLookup[i] = ContentVar.get(name, Floor.class);
      }
      
      // 读取 Block 映射表
      int blockCount = r.s();
      ENVBlock[] blockLookup = new ENVBlock[blockCount];
      for(int i=0; i<blockCount; i++){
          String name = r.str();
          blockLookup[i] = ContentVar.get(name, ENVBlock.class);
      }

      // 读取地图索引数据
      int total = width * height;
      for (int i = 0; i < total; i++) {
        short floorId = r.s();
        short blockId = r.s();

        // 通过 ID 查数组，-1 则为 null
        Floor floor = (floorId == -1) ? null : floorLookup[floorId];
        ENVBlock block = (blockId == -1) ? null : blockLookup[blockId];

        WorldData.world.floors.add(floor);
        WorldData.world.envblocks.add(block);
      }

      //读取单位
      int unitCount = r.i();
      for (int i = 0; i < unitCount; i++) {
        String typeName = r.str();
        UnitType type = ContentVar.get(typeName, UnitType.class);

        if (type != null) {
          Unit u = Unit.create(type);
          u.read(r);
        } else {
          // TODO: 如何解决
          Log.err("Unknown unit type: @", typeName);
        }
      }
      
      // 后处理 
      RouteData.init();
      // MapRender.rebuild(); 记得调用

      Log.info("Loaded world from @", file.path());

    } catch (IOException e) {
      Log.err("Load failed", e);
      WorldData.initWorld();
    }
  }
}