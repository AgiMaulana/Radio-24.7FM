plugins {
    id(libs.plugins.boilerplate.android.feature.get().pluginId)
}

android {
    namespace = "io.github.agimaulana.radio.feature.sample"
}

dependencies {
    implementation(project(":domain:api"))
}
