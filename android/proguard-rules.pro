-dontobfuscate

-keep class caliniya.armadax.** { *; }
-keep class arc.** { *; }
-keep class net.jpountz.** { *; }
-keep class com.android.dex.** { *; }
-keepattributes Signature,*Annotation*,InnerClasses,EnclosingMethod

-dontwarn javax.naming.**

# 禁用混淆
-dontobfuscate

# 禁用优化
-dontoptimize

# 保留所有接口
-keep interface ** {
    *;
}