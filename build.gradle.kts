buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        // ✅ Added JitPack repository for MPAndroidChart
        maven (url = "https://jitpack.io")
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.7.2")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        // ✅ Also add JitPack here to ensure all modules can access it
        maven ( url = "https://jitpack.io" )
    }
}

