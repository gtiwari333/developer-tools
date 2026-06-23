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

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
}
