plugins {
    id(libs.plugins.boilerplate.android.design.get().pluginId)
}

android {
    namespace = "io.github.agimaulana.radio.core.design.glance"
}

dependencies {
    implementation(project(":core:design"))
    implementation(libs.glance)
    implementation(libs.glance.appwidget)
    implementation(libs.glance.material)
}
