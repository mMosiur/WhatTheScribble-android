# Add project specific ProGuard rules here.
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep line numbers and source file names in stack traces for readable crash logs
-keepattributes SourceFile,LineNumberTable

# Keep Kotlinx Serialization attributes and classes used for Navigation 3 routes
-keepattributes *Annotation*,Signature,InnerClasses
-keepclassmembers class * {
    @org.jetbrains.kotlinx.serialization.Serializable *;
}
-keep class * implements org.jetbrains.kotlinx.serialization.KSerializer { *; }
