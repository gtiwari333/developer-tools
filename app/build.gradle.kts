plugins {
    java
    application
    id("org.graalvm.buildtools.native")
    id("org.openjfx.javafxplugin")
}

javafx {
    version = "23.0.2"
    modules = listOf("javafx.controls", "javafx.graphics")
}

application {
    mainClass.set("gt.devtools.app.DevToolsAppFx")
    mainModule.set("gt.devtools.app")
    applicationDefaultJvmArgs = listOf(
        "--add-reads", "gt.devtools.tools.standalone=ALL-UNNAMED"
    )
}

dependencies {
    implementation(project(":modules:common"))
    implementation(project(":modules:settings"))
    implementation(project(":modules:tools-api"))
    implementation(project(":modules:tools-encoders"))
    implementation(project(":modules:tools-escape"))
    implementation(project(":modules:tools-crypto"))
    implementation(project(":modules:tools-text"))
    implementation(project(":modules:tools-formatters"))
    implementation(project(":modules:tools-standalone"))

    runtimeOnly("org.slf4j:slf4j-simple:2.0.18")
}

graalvmNative {
    binaries {
        named("main") {
            imageName.set("developer-tools")
            mainClass.set("gt.devtools.app.DevToolsAppFx")

            buildArgs.addAll(
                "--enable-url-protocols=https,http",
                "--enable-all-security-services",
                "-H:+ReportExceptionStackTraces",
                "-H:IncludeResources=.*\\.properties$",
                "-H:IncludeResources=.*\\.xml$",
                "-H:IncludeResources=.*\\.json$",
                "-H:IncludeResources=.*\\.png$",
                "-H:IncludeResources=.*\\.svg$",
                "-H:IncludeResources=.*\\.ttf$",
                "-H:IncludeResources=.*\\.css$",
                "-H:IncludeResources=META-INF/services/.*",
                "-H:+AddAllCharsets",
                "--initialize-at-build-time=org.slf4j",
                "--add-modules", "javafx.controls,javafx.graphics",
                "--enable-native-access=javafx.graphics",
                "-J-Xmx4g"
            )
        }
    }

    metadataRepository {
        enabled.set(true)
    }
}
