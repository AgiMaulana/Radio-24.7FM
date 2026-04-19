plugins {
    id(libs.plugins.boilerplate.android.library.asProvider().get().pluginId)
}

android {
    namespace = "io.github.agimaulana.radio.core.sharedtest"
}

dependencies {
    implementation(libs.junit)
    implementation(libs.kotlinx.coroutines.test)
    implementation(project(":core:common"))
}
