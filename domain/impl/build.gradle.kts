@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id(libs.plugins.boilerplate.android.library.asProvider().get().pluginId)
    id(libs.plugins.boilerplate.android.hilt.get().pluginId)
}

android {
    namespace = "io.github.agimaulana.radio.domain.impl"
}

dependencies {
    implementation(libs.moshi)
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(project(":core:common"))
    implementation(project(":domain:api"))

    ksp(libs.moshi.codegen)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
}
