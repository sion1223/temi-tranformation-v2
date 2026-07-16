# ProGuard rules for SwitchGo

# Keep SwitchGo SDK
-keep class com.rhizo.switchgo.** { *; }

# Kotlin Coroutines
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# AndroidX Lifecycle
-keep class androidx.lifecycle.** { *; }
-dontwarn androidx.lifecycle.**

# UtilCodex
-keep class com.blankj.utilcode.** { *; }
-dontwarn com.blankj.utilcode.**

# Material Components
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**
