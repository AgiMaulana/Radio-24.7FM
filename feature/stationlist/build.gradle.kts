plugins {
    id(libs.plugins.boilerplate.android.feature.get().pluginId)
}

android {
    namespace = "io.github.agimaulana.radio.feature.stationlist"
}

dependencies {
    implementation(project(":core:design"))
    implementation(project(":core:shared-test"))
    implementation(project(":domain:api"))
//    implementation("dev.chrisbanes.haze:haze-materials:1.7.1")
}