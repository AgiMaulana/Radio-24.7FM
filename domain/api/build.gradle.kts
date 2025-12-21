@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id(libs.plugins.boilerplate.android.library.asProvider().get().pluginId)
}

android {
    namespace = "io.github.agimaulana.radio.domain.api"
}

dependencies {
    implementation(libs.moshi)
    implementation(project(":core:common"))

    ksp(libs.moshi.codegen)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
}
