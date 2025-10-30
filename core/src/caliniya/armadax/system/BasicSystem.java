package caliniya.armadax.system;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.TimeUtils;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 系统基类
 * 默认在主线程运行，子类可以选择启用多线程
 */
public abstract class BasicSystem {
    private long lastUpdateTime;
    private final AtomicBoolean running = new AtomicBoolean(false);
    
    // 更新间隔（毫秒）
    private final long updateInterval;
    
    // 多线程相关配置
    private boolean multiThreaded = false;
    private Thread updateThread;
    
    protected BasicSystem(long updateIntervalMs) {
        this(updateIntervalMs, false); // 默认单线程
    }
    
    protected BasicSystem(long updateIntervalMs, boolean multiThreaded) {
        this.updateInterval = updateIntervalMs;
        this.multiThreaded = multiThreaded;
    }
    
    /**
     * 获取单例实例
     */
    
    /**
    所有子系统应该实现此方法
    @SuppressWarnings("unchecked")
    public static <T extends BasicSystem> T getInstance();
    */
    
    /**
     * 启动系统
     */
    public void start() {
        if (running.get()) return;
        
        running.set(true);
        lastUpdateTime = TimeUtils.millis();
        
        if (multiThreaded) {
            startMultiThreaded();
        }
        // 单线程模式不需要额外启动线程，由主循环调用updateManual()
    }
    
    /**
     * 启动多线程模式
     */
    private void startMultiThreaded() {
        updateThread = new Thread(() -> {
            while (running.get()) {
                long currentTime = TimeUtils.millis();
                if (currentTime - lastUpdateTime >= updateInterval) {
                    lastUpdateTime = currentTime;
                    
                    if (requiresMainThread()) {
                        // 需要在主线程执行的更新
                        Gdx.app.postRunnable(this::update);
                    } else {
                        // 可以在后台线程执行的更新
                        update();
                    }
                }
                
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Gdx.app.error("BasicSystem", "Update thread interrupted", e);
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "System-Update-Thread");
        
        updateThread.setDaemon(true);
        updateThread.start();
    }
    
    /**
     * 手动更新（用于单线程模式）
     * 在主游戏循环中调用
     */
    public void updateManual() {
        if (!running.get() || multiThreaded) return;
        
        long currentTime = TimeUtils.millis();
        if (currentTime - lastUpdateTime >= updateInterval) {
            lastUpdateTime = currentTime;
            update();
        }
    }
    
    /**
     * 停止系统
     */
    public void stop() {
        running.set(false);
        if (updateThread != null && updateThread.isAlive()) {
            updateThread.interrupt();
        }
    }
    
    /**
     * 抽象更新方法，子类需要实现具体的逻辑
     */
    protected abstract void update();
    
    /**
     * 子类重写此方法来确定是否需要主线程
     * 默认返回true（安全起见）
     */
    protected boolean requiresMainThread() {
        return true;
    }
    
    /**
     * 动态切换线程模式（运行时）
     */
    public void setMultiThreaded(boolean multiThreaded) {
        if (this.multiThreaded == multiThreaded) return;
        
        boolean wasRunning = running.get();
        if (wasRunning) {
            stop();
        }
        
        this.multiThreaded = multiThreaded;
        
        if (wasRunning) {
            start();
        }
    }
    
    /**
     * 检查系统是否正在运行
     */
    public boolean isRunning() {
        return running.get();
    }
    
    /**
     * 获取更新间隔
     */
    public long getUpdateInterval() {
        return updateInterval;
    }
    
    /**
     * 检查是否为多线程模式
     */
    public boolean isMultiThreaded() {
        return multiThreaded;
    }
    
    /**
     * 获取上次更新时间
     */
    public long getLastUpdateTime() {
        return lastUpdateTime;
    }
    
    /**
     * 重置计时器
     */
    public void resetTimer() {
        lastUpdateTime = TimeUtils.millis();
    }
}