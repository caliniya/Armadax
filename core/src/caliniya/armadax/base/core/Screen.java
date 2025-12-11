package caliniya.armadax.base.core; // 修改包名

/** 一个简单的屏幕接口(好吧，这确实很抽象)*/
public interface Screen {
    /** 当屏幕显示时调用（初始化资源、设置输入处理器） */
    default void show() {}

    /** 每帧调用（逻辑更新 + 绘制） */
    void render();

    /** 窗口大小改变时调用 */
    default void resize(int width, int height) {}

    /** 当屏幕被切换走/隐藏时调用（停止音乐、取消输入） */
    default void hide() {}
    
    /** 销毁资源 */
    default void dispose() {}
}