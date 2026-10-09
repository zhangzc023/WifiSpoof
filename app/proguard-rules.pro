# Xposed 模块入口与 Hook 类禁止混淆/裁剪
-keep class com.zhangzc.wifispoof.XposedInit { *; }
-keep class com.zhangzc.wifispoof.hooks.** { *; }
