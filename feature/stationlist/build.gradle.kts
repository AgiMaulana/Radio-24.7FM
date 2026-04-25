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
    implementation(project(":core:tracker"))
    implementation(project(":domain:api"))

    implementation(libs.androidx.palette)
    implementation(libs.androidx.compose.animation.graphics)
    implementation(libs.accompanist.permissions)
    implementation(libs.play.services.location)
    implementation(libs.kotlinx.coroutines.play.services)
}
