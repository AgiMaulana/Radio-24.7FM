package io.github.agimaulana.radio

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project

internal fun Project.configureProductFlavors(
    commonExtension: CommonExtension<*, *, *, *, *, *>
) {
    commonExtension.apply {
        flavorDimensions += "env"
        productFlavors {
            register("dev") {
            }
            register("staging") {
            }
            register("prod") {
            }
        }
    }
}
