# =====================================================================
# HandyLog R8/ProGuard 규칙 (release minify 시 적용)
# =====================================================================

# --- Room + androidx.sqlite 번들 네이티브 드라이버 ---
# libsqliteJni.so 의 JNI_OnLoad 가 아래 클래스/네이티브 메서드에 바인딩한다.
# R8 이 제거·리네임하면 JNI_ERR (UnsatisfiedLinkError) 로 첫 DB 접근 시 크래시.
-keep class androidx.sqlite.** { *; }
-keep class androidx.room.** { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase { <init>(); }
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
# JNI 로 접근되는 네이티브 메서드 이름 보존
-keepclasseswithmembernames class * {
    native <methods>;
}

# --- kotlinx.serialization (@Serializable NavKey Route·도메인 모델·차트 DTO) ---
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
# 앱 코드에서 생성된 직렬화기·Companion 보존
-keep,includedescriptorclasses class com.hand.log.**$$serializer { *; }
-keepclassmembers class com.hand.log.** {
    *** Companion;
}
-keepclasseswithmembers class com.hand.log.** {
    kotlinx.serialization.KSerializer serializer(...);
}
# @Serializable enum 의 값 보존(직렬화 이름 매칭)
-keepclassmembers class com.hand.log.** {
    static **[] values();
    static ** valueOf(java.lang.String);
}
