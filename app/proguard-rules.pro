# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# ── Retrofit ──────────────────────────────────────────────────────────────
-keepattributes Signature
-keepattributes *Annotation*

-keep class com.iptvapp.data.remote.** { *; }

-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# ── Gson ──────────────────────────────────────────────────────────────────
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# ── Room ──────────────────────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ── OkHttp ────────────────────────────────────────────────────────────────
-dontwarn okhttp3.**
-dontwarn okio.**

# ── Media3 / ExoPlayer ───────────────────────────────────────────────────
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**
