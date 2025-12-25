plugins {
    id(libs.plugins.boilerplate.android.library.asProvider().get().pluginId)
}

android {
    namespace = "io.github.agimaulana.radio.core.radioplayer"
}

dependencies {
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.exoplayer)
}
