plugins {
    id("boilerplate.android.library")
    id("boilerplate.android.hilt")
}

android {
    namespace = "io.github.agimaulana.radio.core.location"
}

dependencies {
    api(libs.play.services.location)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.timber)
}
