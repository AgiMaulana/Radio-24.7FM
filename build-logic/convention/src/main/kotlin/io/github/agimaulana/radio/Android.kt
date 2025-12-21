package io.github.agimaulana.radio

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal fun Project.configureAndroid(
    commonExtension: CommonExtension<*, *, *, *, *, *>
) {
    val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

    commonExtension.apply {
        compileSdk = libs.findVersion("compileSdk").get().toString().toInt()
        ndkVersion = libs.findVersion("ndk").get().toString()

        defaultConfig {
            minSdk = libs.findVersion("minSdk").get().toString().toInt()
        }

        compileOptions {
            sourceCompatibility = JavaVersion.valueOf(libs.findVersion("jdk").get().toString())
            targetCompatibility = JavaVersion.valueOf(libs.findVersion("jdk").get().toString())
            isCoreLibraryDesugaringEnabled = true
        }

        packaging {
            resources {
                excludes.addAll(
                    listOf(
                        "/META-INF/{AL2.0,LGPL2.1}",
                        "META-INF/LICENSE.md",
                        "META-INF/LICENSE-notice.md",
                    )
                )
            }
        }

        configureKotlin {
            freeCompilerArgs.addAll(
                listOf(
                    "-opt-in=kotlin.RequiresOptIn",
                    // Enable experimental coroutines APIs, including Flow
                    "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                    "-opt-in=kotlinx.coroutines.FlowPreview",
                    "-opt-in=kotlin.Experimental",
                )
            )
            jvmTarget.set(
                JvmTarget.fromTarget(
                    libs.findVersion("jdkNumber").get().toString()
                )
            )
        }
    }

    extensions.configure<KotlinAndroidProjectExtension> {
        jvmToolchain(libs.findVersion("jdkNumber").get().toString().toInt())
    }

    dependencies {
        add("coreLibraryDesugaring", libs.findLibrary("android.desugarJdkLibs").get())
    }

    tasks.withType(Test::class.java) {
        testLogging {
            events(TestLogEvent.SKIPPED, TestLogEvent.FAILED)
            exceptionFormat = TestExceptionFormat.FULL
            showExceptions = true
            showCauses = true
            showStackTraces = true
        }
    }
}

private fun Project.configureKotlin(block: KotlinJvmCompilerOptions.() -> Unit) {
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions(block)
    }
}
