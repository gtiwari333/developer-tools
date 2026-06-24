plugins {
    java
    application
    id("org.graalvm.buildtools.native")
    id("org.openjfx.javafxplugin")
}

javafx {
    version = "23.0.2"
    modules = listOf("javafx.controls", "javafx.graphics", "javafx.swing")
}

application {
    mainClass.set("gt.devtools.app.DevToolsApp")
    mainModule.set("gt.devtools.app")
    applicationDefaultJvmArgs = listOf(
        "--add-reads", "gt.devtools.tools.standalone=ALL-UNNAMED"
    )
}

// Never skip the run task — Gradle 9.x may mark it UP-TO-DATE incorrectly
tasks.named("run") {
    outputs.upToDateWhen { false }
}

// JavaFX launcher task — Phase 0
tasks.register<JavaExec>("runFx") {
    group = "application"
    description = "Runs the JavaFX shell (Phase 0 migration)"
    mainClass.set("gt.devtools.app.DevToolsAppFx")
    mainModule.set("gt.devtools.app")
    classpath = sourceSets["main"].runtimeClasspath
    // JavaFX modules are added by the javafx plugin automatically
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

    implementation("com.formdev:flatlaf:3.6")
    implementation("com.formdev:flatlaf-extras:3.6")
    implementation("com.fifesoft:rsyntaxtextarea:3.6.0")
    runtimeOnly("org.slf4j:slf4j-simple:2.0.18")
}

graalvmNative {
    binaries {
        named("main") {
            imageName.set("developer-tools")
            mainClass.set("gt.devtools.app.DevToolsApp")

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
                "-H:IncludeResources=META-INF/services/.*",
                "-H:+AddAllCharsets",
                "--initialize-at-build-time=org.slf4j",
                "-H:ConfigurationFileDirectories=src/main/resources/META-INF/native-image/gt.devtools.app",
                "-J-Xmx4g"
            )
        }
    }

    metadataRepository {
        enabled.set(true)
    }
}
