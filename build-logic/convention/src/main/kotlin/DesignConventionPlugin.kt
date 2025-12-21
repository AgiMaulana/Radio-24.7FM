import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class DesignConventionPlugin : Plugin<Project>  {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("boilerplate.android.library")
                apply("boilerplate.android.hilt")
                apply("boilerplate.jetpack.compose")
                apply("org.jetbrains.kotlin.android")
            }

            extensions.configure<LibraryExtension> {
//                configureProductFlavors(this)
            }

            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
            dependencies {
                add("implementation", libs.findLibrary("android.material").get())
                add("implementation", libs.findLibrary("compose.lifecycle.runtime").get())
            }
        }
    }

}