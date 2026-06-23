pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "developer-tools-desktop"

include(":app")
include(":modules:common")
include(":modules:settings")
include(":modules:tools-api")
include(":modules:tools-encoders")
include(":modules:tools-escape")
include(":modules:tools-crypto")
include(":modules:tools-text")
include(":modules:tools-formatters")
include(":modules:tools-standalone")
