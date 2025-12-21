import com.android.build.api.dsl.ApplicationExtension
import io.github.agimaulana.radio.configureAndroid
import io.github.agimaulana.radio.configureProductFlavors
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class ApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

            with(pluginManager) {
                apply("com.android.application")
                apply("org.jetbrains.kotlin.android")
                apply("boilerplate.android.hilt")
                apply("boilerplate.kotlin.detekt")
                apply("boilerplate.android.application.jacoco")
            }

            extensions.configure<ApplicationExtension> {
                configureAndroid(this)
                configureProductFlavors(this)
                defaultConfig.targetSdk = libs.findVersion("targetSdk").get().toString().toInt()
            }

            dependencies {
                add("implementation", libs.findLibrary("androidx.core.ktx").get())
            }
        }
    }
}
