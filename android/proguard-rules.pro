# 保持所有类名、方法名、字段名不混淆
-keepnames class ** { *; }
-keepnames interface ** { *; }
-keepnames enum ** { *; }

# 保持所有成员不混淆
-keepclassmembernames class ** {
    *;
}

# 保持所有注解不混淆
-keepattributes *Annotation*

# 特别保护 LibGDX 相关类
-keep class com.badlogic.gdx.** { *; }
-keepclassmembers class com.badlogic.gdx.** { *; }

# 保护应用入口点
-keep class * implements com.badlogic.gdx.ApplicationListener {
    public void create();
    public void render();
    public void resize(int, int);
    public void pause();
    public void resume();
    public void dispose();
}

# 允许代码优化（内联、常量传播等）
-optimizations !code/simplification/cast,!field/*,!class/merging/*

# 忽略 3D 相关类的警告
-dontwarn com.badlogic.gdx.graphics.g3d.**
-dontwarn com.badlogic.gdx.graphics.g3d.loader.**
-dontwarn com.badlogic.gdx.graphics.g3d.model.**
-dontwarn com.badlogic.gdx.graphics.g3d.utils.**

# 允许移除未使用的类
-allowaccessmodification
-overloadaggressively
-repackageclasses ''