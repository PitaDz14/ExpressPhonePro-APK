-keep class com.expressphonepro.** { *; }
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
