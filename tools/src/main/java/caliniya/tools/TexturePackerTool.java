package caliniya.tools;

import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.tools.texturepacker.TexturePacker.Settings;

import java.io.File;

public class TexturePackerTool {
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: TexturePackerTool <inputDir> <outputDir> <packName> [settingsFile]");
            System.out.println("Example: TexturePackerTool ./input ./output game_textures");
            System.exit(1);
        }

        File inputDir = new File(args[0]);
        File outputDir = new File(args[1]);
        String packName = args[2];
        
        // 验证输入目录
        if (!inputDir.exists() || !inputDir.isDirectory()) {
            System.err.println("Error: Input directory does not exist or is not a directory");
            System.exit(1);
        }
        
        // 清理旧的打包产物
        cleanPreviousOutput(outputDir, packName);
        
        // 创建设置
        Settings settings = new Settings();
        
        // 默认设置
        settings.duplicatePadding = true;
        settings.combineSubdirectories = true;
        //settings.flattenPaths = true;
        settings.maxWidth = 2048;
        settings.maxHeight = 2048;
        settings.fast = true;
        
        // 对于 NinePatch 图片，不能去除空白区域
        settings.stripWhitespaceX = false;
        settings.stripWhitespaceY = false;
        
        //  NinePatch 的设置
        settings.paddingX = 2; 
        settings.paddingY = 2;
        settings.alias = true;
        
        // 确保输出目录存在
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            System.err.println("Error: Failed to create output directory");
            System.exit(1);
        }
        
        // 执行打包
        try {
            System.out.println("Starting texture packing...");
            System.out.println("Input: " + inputDir.getAbsolutePath());
            System.out.println("Output: " + outputDir.getAbsolutePath());
            System.out.println("Detecting NinePatch (.9.png) files...");
            
            TexturePacker.process(settings, 
                inputDir.getAbsolutePath(), 
                outputDir.getAbsolutePath(), 
                packName);
            
            System.out.println("Texture packing completed successfully!");
            System.out.println("Output files:");
            System.out.println("- " + new File(outputDir, packName + ".png").getAbsolutePath());
            System.out.println("- " + new File(outputDir, packName + ".atlas").getAbsolutePath());
            
            // 检查是否包含 NinePatch 图片
            checkNinePatchFiles(inputDir);
            
        } catch (Exception e) {
            System.err.println("Error during texture packing: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * 检查输入目录中是否包含 NinePatch 文件
     */
    private static void checkNinePatchFiles(File inputDir) {
        int ninePatchCount = countNinePatchFiles(inputDir);
        if (ninePatchCount > 0) {
            System.out.println("Found " + ninePatchCount + " NinePatch (.9.png) files");
            System.out.println("Note: NinePatch files require special handling in your game code:");
            System.out.println("- Use TextureAtlas.createPatch() to create NinePatch instances");
            System.out.println("- Or use skin.getPatch() if using Scene2D Skin");
        }
    }
    
    /**
     * 计算目录中的 NinePatch 文件数量
     */
    private static int countNinePatchFiles(File dir) {
        int count = 0;
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        count += countNinePatchFiles(file);
                    } else if (file.getName().endsWith(".9.png")) {
                        count++;
                        System.out.println("  Found NinePatch: " + file.getName());
                    }
                }
            }
        }
        return count;
    }
    
    /**
     * 清理之前打包生成的文件
     */
    private static void cleanPreviousOutput(File outputDir, String packName) {
        if (!outputDir.exists()) {
            return;
        }
        
        System.out.println("Cleaning previous output files...");
        
        // 删除主要的打包文件
        deleteFile(new File(outputDir, packName + ".png"));
        deleteFile(new File(outputDir, packName + ".atlas"));
        
        // 删除可能的分页文件
        File[] files = outputDir.listFiles();
        if (files != null) {
            for (File file : files) {
                String fileName = file.getName();
                if (fileName.startsWith(packName) && 
                    (fileName.endsWith(".png") || fileName.endsWith(".atlas"))) {
                    deleteFile(file);
                }
            }
        }
    }
    
    /**
     * 安全删除文件
     */
    private static void deleteFile(File file) {
        if (file.exists()) {
            if (file.delete()) {
                System.out.println("Deleted: " + file.getAbsolutePath());
            } else {
                System.err.println("Failed to delete: " + file.getAbsolutePath());
            }
        }
    }
}