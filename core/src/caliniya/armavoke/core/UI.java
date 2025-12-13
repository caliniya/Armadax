package caliniya.armavoke.core;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import caliniya.armavoke.ui.fragment.MenuFragment;

import static arc.Core.*;

public class UI {

    public static void Menu() {
        scene.clear();
        new MenuFragment().build();
    }

    // 注意：这个方法需要在你的主渲染循环(ApplicationListener.draw)中每一帧调用
    public static void Loading() {
        

        // 2. 设置投影矩阵 (Orthographic Projection)
        // 这一步至关重要，它将绘图坐标系映射到屏幕像素 (0,0 在左下角)
        //Draw.proj().setOrtho(0, 0, graphics.getWidth(), graphics.getHeight());

        // 3. 计算尺寸和位置
        float screenW = graphics.getWidth();
        float screenH = graphics.getHeight();
        float centerX = screenW / 2f;
        float centerY = screenH / 2f;

        // 定义进度条的外观
        float barWidth = 300f;  // 总宽度
        float barHeight = 20f;  // 总高度
        float padding = 4f;     // 边框与填充之间的空隙

        // 4. 设置颜色为白色
        Draw.color(Color.white);

        // 5. 绘制外边框 (Lines 用于画线条/空心形状)
        Lines.stroke(2f); // 设置线条粗细
        // Lines.rect 的前两个参数是矩形的中心点还是左下角取决于具体方法，
        // 但在 Arc 中，Lines.rect(x, y, w, h) 通常是以 (x, y) 为左下角或者中心，
        // 为了稳妥，我们这里手动计算坐标绘制空心矩形：
        Lines.rect(centerX - barWidth / 2f, centerY - barHeight / 2f, barWidth, barHeight);

        // 6. 绘制内部进度填充 (Fill 用于画实心形状)
        // 获取资源加载进度 (0.0 到 1.0)
        float progress = assets.getProgress();

        // 只有进度大于0才绘制内部
        if (progress > 0.01f) {
            // 计算内部填充条的完整宽度
            float maxFillWidth = barWidth - padding * 2;
            float currentFillWidth = maxFillWidth * progress;
            float fillHeight = barHeight - padding * 2;

            // 计算填充矩形的中心点
            // 我们希望它从左向右增长。
            // 填充条的左边缘位置 = (屏幕中心X - 总宽一半 + 内边距)
            float leftEdgeX = centerX - barWidth / 2f + padding;
            
            // Fill.rect 默认是以 (x, y) 为矩形**中心点**绘制的
            // 所以我们需要计算出当前填充矩形的中心点位置
            float drawCenterX = leftEdgeX + currentFillWidth / 2f;

            Fill.rect(drawCenterX, centerY, currentFillWidth, fillHeight);
        }

        // 7. 强制刷新绘制批处理 (将指令发送给 GPU)
        // 如果没有这一行，你可能什么都看不到
        Draw.flush();
    }
}