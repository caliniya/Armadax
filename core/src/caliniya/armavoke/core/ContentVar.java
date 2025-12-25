package caliniya.armavoke.core;

import arc.struct.ObjectMap;
import caliniya.armavoke.base.game.ContentType;
import caliniya.armavoke.base.type.CType;

public class ContentVar {
  
  // 映射表"CType.name" -> ContentType 对象
  private static ObjectMap<String, ContentType> contentMap = new ObjectMap<>();
  
  /** 注册内容，在 XXX.load()调用 */
  public static void add(ContentType content) {
    if (content == null) return;
    contentMap.put(content.getLName(), content);
  }

  /** 通过名字查找内容 */
  public static ContentType get(String name) {
    return contentMap.get(name);
  }

  /** 带类型的查找 (强制转换) */
  @SuppressWarnings("unchecked")
  public static <T extends ContentType> T get(String name, Class<T> type) {
    ContentType c = contentMap.get(name);
    if (c != null && type.isInstance(c)) {
      return (T) c;
    }
    return null;
  }
}
