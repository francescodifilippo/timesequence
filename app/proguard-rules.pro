# Keep kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.francescodf.sequenzatimer.**$$serializer { *; }
-keepclassmembers class com.francescodf.sequenzatimer.** {
    *** Companion;
}
-keepclasseswithmembers class com.francescodf.sequenzatimer.** {
    kotlinx.serialization.KSerializer serializer(...);
}
