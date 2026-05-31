plugins {
    id(libs.plugins.boilerplate.android.library.asProvider().get().pluginId)
}

android {
    namespace = "io.github.agimaulana.radio.core.glance.viewmodel"
}

dependencies {
    compileOnly(libs.glance.appwidget)
}

kotlin {
    explicitApi()
}
