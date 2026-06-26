import org.gradle.jvm.toolchain.JavaToolchainService

plugins {
    java
    application
    id("org.graalvm.buildtools.native")
    id("org.openjfx.javafxplugin")
}

javafx {
    version = "25.0.3"
    modules = listOf("javafx.controls", "javafx.graphics")
}

// JVM args shared between the application plugin and the runtime-packaging scripts
val appDefaultJvmArgs = listOf(
    "--add-reads", "gt.devtools.tools.standalone=ALL-UNNAMED"
)

application {
    mainClass.set("gt.devtools.app.DevToolsAppFx")
    mainModule.set("gt.devtools.app")
    applicationDefaultJvmArgs = appDefaultJvmArgs
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

// ============================================================
// Runtime packaging — jlink minimal JRE + ZIP distribution + jpackage
//
// Produces:
//   ./gradlew :app:runtimeZip      → build/distributions/developer-tools-<ver>-linux-x64.zip
//   ./gradlew :app:jpackageImage   → build/jpackage/developer-tools/  (native installer image)
// ============================================================

// JDK modules to bake into the minimal jlink runtime image.
// Covers the transitive closure of the app's JDK dependencies.
val jlinkModules = listOf(
    "java.base",
    "java.desktop",       // JavaFX depends on AWT/Swing internals
    "java.net.http",      // settings → UpdateChecker
    "jdk.httpserver",     // tools-standalone → HttpServerToolFx
    "java.logging",       // SLF4J → java.util.logging bridge
    "java.management",    // JMX (monitoring / management)
    "jdk.unsupported",    // sun.misc.Unsafe (various libraries)
    "jdk.crypto.ec",      // elliptic-curve TLS certificates
    "java.xml",           // XML parsing (Jackson XML, DOM)
    "java.scripting",     // commons-text (StringEscapeUtils)
    "jdk.zipfs"           // zip filesystem provider
)

val jlinkModulesString = jlinkModules.joinToString(",")

// Resolve a JDK 25 that contains jmods/ for use as the jlink source.
// Temurin 25.0.2 ships as a pre-linked image (no jmods), so we fall back
// to GraalVM 25.0.3 which ships the full set of JDK module packages.
val toolchainService = project.extensions.getByType<JavaToolchainService>()

val jlinkSourceJdk: File = run {
    // 1. Try the Gradle toolchain for a JDK 25 with jmods
    val tcJdk = try {
        toolchainService.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(25))
        }.get().metadata.installationPath.asFile
    } catch (_: Exception) { null }

    if (tcJdk != null && File(tcJdk, "jmods").isDirectory) tcJdk
    else {
        // 2. Try a known JDK 25 path (sdkman GraalVM)
        val known25 = File("/home/gt/.sdkman/candidates/java/25.0.3-graal")
        if (known25.resolve("jmods").isDirectory) known25
        else {
            // 3. Fallback to JAVA_HOME (CI: graalvm/setup-graalvm action)
            val envJavaHome = System.getenv("JAVA_HOME")
            if (envJavaHome != null && File(envJavaHome, "jmods").isDirectory) File(envJavaHome)
            else known25  // last resort (will fail fast if no jmods)
        }
    }
}

// --- jlink: create a minimal JRE ---
val createRuntime = tasks.register<Exec>("createRuntime") {
    description = "Runs jlink to produce a minimal JRE at build/runtime"
    dependsOn(tasks.installDist)

    val jlinkBin  = File(jlinkSourceJdk, "bin/jlink").absolutePath
    val jmodsDir  = File(jlinkSourceJdk, "jmods").absolutePath
    val outputDir = layout.buildDirectory.dir("runtime")

    inputs.property("jlinkModules", jlinkModules)
    inputs.property("jlinkSourceJdk", jlinkSourceJdk.absolutePath)
    outputs.dir(outputDir)

    // jlink refuses to overwrite an existing image directory
    doFirst {
        outputDir.get().asFile.deleteRecursively()
    }

    commandLine(
        jlinkBin,
        "--output",                  outputDir.get().asFile.absolutePath,
        "--module-path",             jmodsDir,
        "--add-modules",             jlinkModulesString,
        "--strip-debug",
        "--no-man-pages",
        "--no-header-files",
        "--compress",                "2",
        "--bind-services"
    )

    // Ensure the jlink runtime binaries have execute permissions.
    // jlink normally creates them with +x, but certain filesystems
    // or CI environments may strip the bits.
    doLast {
        val binDir = outputDir.get().asFile.resolve("bin")
        if (binDir.isDirectory) {
            binDir.listFiles()?.forEach { it.setExecutable(true) }
        }
    }
}

