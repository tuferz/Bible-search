# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to the flags specified
# in C:\Users\tufer\AppData\Local\Android\Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.

# Retrofit & Gson rules
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.example.blesearchum.model.** { *; }
-dontwarn okhttp3.**
-dontwarn retrofit2.**
