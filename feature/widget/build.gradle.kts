plugins {
    alias(libs.plugins.boilerplate.android.library)
    alias(libs.plugins.boilerplate.android.glance)
}

android {
    namespace = "io.github.agimaulana.radio.feature.widget"
}

dependencies {
    implementation(project(":core:design"))
    implementation(project(":core:radioplayer"))
    implementation(project(":core:glance-viewmodel-core"))
    implementation(project(":core:glance-viewmodel-hilt"))
    implementation(project(":core:glance-state"))
    implementation(libs.androidx.compose.material3)
    implementation(libs.coil.compose)
}
