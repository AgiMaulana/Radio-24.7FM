plugins {
    id(libs.plugins.boilerplate.android.application.asProvider().get().pluginId)
    id(libs.plugins.boilerplate.android.hilt.get().pluginId)
}

android {
    namespace = "io.github.agimaulana.radio"
    defaultConfig {
        minSdk = 29
    }
}

dependencies {
    implementation(project(":feature:auto"))
    implementation(project(":domain:impl"))
    implementation(project(":infrastructure"))
    implementation(libs.androidx.car.app.automotive)
}