// --- Shared content for runtime archives ---
// Used by both runtimeTarGz (Unix — preserves +x) and runtimeZip (Windows).

// Captured at project scope so the extension function on AbstractArchiveTask
// can reference them (inside the function, 'this' is the task, not the project).
val archiveAppModule   = application.mainModule.get()
val archiveAppClass    = application.mainClass.get()
@Suppress("UNCHECKED_CAST")
val archiveJvmArgs     = appDefaultJvmArgs
val archiveInstallDist = tasks.installDist.map { it.destinationDir }

fun <T : AbstractArchiveTask> T.configureRuntimeContent() {
    // Embed the jlink minimal runtime
    from(layout.buildDirectory.dir("runtime")) {
        into("developer-tools/runtime")
    }

    // Copy all app JARs (modular + automatic + unnamed) from installDist
    from(archiveInstallDist) {
        into("developer-tools")
        exclude("bin/**")
    }

    // ---- Custom launch scripts ----

    val jvmArgsStr = archiveJvmArgs.joinToString(" ") { it }
    val dollar     = '$'

    doFirst {
        val scriptsDir = temporaryDir.resolve("scripts")
        scriptsDir.mkdirs()

        // Unix / macOS launch script
        val unixScript = scriptsDir.resolve("developer-tools")
        unixScript.writeText(
            """
            |#!/bin/sh
            |# Developer Tools — launch script (embedded JRE)
            |APP_HOME="${dollar}(cd "${dollar}(dirname "${dollar}0")/.." && pwd)"
            |export JAVA_HOME="${dollar}APP_HOME/runtime"
            |
            |exec "${dollar}JAVA_HOME/bin/java" \
            |    --module-path "${dollar}APP_HOME/lib" \
            |    --module $archiveAppModule/$archiveAppClass \
            |    $jvmArgsStr \
            |    "${dollar}@"
            """.trimMargin()
        )
        unixScript.setExecutable(true)

        // Windows launch script
        val winScript = scriptsDir.resolve("developer-tools.bat")
        winScript.writeText(
            """
            |@echo off
            |REM Developer Tools — launch script (embedded JRE)
            |set APP_HOME=%~dp0..
            |set JAVA_HOME=%APP_HOME%\runtime
            |
            |"%JAVA_HOME%\bin\java.exe" ^
            |    --module-path "%APP_HOME%\lib" ^
            |    --module $archiveAppModule/$archiveAppClass ^
            |    $jvmArgsStr ^
            |    %*
            """.trimMargin()
        )
    }

    from(temporaryDir.resolve("scripts")) {
        into("developer-tools/bin")
    }
}

