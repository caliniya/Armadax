package caliniya.armavoke.map;

import arc.Core;
import arc.files.Fi;
import arc.struct.Seq;
import caliniya.armavoke.io.GameIO;

public class Maps {
    // 所有的地图列表
    public static Seq<Map> all = new Seq<>();

    /** 扫描 saves 目录并加载元数据 */
    public static void load() {
        all.clear();
        
        Fi mapDir = Core.files.local("saves/");
        if (!mapDir.exists()) mapDir.mkdirs();

        // 遍历所有 .dat 文件
        for (Fi file : mapDir.list()) {
            if (file.extension().equals("dat")) {
                // 只读取元数据，速度很快
                Map map = GameIO.readMeta(file);
                if (map != null) {
                    all.add(map);
                }
            }
        }
        
        // 排序
        all.sort();
    }
    
    /** 添加一个新地图(例如刚保存后)，不用重新扫描全部 */
    public static void add(Fi file) {
        Map map = GameIO.readMeta(file);
        if(map != null) {
            all.add(map);
            all.sort();
        }
    }
}