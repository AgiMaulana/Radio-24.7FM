pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Boilerplate"
include(":app")

include(
    ":feature:sample",
    ":feature:stationlist"
)

include(
    ":core:common",
    ":core:design",
    ":core:network",
    ":core:network:test",
    ":core:shared-test",
)

include(
    ":domain:api",
    ":domain:impl",
)

include(":infrastructure")
