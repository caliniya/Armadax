package caliniya.armadax.system.input;
import arc.input.InputProcessor;

public class UiInput implements InputProcessor {
   
    /**
     * 当字符被输入时调用（通常用于文本输入）
     * 
     * @param character 输入的字符
     * @return 如果返回 true，表示输入事件已被处理，不再传递给其他输入处理器；
     *         如果返回 false，表示事件可以继续传递给其他处理器
     */
    @Override
    public boolean keyTyped(char character) {
        // TODO: 实现字符输入逻辑
        return false;
    }

    /**
     * 当触摸拖动或鼠标拖动时调用
     * 
     * @param screenX 当前拖动点的X坐标（屏幕坐标系）
     * @param screenY 当前拖动点的Y坐标（屏幕坐标系）
     * @param pointer 拖动的手指/指针索引（0为第一个手指/主鼠标按钮）
     * @return 如果返回 true，表示输入事件已被处理，不再传递给其他输入处理器；
     *         如果返回 false，表示事件可以继续传递给其他处理器
     */
    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        // TODO: 实现触摸/鼠标拖动逻辑
        return false;
    }

    /**
     * 当鼠标移动时调用（仅适用于桌面应用）
     * 
     * @param screenX 鼠标当前位置的X坐标（屏幕坐标系）
     * @param screenY 鼠标当前位置的Y坐标（屏幕坐标系）
     * @return 如果返回 true，表示输入事件已被处理，不再传递给其他输入处理器；
     *         如果返回 false，表示事件可以继续传递给其他处理器
     */
    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        // TODO: 实现鼠标移动逻辑
        return false;
    }

    /**
     * 当鼠标滚轮滚动时调用（仅适用于桌面应用）
     * 
     * @param amountX 水平滚动量（正值向右，负值向左）
     * @param amountY 垂直滚动量（正值向上，负值向下）
     * @return 如果返回 true，表示输入事件已被处理，不再传递给其他输入处理器；
     *         如果返回 false，表示事件可以继续传递给其他处理器
     */
    @Override
    public boolean scrolled(float amountX, float amountY) {
        // TODO: 实现鼠标滚轮滚动逻辑
        return false;
    }
    
    
    
    
}