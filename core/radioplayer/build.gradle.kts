plugins {
    id(libs.plugins.boilerplate.android.library.asProvider().get().pluginId)
    id(libs.plugins.boilerplate.android.hilt.get().pluginId)
    id(libs.plugins.boilerplate.android.media3.get().pluginId)
}

android {
    namespace = "io.github.agimaulana.radio.core.radioplayer"
}

dependencies {
    implementation(libs.kotlinx.coroutines.guava)
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(project(":domain:api"))
    implementation(project(":domain:impl"))

    testImplementation(libs.robolectric)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")

    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
}