// --- Tar.gz: Unix distribution (preserves POSIX execute permissions) ---
// Uses native 'tar' rather than Gradle's Tar task because Gradle 9.6 defaults
// fileMode to 0644 and doesn't expose mode/setMode on FileCopyDetails in Kotlin
// DSL, making it impossible to preserve +x bits through the Gradle Tar task.
// Native tar preserves permissions from the source filesystem correctly.
val runtimeTarGz = tasks.register<Exec>("runtimeTarGz") {
    description = "Creates a tar.gz distribution with an embedded JRE (Unix — preserves +x)"
    group = "distribution"
    dependsOn(createRuntime, tasks.installDist)

    val classifier = providers.gradleProperty("runtimeClassifier").orElse("linux-x64")
    val version    = project.version.toString()
    val archiveName = "developer-tools-${version}-${classifier.get()}.tar.gz"
    val destDir     = layout.buildDirectory.dir("distributions").get().asFile
    val buildDir    = layout.buildDirectory.get().asFile
    val runtimeDir  = layout.buildDirectory.dir("runtime").get().asFile
    val installDir  = tasks.installDist.get().destinationDir

    // Build a staging directory with the correct layout, then tar it.
    // Using doFirst so the staging dir is created fresh each run.
    val stagingDir = layout.buildDirectory.dir("staging/developer-tools").get().asFile

    inputs.dir(runtimeDir)
    inputs.dir(installDir)
    outputs.file(File(destDir, archiveName))

    doFirst {
        stagingDir.parentFile.mkdirs()
        stagingDir.deleteRecursively()
        stagingDir.mkdirs()

        // Copy runtime
        project.copy {
            from(runtimeDir)
            into(File(stagingDir, "runtime"))
        }
        // The project.copy may also strip permissions, so ensure +x on bin/
        File(stagingDir, "runtime/bin").listFiles()?.forEach { it.setExecutable(true) }

        // Copy app libs
        project.copy {
            from(installDir) {
                exclude("bin/**")
            }
            into(stagingDir)
        }

        // Create and set permissions on the launch script
        val binDir = File(stagingDir, "bin")
        binDir.mkdirs()

        val appMainModule = application.mainModule.get()
        val appMainClass  = application.mainClass.get()
        val jvmArgs       = appDefaultJvmArgs.joinToString(" ") { it }
        val dollar        = '$'

        val scriptFile = File(binDir, "developer-tools")
        scriptFile.writeText(
            """
            |#!/bin/sh
            |# Developer Tools — launch script (embedded JRE)
            |APP_HOME="${dollar}(cd "${dollar}(dirname "${dollar}0")/.." && pwd)"
            |export JAVA_HOME="${dollar}APP_HOME/runtime"
            |
            |exec "${dollar}JAVA_HOME/bin/java" \
            |    --module-path "${dollar}APP_HOME/lib" \
            |    --module $appMainModule/$appMainClass \
            |    $jvmArgs \
            |    "${dollar}@"
            """.trimMargin()
        )
        scriptFile.setExecutable(true)

        val batFile = File(binDir, "developer-tools.bat")
        batFile.writeText(
            """
            |@echo off
            |REM Developer Tools — launch script (embedded JRE)
            |set APP_HOME=%~dp0..
            |set JAVA_HOME=%APP_HOME%\runtime
            |
            |"%JAVA_HOME%\bin\java.exe" ^
            |    --module-path "%APP_HOME%\lib" ^
            |    --module $appMainModule/$appMainClass ^
            |    $jvmArgs ^
            |    %*
            """.trimMargin()
        )
    }

    workingDir = stagingDir.parentFile
    commandLine(
        "tar", "czf", File(destDir, archiveName).absolutePath,
        "developer-tools"
    )

    doLast {
        val tarFile = File(destDir, archiveName)
        logger.lifecycle("Runtime tar.gz: ${tarFile.absolutePath} (${"%,d".format(tarFile.length())} bytes)")
    }
}

// --- ZIP: Windows distribution (for .bat scripts, easier for Windows users) ---
tasks.register<Zip>("runtimeZip") {
    description = "Creates a ZIP distribution with an embedded JRE (Windows-friendly)"
    group = "distribution"
    dependsOn(createRuntime, tasks.installDist)

    archiveBaseName.set("developer-tools")
    archiveClassifier.set(providers.gradleProperty("runtimeClassifier").orElse("linux-x64"))
    destinationDirectory.set(layout.buildDirectory.dir("distributions"))

    configureRuntimeContent()

    doLast {
        val zip = archiveFile.get().asFile
        logger.lifecycle("Runtime ZIP: ${zip.absolutePath} (${"%,d".format(zip.length())} bytes)")
    }
}

// --- jpackage: create a native platform installer image ---
tasks.register<Exec>("jpackageImage") {
    description = "Runs jpackage to create a native installer image (requires createRuntime)"
    group = "distribution"
    dependsOn(createRuntime, tasks.installDist)

    val jpackageBin = File(jlinkSourceJdk, "bin/jpackage").absolutePath
    val runtimeDir = layout.buildDirectory.dir("runtime").get().asFile
    val installDir = tasks.installDist.get().destinationDir
    val outputDir  = layout.buildDirectory.dir("jpackage").get().asFile

    inputs.dir(runtimeDir)
    inputs.dir(installDir)
    outputs.dir(outputDir)

    commandLine(
        jpackageBin,
        "--type",                    "app-image",
        "--name",                    "DeveloperTools",
        "--app-version",             project.version.toString(),
        "--input",                   installDir.absolutePath + "/lib",
        "--main-jar",                ".",        // we use --module instead
        "--main-class",              application.mainClass.get(),
        "--module",                  application.mainModule.get() + "/" + application.mainClass.get(),
        "--module-path",             installDir.absolutePath + "/lib",
        "--runtime-image",           runtimeDir.absolutePath,
        "--dest",                    outputDir.absolutePath,
        "--java-options",            application.applicationDefaultJvmArgs.joinToString(" "),
        "--add-modules",             "javafx.controls,javafx.graphics"
    )
}
