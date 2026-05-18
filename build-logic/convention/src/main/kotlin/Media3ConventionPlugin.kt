import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class Media3ConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
            dependencies {
                add("implementation", libs.findLibrary("androidx.media3.exoplayer").get())
                add("implementation", libs.findLibrary("androidx.media3.exoplayer.hls").get())
                add("implementation", libs.findLibrary("androidx.media3.ui").get())
                add("implementation", libs.findLibrary("androidx.media3.session").get())
                add("implementation", libs.findLibrary("androidx.media3.cast").get())
                add("implementation", libs.findLibrary("androidx.media3.ui.compose").get())
                add("implementation", libs.findLibrary("androidx.media3.ui.compose.material3").get())
                add("implementation", libs.findLibrary("androidx.mediarouter").get())

                add("androidTestImplementation", libs.findLibrary("androidx.media3.session").get())
                add("androidTestImplementation", libs.findLibrary("androidx.media3.exoplayer").get())
            }
        }
    }
}
