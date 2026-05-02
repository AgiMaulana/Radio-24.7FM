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
    implementation(project(":domain:api"))
    implementation(project(":core:radioplayer"))
    implementation(project(":core:car-app-state"))
    implementation(project(":core:car-app-viewmodel-core"))
    implementation(project(":core:car-app-viewmodel-hilt"))
    implementation(libs.androidx.car.app)
//    implementation(libs.androidx.media3)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.collections.immutable)
    implementation(libs.coil)
    implementation(libs.coil.network)
    testImplementation(libs.androidx.car.app.testing)
}
