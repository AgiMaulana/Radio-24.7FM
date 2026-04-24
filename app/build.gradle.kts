import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.boilerplate.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.boilerplate.jetpack.compose)
}

val isCI = System.getenv("CI")?.toBoolean() == true

val localProperties = Properties()
if (isCI) {
    // In CI, read from keystore.properties
    val keystoreProps = rootProject.file("keystore.properties")
    if (keystoreProps.exists()) {
        localProperties.load(keystoreProps.inputStream())
    }
} else {
    // Locally, read from local.properties
    val localProps = rootProject.file("local.properties")
    if (localProps.exists()) {
        localProperties.load(localProps.inputStream())
    }
}

android {
    namespace = "io.github.agimaulana.radio"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.github.agimaulana.radio"
        minSdk = 24
        targetSdk = 36
        versionCode = project.findProperty("buildNumber")?.toString()?.toIntOrNull() ?: 1
        versionName = project.findProperty("versionName")?.toString() ?: "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    if (localProperties.isNotEmpty()) {
        signingConfigs {
            create("release") {
                storeFile = file(localProperties["keystoreFile"] as String)
                storePassword = localProperties["storeFilePassword"] as String
                keyAlias = localProperties["keyAlias"] as String
                keyPassword = localProperties["keyPassword"] as String
            }
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
        }
        release {
            isMinifyEnabled = false
            isDebuggable = false
            signingConfig = if (localProperties.isNotEmpty()) signingConfigs.getByName("release") else null
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
            isUniversalApk = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.valueOf(libs.versions.jdk.get())
        targetCompatibility = JavaVersion.valueOf(libs.versions.jdk.get())
    }
    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }
}

dependencies {
    implementation(project(":core:design"))
    implementation(project(":domain:impl"))
    implementation(project(":infrastructure"))
    implementation(project(":feature:stationlist"))
    implementation(libs.android.material)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.startup)

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
