package caliniya.armavoke.core;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import caliniya.armavoke.ui.fragment.*;

import static arc.Core.*;

public class UI {
  // 保持单例
  public static DebugFragment de;
  // 跟踪当前是否显示
  private static boolean isDebugShown = false;

  public static void Menu() {
    scene.clear();
    new MenuFragment().build();
    // 菜单界面可能不需要显示 debug，可以移除，或者根据需求保留
    if (isDebugShown) de.add(); 
  }

  // 加载界面渲染逻辑 (保持不变)
  public static void Loading() {
    float screenW = graphics.getWidth();
    float screenH = graphics.getHeight();
    float centerX = screenW / 2f;
    float centerY = screenH / 2f;

    float barWidth = 300f; 
    float barHeight = 20f; 
    float padding = 4f; 

    Draw.color(Color.white);
    Lines.stroke(2f);
    Lines.rect(centerX - barWidth / 2f, centerY - barHeight / 2f, barWidth, barHeight);

    float progress = assets.getProgress();
    if (progress > 0.01f) {
      float maxFillWidth = barWidth - padding * 2;
      float currentFillWidth = maxFillWidth * progress;
      float fillHeight = barHeight - padding * 2;
      float leftEdgeX = centerX - barWidth / 2f + padding;
      float drawCenterX = leftEdgeX + currentFillWidth / 2f;
      Fill.rect(drawCenterX, centerY, currentFillWidth, fillHeight);
    }
    Draw.flush();
  }

  public static void Game() {
    scene.clear();
    new GameFragment().build();
    
    // 如果之前开启了 debug，进入游戏时需要重新添加回 scene
    if (isDebugShown && de != null) {
        de.add();
    }
  }

  /**
   * 切换调试信息的显示状态
   */
  public static void Debug() {
    // 懒加载初始化
    if(de == null) {
        de = new DebugFragment();
    }

    if (isDebugShown) {
        // 如果当前是显示的，则关闭
        de.remove();
        isDebugShown = false;
    } else {
        // 如果当前是关闭的，则显示
        de.add();
        isDebugShown = true;
    }
  }
}