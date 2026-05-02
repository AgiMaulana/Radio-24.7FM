plugins {
    id("boilerplate.android.library")
}

android {
    namespace = "io.github.agimaulana.radio.core.car.viewmodel.hilt"
}

dependencies {
    api(project(":core:car-app-viewmodel-core"))
    compileOnly(libs.androidx.car.app)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}

kotlin {
    explicitApi()
}
