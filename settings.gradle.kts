pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven (url = "https://jitpack.io")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS) // ito ang cause ng error
    repositories {
        google()
        mavenCentral()
        maven (url = "https://jitpack.io")
    }
}

rootProject.name = "ExpenseTracker3"
include(":app")