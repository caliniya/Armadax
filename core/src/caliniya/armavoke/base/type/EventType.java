package caliniya.armavoke.base.type;

public class EventType {
  
  public enum events{
    Mapinit,
    ThreadedStop;
  }
  
  public static class GameInit {}

  public static class CommandChange {
    public final boolean enabled;

    public CommandChange(boolean enabled) {
      this.enabled = enabled;
    }
  }
}
