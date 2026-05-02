plugins {
    id("boilerplate.android.library")
}

android {
    namespace = "io.github.agimaulana.radio.core.car.viewmodel"
}

dependencies {
    compileOnly(libs.androidx.car.app)
}

kotlin {
    explicitApi()
}