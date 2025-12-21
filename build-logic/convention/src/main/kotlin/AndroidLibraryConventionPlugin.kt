import com.android.build.api.dsl.LibraryExtension
import io.github.agimaulana.radio.configureAndroid
import io.github.agimaulana.radio.configureJacoco
import io.github.agimaulana.radio.configureProductFlavors
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply("org.jetbrains.kotlin.android")
                apply("boilerplate.android.hilt")
            }

            extensions.configure<LibraryExtension> {
                configureAndroid(this)
                configureProductFlavors(this)
            }
            configureJacoco()

            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

            dependencies {
                add("implementation", libs.findLibrary("kotlinx.coroutines.core").get())
                add("implementation", libs.findLibrary("retrofit.converter.moshi").get())
                add("ksp", libs.findLibrary("moshi.codegen").get())
                add("implementation", libs.findLibrary("joda.time").get())

                add("testImplementation", libs.findLibrary("kotlinx.coroutines.test").get())
                add("testImplementation", libs.findLibrary("junit").get())
            }
        }
    }
}
