// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.navigation.safeargs) apply false
    alias(libs.plugins.sentry) apply false
}

tasks.register<Exec>("aconnectMuMUu") {
    if (org.gradle.internal.os.OperatingSystem.current().isWindows) {
        commandLine("adb", "kill-server")
        doLast {
            exec { commandLine("adb", "start-server") }
            exec { commandLine("adb", "connect", "127.0.0.1:7555") }
        }
    } else {
        commandLine("sh", "-c", "adb kill-server && adb start-server && adb connect 127.0.0.1:7555")
    }
}
