plugins {
    id(libs.plugins.boilerplate.android.library.asProvider().get().pluginId)
    id(libs.plugins.boilerplate.android.hilt.get().pluginId)
}

android {
    namespace = "io.github.agimaulana.radio.feature.auto"
    defaultConfig {
        minSdk = 23
    }
}

dependencies {
    implementation(libs.androidx.car.app)
    implementation(project(":domain:api"))
    implementation(project(":core:radioplayer"))
}
