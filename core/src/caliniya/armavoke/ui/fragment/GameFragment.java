package caliniya.armavoke.ui.fragment;

import arc.Core;
import arc.Events;
import arc.scene.Group;
import arc.scene.ui.layout.Table;
import caliniya.armavoke.base.type.EventType;
import caliniya.armavoke.ui.Button;

public class GameFragment {
    
    // 用来记录当前 UI 上的按钮状态
    private boolean isCommandEnabled = false;
    private Button commandBtn;

    public void build() { // 建议传入 parent Group，通常是 Core.scene.root 或一个 HUD 层
        
        Table table = new Table();
        table.setFillParent(true); // 填满屏幕
        table.bottom().left(); // 将内容定位在右下角 (你可以根据需要改成 left 或其他)

        // 创建按钮逻辑
        commandBtn = new Button("指挥", () -> {
            isCommandEnabled = !isCommandEnabled;
            // 3. 发送事件通知 UnitControl
            Events.fire(new EventType.CommandChange(isCommandEnabled));
        });

        // 添加按钮到布局，设置大小和边距
        table.add(commandBtn).size(120f, 50f).margin(10f);

        // 将布局添加到场景
        Core.scene.root.addChild(table);
    }
}