# Keep Compose runtime internals used via reflection
-keep class androidx.compose.runtime.** { *; }
-dontwarn kotlinx.coroutines.**
-keepclassmembers class com.ludolegends.game.engine.** { *; }
