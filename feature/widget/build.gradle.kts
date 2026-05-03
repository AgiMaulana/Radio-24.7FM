plugins {
    id(libs.plugins.boilerplate.android.feature.get().pluginId)
}

android {
    namespace = "io.github.agimaulana.radio.feature.widget"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:design"))
    implementation(project(":core:design-glance"))
    implementation(project(":core:radioplayer"))
    implementation(project(":domain:api"))
    implementation(libs.glance)
    implementation(libs.glance.appwidget)
    implementation(libs.glance.material)
    implementation(libs.hilt.android)
    implementation(libs.timber)
    debugImplementation(libs.glance.appwidget.preview)
    debugImplementation(libs.glance.preview)
}
