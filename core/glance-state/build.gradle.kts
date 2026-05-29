plugins {
    id(libs.plugins.boilerplate.android.library.asProvider().get().pluginId)
    id(libs.plugins.boilerplate.android.hilt.get().pluginId)
}

android {
    namespace = "io.github.agimaulana.radio.core.glance.state"
}

dependencies {
    implementation(project(":core:glance-viewmodel-core"))
    implementation(libs.glance.appwidget)
    implementation(libs.hilt.android)
    implementation(libs.kotlinx.coroutines.core)
    ksp(libs.hilt.android.compiler)
}

kotlin {
    explicitApi()
}
