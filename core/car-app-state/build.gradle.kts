plugins {
    id("boilerplate.android.library")
}

android {
    namespace = "io.github.agimaulana.radio.core.car.state"
}

dependencies {
    compileOnly(libs.androidx.car.app)
    api(libs.androidx.lifecycle.runtime.ktx)
    api(libs.kotlinx.coroutines.core)
}

kotlin {
    explicitApi()
}