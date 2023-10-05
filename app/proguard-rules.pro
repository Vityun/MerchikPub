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

# Keep all classes in Bouncy Castle
-keep class org.bouncycastle.** { *; }

# Keep all classes in Conscrypt
-keep class org.conscrypt.** { *; }
-keep class org.openjsse.** { *; }


-keep class org.bouncycastle.jsse.**{ *; }
-keep class org.bouncycastle.jsse.**{ *; }
-keep class org.bouncycastle.jsse.provider.**{ *; }
-keep class org.conscrypt.**{ *; }
-keep class org.conscrypt.**{ *; }
-keep class org.conscrypt.**{ *; }
-keep class org.openjsse.javax.net.ssl.**{ *; }
-keep class org.openjsse.javax.net.ssl.**{ *; }
-keep class org.openjsse.net.ssl.**{ *; }

-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.conscrypt.Conscrypt$Version
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.ConscryptHostnameVerifier
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE


-keep class com.google.gson.** { *; }
-keep class com.google.inject.** { *; }
-keep class org.apache.http.** { *; }
-keep class org.apache.james.mime4j.** { *; }
-keep class javax.inject.** { *; }
-keep class retrofit.** { *; }
-dontwarn rx.**
-keep class sun.misc.Unsafe { *; }

-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-keep class com.chuckerteam.chucker.** { *; }

-keep class com.google.gson.** { *; }
-keep class org.apache.** { *; }
-keep class javax.inject.** { *; }
-keep class retrofit.** { *; }

-keep class ua.com.merchik.merchik.data.** { *; }
-keep class ua.com.merchik.merchik.dialogs.** { *; }
#-keep class ua.com.merchik.merchik.menu_login { *; }

# Keep GSON classes (Bluesnap module uses it too)

# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-dontwarn sun.misc.**
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { *; }

# Prevent proguard from stripping interface information from TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

-keep class ua.com.merchik.merchik.data.RetrofitResponse.** { *; }

# Для Доп. требований.
-keep class ua.com.merchik.merchik.Options.Controls.** { *; }

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
