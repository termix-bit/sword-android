# Sword Android ProGuard / R8 Rules

# Keep Kotlin Serialization generated serializers
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.sword.security.android.**$$serializer { *; }
-keepclassmembers class com.sword.security.android.** {
    *** Companion;
}
-keepclasseswithmembers class com.sword.security.android.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Chaquopy
-keep class com.chaquo.python.** { *; }
-dontwarn com.chaquo.python.**
