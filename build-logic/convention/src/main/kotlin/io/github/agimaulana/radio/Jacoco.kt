package io.github.agimaulana.radio

import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.android.build.api.variant.SourceDirectories
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport
import java.util.Locale

private fun String.capitalize() = replaceFirstChar {
    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
}

internal fun Project.configureJacoco(
    androidComponentsExtension: AndroidComponentsExtension<*, *, *>,
) {
    configure<JacocoPluginExtension> {
        toolVersion = libs.findVersion("jacoco").get().toString()
    }

    androidComponentsExtension.onVariants { variant ->
        tasks.register<JacocoReport>("${variant.name.capitalize()}UnitTestCoverageReport") {
            group = "Unit Test Coverage Report"
            description = "Generates unit test JaCoCo coverage report for ${variant.name}."

            dependsOn(
                "test${variant.name.capitalize()}UnitTest",
            )

            reports {
                html.required.set(true)
                xml.required.set(false)
                csv.required.set(false)
            }

            classDirectories.setFrom(
                fileTree(layout.buildDirectory) {
                    include("intermediates/javac/${variant.name}/**/*.class")
                    include("intermediates/jvm/${variant.name}/compile/${variant.name}Jvm/classes/**/*.class")
                    include("tmp/kotlin-classes/${variant.name}/**/*.class")
                    exclude(
                        "**/R.class",
                        "**/R\$*.class",
                        "**/BuildConfig.*",
                        "**/Manifest*.*",
                        "**/*_Hilt*.class",
                        "**/Hilt_*.class",
                        "**/*_Factory*",
                        "**/*Test*.*",
                        "**/*Activity*.*",
                        "**/*Fragment*.*",
                        "**/*Screen.kt",
                        "**/hilt_aggregated_deps/**",
                        "**/_HiltModules*",
                        "**/*ContentPreviewKt*",
                    )
                }
            )

            fun SourceDirectories.Flat?.toFilePaths(): Provider<List<String>> = this
                ?.all
                ?.map { directories -> directories.map { it.asFile.path } }
                ?: provider { emptyList() }
            sourceDirectories.setFrom(
                files(
                    variant.sources.java.toFilePaths(),
                    variant.sources.kotlin.toFilePaths()
                ),
            )

            val execFiles = fileTree("${layout.buildDirectory}/outputs/unit_test_code_coverage/${variant.name}UnitTest")
                .matching { include("**/*.exec") }
            executionData.setFrom(execFiles)
        }
    }

    tasks.withType<Test>().configureEach {
        configure<JacocoTaskExtension> {
            // Required for JaCoCo + Robolectric
            // https://github.com/robolectric/robolectric/issues/2230
            isIncludeNoLocationClasses = true

            // Required for JDK 11 with the above
            // https://github.com/gradle/gradle/issues/5184#issuecomment-391982009
            excludes = listOf("jdk.internal.*")
        }
    }
}

fun Project.configureJacoco() {
    pluginManager.apply("jacoco")
    val androidExtension = extensions.getByType<LibraryExtension>()

    androidExtension.buildTypes.configureEach {
        enableAndroidTestCoverage = true
        enableUnitTestCoverage = true
    }

    configureJacoco(extensions.getByType<LibraryAndroidComponentsExtension>())
}
