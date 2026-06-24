plugins {
    java
    id("org.graalvm.buildtools.native") version "0.10.5" apply false
}

allprojects {
    group = "gt.devtools"
    version = "1.0.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(25))
        }
        modularity.inferModulePath.set(true)
    }

    tasks.withType<JavaCompile>().configureEach {
        options.release.set(25)
        options.compilerArgs.addAll(listOf(
            "-Xlint:all",
            "-Xlint:-serial",
            "-Xlint:-exports",
            "-Xlint:-requires-automatic",
            "-Xlint:-requires-transitive-automatic",
            "-Xlint:-this-escape",
            "-Werror"
        ))
    }

    dependencies {
        testImplementation("org.junit.jupiter:junit-jupiter:5.12.2")
        testImplementation("org.assertj:assertj-core:3.27.3")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        // Per-module --add-opens configured in the module's build.gradle.kts
    }
}
