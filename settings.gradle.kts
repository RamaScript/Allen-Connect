pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        jcenter()
        maven("https://jitpack.io")
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google {
            // Explicitly include WebRTC
            content {
                includeGroup("org.webrtc")
            }
        }
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://maven.google.com")
    }
}

rootProject.name = "AllenConnect"
include(":app")
 