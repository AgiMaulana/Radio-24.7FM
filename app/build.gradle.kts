import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.boilerplate.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.boilerplate.jetpack.compose)
}

android {
    namespace = "io.github.agimaulana.radio"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.github.agimaulana.radio"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
    implementation(project(":feature:sample"))
    implementation(project(":feature:stationlist"))
    implementation(libs.android.material)
    implementation(libs.androidx.core.ktx)

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}