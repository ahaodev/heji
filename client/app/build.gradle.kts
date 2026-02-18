import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.navigation.safeargs)
    alias(libs.plugins.ksp)
    alias(libs.plugins.sentry)
}

android {
    namespace = "com.hao.heji"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.hao.heji"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf(
                    "room.schemaLocation" to "$projectDir/schemas",
                    "room.incremental" to "true",
                    "room.expandProjection" to "true"
                )
            }
        }
    }

    signingConfigs {
        create("single") {
            storeFile = file("../heji.keystore")
            storePassword = "password"
            keyPassword = "password"
            keyAlias = "heji"
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("single")
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        release {
            signingConfig = signingConfigs.getByName("single")
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    flavorDimensions += "network"

    productFlavors {
        create("local") {
            dimension = "network"
            applicationId = "com.hao.heji_test"
            buildConfigField("String", "HTTP_URL", properties["LOCALHOST"] as String)
            resValue("string", "app_name", "合記开发版")
        }
        create("cloud") {
            dimension = "network"
            applicationId = "com.hao.heji"
            buildConfigField("String", "HTTP_URL", properties["HJSERVER"] as String)
            resValue("string", "app_name", "合記")
        }
    }

    applicationVariants.all {
        val variant = this
        outputs.all {
            val output = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
            val dateFormat = SimpleDateFormat("yyyyMMddHHmm")
            dateFormat.timeZone = TimeZone.getTimeZone("GMT+08:00")
            val time = dateFormat.format(Date())
            val productFlavors = variant.productFlavors[0].name
            val buildTypes = variant.buildType.name
            val versionName = variant.versionName
            val gitVersion = "git rev-parse --short HEAD".runCommand() ?: "unknown"
            output.outputFileName = "$productFlavors-$buildTypes-$versionName-$gitVersion-$time.apk"
            println(output.outputFileName)
        }
    }
}

configurations.all {
    exclude(group = "org.jetbrains.kotlin", module = "kotlin-android-extensions-runtime")
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    // AndroidX
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.legacy.support)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.core.ktx)

    // Lifecycle
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // Navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // WorkManager
    implementation(libs.androidx.work.runtime)

    // MMKV
    implementation(libs.mmkv)

    // Koin
    implementation(libs.koin.android)

    // Network
    implementation(libs.retrofit.converter.kotlinx)
    implementation(libs.okhttp.logging.interceptor)

    // Image Loading
    implementation(libs.glide)
    implementation(libs.glide.okhttp3.integration)
    ksp(libs.glide.ksp)

    // Kotlin
    implementation(libs.kotlin.stdlib.jdk8)

    // serialization
    implementation(libs.kotlinx.serialization.json)

    // Xid
    implementation(libs.xid)

    // MQTT
    implementation(libs.paho.mqtt.android)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Third-party
    implementation(libs.brvah)
    implementation(libs.permissionx)
    implementation(libs.matisse)
    implementation(libs.utilcodex)
    implementation(libs.xpopup)
    implementation(libs.calendarview)
    implementation(libs.mpandroidchart)
    implementation(libs.immersionbar)
    implementation(libs.immersionbar.ktx)
    implementation(libs.jexcelapi)
    implementation(libs.opencsv)
    implementation(libs.subsampling.scale.image.view)

    // Debug
    debugImplementation(libs.glance)
}

// Helper function to run shell commands
fun String.runCommand(): String? {
    return try {
        val process = ProcessBuilder(*split(" ").toTypedArray())
            .directory(projectDir)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()
        process.waitFor()
        process.inputStream.bufferedReader().readText().trim()
    } catch (e: Exception) {
        null
    }
}
