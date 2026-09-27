# Room Persistence Library ProGuard Rules
-keep class androidx.room.** { *; }
-keep class com.rupeemath.app.data.** { *; }
-keepclassmembers class * {
    @androidx.room.TypeConverter *;
}

# Kotlin Coroutines and Serialization
-keep class kotlinx.coroutines.** { *; }
-keep class com.rupeemath.app.data.model.** { *; }

# Coil Image Loading
-keep class coil.** { *; }
