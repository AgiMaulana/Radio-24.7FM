import com.android.build.api.dsl.LibraryExtension
import io.github.agimaulana.radio.configureView
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class FeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

            with(pluginManager) {
                apply("boilerplate.android.library")
                apply("boilerplate.android.hilt")
                apply("org.jetbrains.kotlin.android")
                apply("org.jetbrains.kotlin.plugin.compose")
                apply("kotlin-parcelize")
            }

            dependencies {
                add("implementation", libs.findLibrary("android.material").get())
                add("implementation", libs.findLibrary("compose.lifecycle.runtime").get())
                add("implementation", libs.findLibrary("retrofit").get())
                add("implementation", libs.findLibrary("moshi").get())
                add("ksp", libs.findLibrary("moshi.codegen").get())
                add("implementation", libs.findLibrary("kotlinx.coroutines.core").get())
                add("implementation", libs.findLibrary("compose.lifecycle.runtime").get())

                add("testImplementation", libs.findLibrary("turbine").get())
                add("testImplementation", libs.findLibrary("junit").get())
                add("testImplementation", libs.findLibrary("androidx.arch.core.testing").get())
                add("testImplementation", libs.findLibrary("androidx.lifecycle.runtime.test").get())
                add("testImplementation", libs.findLibrary("kotlinx.coroutines.test").get())
                add("testImplementation", libs.findLibrary("mockk").get())
                add("testImplementation", libs.findLibrary("mockk.android").get())
                add("testImplementation", libs.findLibrary("robolectric").get())

                add("androidTestImplementation", libs.findLibrary("androidx.test.core").get())
                add("androidTestImplementation", libs.findLibrary("compose.navigation.testing").get())
                add("androidTestImplementation", libs.findLibrary("mockk").get())
                add("androidTestImplementation", libs.findLibrary("mockk.agent").get())
                add("androidTestImplementation", libs.findLibrary("mockk.android").get())

                add("debugImplementation", libs.findLibrary("compose.ui.testing").get())
                add("debugImplementation", libs.findLibrary("compose.manifest.testing").get())
            }

            extensions.configure<LibraryExtension> {
                configureView(this, isEnableCompose = true)
                defaultConfig {
                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                }
            }
        }
    }
}
