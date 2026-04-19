@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id(libs.plugins.boilerplate.android.library.asProvider().get().pluginId)
}

android {
    namespace = "io.github.agimaulana.radio.infrastructure"
}

dependencies {
    implementation(libs.moshi)
    ksp(libs.moshi.codegen)
    implementation(project(":core:common"))
    implementation(project(":core:network"))
    implementation(project(":domain:api"))

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(project(":core:shared-test"))
    testImplementation(project(":core:network:test"))
}
