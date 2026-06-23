plugins { java }

dependencies {
    implementation(project(":modules:common"))
    implementation(project(":modules:settings"))
    implementation(project(":modules:tools-api"))
    implementation("io.github.java-diff-utils:java-diff-utils:4.15")
}
