import com.android.build.api.dsl.ApplicationExtension
import io.github.agimaulana.radio.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withGroovyBuilder
import java.io.File
import java.util.Properties

class SentryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val sentryDsn = resolveSecret("SENTRY_DSN")
            val sentryAuthToken = resolveSecret("SENTRY_AUTH_TOKEN")
            val sentryOrg = resolveProjectSetting("sentryOrg", "colonyx")
            val sentryProject = resolveProjectSetting("sentryProject", "radio247fm")

            pluginManager.apply("io.sentry.android.gradle")

            extensions.configure<ApplicationExtension> {
                buildFeatures {
                    buildConfig = true
                }
                defaultConfig {
                    buildConfigField(
                        "String",
                        "SENTRY_DSN",
                        "\"\""
                    )
                }

                productFlavors.all {
                    if (name == "prod") {
                        buildConfigField(
                            "String",
                            "SENTRY_DSN",
                            "\"${sentryDsn.escapeForBuildConfig()}\""
                        )
                    }
                }
            }

            extensions.findByName("sentry")?.withGroovyBuilder {
                setProperty("org", sentryOrg)
                setProperty("projectName", sentryProject)
                setProperty("includeProguardMapping", true)
                setProperty("autoUploadProguardMapping", true)
                setProperty("ignoredBuildTypes", listOf("debug"))
                setProperty("ignoredFlavors", listOf("dev", "staging"))
                if (sentryAuthToken.isNotBlank()) {
                    setProperty("authToken", sentryAuthToken)
                }
                "autoInstallation" {
                    setProperty("enabled", false)
                }
            }

            dependencies {
                add("implementation", libs.findLibrary("sentry.android").get())
                add("implementation", libs.findLibrary("sentry.android.timber").get())
            }
        }
    }
}

private fun Project.resolveSecret(key: String): String {
    return System.getenv(key)
        ?.takeIf(String::isNotBlank)
        ?: loadLocalProperties(rootProject.file("local.properties")).getProperty(key).orEmpty()
}

private fun Project.resolveProjectSetting(
    key: String,
    defaultValue: String,
): String {
    return providers.gradleProperty(key).orNull
        ?.takeIf(String::isNotBlank)
        ?: defaultValue
}

private fun loadLocalProperties(file: File): Properties {
    val properties = Properties()
    if (!file.exists()) return properties

    file.inputStream().use(properties::load)
    return properties
}

private fun String.escapeForBuildConfig(): String = replace("\\", "\\\\").replace("\"", "\\\"")
