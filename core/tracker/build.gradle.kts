plugins {
    id("boilerplate.android.library")
}

android {
    namespace = "io.github.agimaulana.radio.core.tracker"
}

dependencies {
    implementation(libs.sentry.android)
}
