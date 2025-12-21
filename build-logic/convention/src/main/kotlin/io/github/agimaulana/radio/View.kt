package io.github.agimaulana.radio

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

internal fun Project.configureView(
    commonExtension: CommonExtension<*, *, *, *, *, *>,
    isEnableCompose: Boolean = false
) {
    val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
    commonExtension.apply {
        buildFeatures {
            compose = isEnableCompose
        }
    }

    dependencies {
        add("implementation", libs.findLibrary("compose.runtime").get())
        add("implementation", libs.findLibrary("compose.ui").get())
        add("implementation", libs.findLibrary("compose.foundation").get())
        add("implementation", libs.findLibrary("compose.foundation.layout").get())
        add("implementation", libs.findLibrary("compose.material").get())
        add("implementation", libs.findLibrary("compose.runtime.livedata").get())
        add("implementation", libs.findLibrary("compose.viewmodel").get())
        add("implementation", libs.findLibrary("compose.ui.tooling").get())
        add("implementation", libs.findLibrary("compose.navigation").get())
        add("implementation", libs.findLibrary("compose.navigation.hilt").get())
        add("implementation", libs.findLibrary("androidx.compose.material3").get())
        add("implementation", libs.findLibrary("androidx.compose.materialWindow").get())
        add("implementation", libs.findLibrary("compose.constraint").get())
        add("implementation", libs.findLibrary("coil.compose").get())
        add("implementation", libs.findLibrary("androidx.window").get())
        add("implementation", libs.findLibrary("accompanist.permissions").get())
        add("implementation", libs.findLibrary("activity.compose").get())
        add("implementation", libs.findLibrary("collections.immutable").get())
        add("implementation", libs.findLibrary("lottie.compose").get())
        add("implementation", libs.findLibrary("androidx.swiperefreshlayout").get())
    }
}
