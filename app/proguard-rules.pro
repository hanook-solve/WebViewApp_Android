# ─────────────────────────────────────────────────────────────────
#  WebViewApp ProGuard Rules
# ─────────────────────────────────────────────────────────────────

# Keep JavaScript interface methods (called via reflection from JS)
-keepclassmembers class com.webviewapp.WebAppInterface {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep all Firebase classes
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Keep AdMob
-keep class com.google.android.gms.ads.** { *; }

# Keep our app classes
-keep class com.webviewapp.** { *; }

# Standard Android rules
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-dontwarn com.google.**
