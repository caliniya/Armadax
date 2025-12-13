package caliniya.armavoke.system;

import arc.util.Log;
import arc.util.Threads;

// 1. 修改泛型定义：T 必须是 BasicSystem<T> 的子类
public abstract class BasicSystem<T extends BasicSystem<T>> {

  public boolean inited = false;
  public int priority = 0;
  protected boolean isThreaded = false;

  private volatile boolean threadRunning = false;
  private Thread systemThread;
  protected long threadSleepMs = 16;

  /** 初始化：返回 T 类型 (具体的子类类型) */
  //@SuppressWarnings("unchecked")
  public T init() {
    if (isThreaded) startThread();
    inited = true;

    // 2. 强制转换 this 为 T
    // 因为我们在类定义中约束了 T extends BasicSystem<T>，
    // 所以在运行时，子类实例的 this 也就是 T 类型。
    return (T) this;
  }

  public void update() {}

  public void dispose() {
    stopThread();
  }

  private void startThread() {
    if (threadRunning) return;
    threadRunning = true;

    systemThread =
        Threads.daemon(
            () -> {
              Log.info("System thread started: " + this.getClass().getSimpleName());
              while (threadRunning) {
                try {
                  if (inited) {
                    update();
                  }
                  if (threadSleepMs > 0) Thread.sleep(threadSleepMs);
                } catch (InterruptedException e) {
                  threadRunning = false;
                } catch (Exception e) {
                  Log.err("Error in system thread: " + this.getClass().getSimpleName(), e);
                }
              }
            });
  }

  private void stopThread() {
    threadRunning = false;
    if (systemThread != null) {
      systemThread.interrupt();
      systemThread = null;
    }
  }
}
