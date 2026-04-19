plugins {
    id(libs.plugins.boilerplate.android.feature.get().pluginId)
}

android {
    namespace = "io.github.agimaulana.radio.feature.stationlist"
}

dependencies {
    implementation(project(":core:design"))
    implementation(project(":core:radioplayer"))
    implementation(project(":core:shared-test"))
    implementation(project(":domain:api"))
}