plugins {
    id(libs.plugins.boilerplate.android.library.asProvider().get().pluginId)
}

android {
    namespace = "io.github.agimaulana.radio.core.network.test"
}

dependencies {
    implementation(project(":core:network"))
    implementation(libs.junit)
    implementation(libs.retrofit)
    implementation(libs.okhttp.mockwebserver)
}
