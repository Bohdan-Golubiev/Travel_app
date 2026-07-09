# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Gson — зберігаємо generic-сигнатури, потрібні для List<T>, Response<T> тощо
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes RuntimeVisibleAnnotations

# Не чіпати ваші дата-класи, які мапляться з JSON
-keep class com.example.travelapp.model.dataclasses.** { *; }

# Retrofit
-keepattributes Exceptions
-keep interface com.example.travelapp.AviationstackService { *; }

# Gson TypeAdapter / TypeToken (стандартні правила з офіційної доки Gson)
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response