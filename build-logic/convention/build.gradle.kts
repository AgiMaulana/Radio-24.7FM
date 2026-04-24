plugins {
    `kotlin-dsl`
}

group = "io.github.agimaulana.radio.buildlogic"

java {
    sourceCompatibility = JavaVersion.valueOf(libs.versions.jdk.get())
    targetCompatibility = JavaVersion.valueOf(libs.versions.jdk.get())
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    implementation(libs.sentry.android.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = libs.plugins.boilerplate.android.application.asProvider().get().pluginId
            implementationClass = "ApplicationConventionPlugin"
        }
        register("jetpackCompose") {
            id = libs.plugins.boilerplate.jetpack.compose.get().pluginId
            implementationClass = "JetpackComposeConventionPlugin"
        }
        register("androidDesign") {
            id = libs.plugins.boilerplate.android.design.get().pluginId
            implementationClass = "DesignConventionPlugin"
        }
        register("androidLibrary") {
            id = libs.plugins.boilerplate.android.library.asProvider().get().pluginId
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("javaLibrary") {
            id = libs.plugins.boilerplate.java.library.get().pluginId
            implementationClass = "JavaLibraryConventionPlugin"
        }
        register("androidFeature") {
            id = libs.plugins.boilerplate.android.feature.get().pluginId
            implementationClass = "FeatureConventionPlugin"
        }
        register("androidHilt") {
            id = libs.plugins.boilerplate.android.hilt.get().pluginId
            implementationClass = "HiltConventionPlugin"
        }
        register("androidSentry") {
            id = libs.plugins.boilerplate.android.sentry.get().pluginId
            implementationClass = "SentryConventionPlugin"
        }
        register("kotlinDetekt") {
            id = libs.plugins.boilerplate.kotlin.detekt.get().pluginId
            implementationClass = "DetektConventionPlugin"
        }
        register("androidApplicationJacoco") {
            id = libs.plugins.boilerplate.android.application.jacoco.get().pluginId
            implementationClass = "AndroidApplicationJacocoConventionPlugin"
        }
        register("androidLibraryJacoco") {
            id = libs.plugins.boilerplate.android.library.jacoco.get().pluginId
            implementationClass = "AndroidLibraryJacocoConventionPlugin"
        }
    }
}
