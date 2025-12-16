package caliniya.armavoke.system;

import arc.util.Log;
import arc.util.Threads;

// T 必须是 BasicSystem<T> 的子类
//@SuppressWarnings("unchecked")
// 压制整个类的unchecked警告，因为这种单例/系统模式很难避免
public abstract class BasicSystem<T extends BasicSystem<T>> {

  public boolean inited = false;
  public int priority = 0;
  
  // 建议默认不开线程，或者通过构造/init参数指定
  protected boolean isThreaded = false;

  private volatile boolean threadRunning = false;
  private Thread systemThread;
  protected long threadSleepMs = 16; // 约60FPS

  /** 
   * 普通初始化
   */
  public T init() {
    // 如果子类在构造时就设置了 isThreaded = true，这里也会启动
    // 但更推荐使用带参数的 init(boolean threaded)
    return init(this.isThreaded); 
  }

  /** 
   * 带参数初始化，显式决定是否开启线程
   */
  public T init(boolean runInThread) {
    if (inited) return (T) this; // 防止重复初始化

    this.isThreaded = runInThread;
    this.inited = true;
    
    // 初始化逻辑...
    
    if (isThreaded) {
        startThread();
    }
    
    return (T) this;
  }

  // 注意：如果在后台线程运行 update，任何对 Arc 核心图形(绘制、纹理等)的操作都会导致崩溃。
  // 数学计算系统通常只处理逻辑（位置、寻路、伤害），不要在 update() 里调用 draw()。
  public void update() {}

  public void dispose() {
    stopThread();
  }

  private void startThread() {
    if (threadRunning) return; // 防止重复启动

    threadRunning = true;
    
    // 使用 Threads.daemon 确保主程序退出时线程自动结束
    systemThread = Threads.daemon("System-" + this.getClass().getSimpleName(), () -> {
          Log.info("System thread started: @", this.getClass().getSimpleName());
          while (threadRunning) {
            try {
              long start = System.currentTimeMillis();
              
              update();
              
              // 简单的帧率控制：减去 update 耗时
              long elapsed = System.currentTimeMillis() - start;
              long sleep = threadSleepMs - elapsed;
              
              if (sleep > 0) {
                  Thread.sleep(sleep);
              } else {
                  // 如果 update 太慢，可能会让出一下 CPU
                  Thread.yield();
              }

            } catch (InterruptedException e) {
              threadRunning = false;
              // 恢复中断状态是好习惯，虽然这里已经是要退出了
              Thread.currentThread().interrupt(); 
            } catch (Exception e) {
              Log.err("Error in system thread: @", this.getClass().getSimpleName(), e);
            }
          }
          Log.info("System thread stopped: @", this.getClass().getSimpleName());
        });
  }

  private void stopThread() {
    threadRunning = false;
    if (systemThread != null) {
      systemThread.interrupt();
      try {
          // 等待线程真正结束(够用了)
          systemThread.join(100); 
      } catch (Exception ignored) {}
      systemThread = null;
    }
  }
}