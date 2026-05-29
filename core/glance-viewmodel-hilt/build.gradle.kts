plugins {
    id(libs.plugins.boilerplate.android.library.asProvider().get().pluginId)
    id(libs.plugins.boilerplate.android.hilt.get().pluginId)
}

android {
    namespace = "io.github.agimaulana.radio.core.glance.viewmodel.hilt"
}

dependencies {
    implementation(project(":core:glance-viewmodel-core"))
    implementation(libs.glance.appwidget)
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
}

kotlin {
    explicitApi()
}
